package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import net.arjun.pool.PoolRooms;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PoolWorldState extends PersistentState {
	public long seed;
	public static PoolWorldState instance;
	public List<Vec3d> playerPoses;

//	public int gridSizeXPositive = 10;
//	public int gridSizeZPositive = 10;

//	public int gridSizeXNegative = 9;
//	public int gridSizeZNegative = 9;

	public final int gridSquareLength = 20;

	public Pair<Integer, Integer> currentGridSquare = Pair.of(0, 0);

	public final Set<Pair<Integer, Integer>> generatedGridSquares = ConcurrentHashMap.newKeySet();

//	public int[] newGridCoords = new int[]{gridSizeXPositive,-gridSizeXNegative,gridSizeZPositive,-gridSizeZNegative};

	private Random random;

	private volatile boolean isGenerating = false;
	private boolean isFirstGenerating = true;

	private final Map<Pair<Integer, Integer>, RoomNode> rooms = new ConcurrentHashMap<>(); // all rooms and their positions
	private final Stack<RoomNode> northStack = new Stack<>();
	private final Stack<RoomNode> southStack = new Stack<>();
	private final Stack<RoomNode> eastStack = new Stack<>();
	private final Stack<RoomNode> westStack = new Stack<>();
	private final ArrayList<RoomNode> outOfBoundsRooms = new ArrayList<>();

	private PoolWorldState() {
		this.seed = new Random().nextLong(); // new world, random seed
		instance = this;
		random = new Random(seed);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putLong("seed", seed);
		return nbt;
	}

	public static PoolWorldState get(ServerWorld world) {
		instance = world.getPersistentStateManager()
			.getOrCreate(
				PoolWorldState::fromNbt,
				PoolWorldState::new,
				"pool_world_state");

		return instance;
	}

	private static PoolWorldState fromNbt(NbtCompound nbt) {
		PoolWorldState state = new PoolWorldState();
		state.seed = nbt.getLong("seed");
		state.random = new Random(state.seed);
		return state;
	}

	public Map<Pair<Integer, Integer>, RoomNode> generateMap() {

		int usedSlots = 0;

		int gridX1 = currentGridSquare.first * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridX2 = currentGridSquare.first * gridSquareLength + (gridSquareLength / 2);
		int gridZ1 = currentGridSquare.second * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridZ2 = currentGridSquare.second * gridSquareLength + (gridSquareLength / 2);

		ArrayList<RoomNode> boundaryRoomsToConnect = new ArrayList<>();

		if (random == null) {
			random = new Random(seed);
		}


		if (isFirstGenerating) {

			RoomNode start = new RoomNode(-1, -1, 3, 3, RoomSize.START, null);

			start.northConnectionPosition = Pair.of(start.gridX + 1, start.gridZ);
			start.westConnectionPosition = Pair.of(start.gridX, start.gridZ + 1);
			start.eastConnectionPosition = Pair.of(start.gridX + start.gridLengthX - 1, start.gridZ + 1);
			start.southConnectionPosition = Pair.of(start.gridX + 1, start.gridZ + start.gridLengthZ - 1);
			usedSlots += 9;


			// CODE TO ADD A ROOM ------------------------------------------
			for (int x = start.gridX; x < start.gridX + start.gridLengthX; x++) {
				for (int z = start.gridZ; z < start.gridZ + start.gridLengthZ; z++) {
					rooms.put(Pair.of(x, z), start);
				}
			}

			RoomNode northRoom = new RoomNode(
				start.northConnectionPosition.first,
				start.northConnectionPosition.second - 1,
				1,
				1,
				RoomSize.R1x1,
				Direction.NORTH
			);
			northRoom.createConnectionPositions();
			start.northConnection = northRoom;
			northRoom.southConnection = start;

			RoomNode southRoom = new RoomNode(
				start.southConnectionPosition.first,
				start.southConnectionPosition.second + 1,
				1,
				1,
				RoomSize.R1x1,
				Direction.SOUTH
			);
			southRoom.createConnectionPositions();
			start.southConnection = southRoom;
			southRoom.northConnection = start;

			RoomNode westRoom = new RoomNode(
				start.westConnectionPosition.first - 1,
				start.westConnectionPosition.second,
				1,
				1,
				RoomSize.R1x1,
				Direction.WEST
			);
			westRoom.createConnectionPositions();
			start.westConnection = westRoom;
			westRoom.eastConnection = start;

			RoomNode eastRoom = new RoomNode(
				start.eastConnectionPosition.first + 1,
				start.eastConnectionPosition.second,
				1,
				1,
				RoomSize.R1x1,
				Direction.EAST
			);
			eastRoom.createConnectionPositions();
			start.eastConnection = eastRoom;
			eastRoom.westConnection = start;

			rooms.put(Pair.of(northRoom.gridX, northRoom.gridZ), northRoom);
			rooms.put(Pair.of(southRoom.gridX, southRoom.gridZ), southRoom);
			rooms.put(Pair.of(westRoom.gridX, westRoom.gridZ), westRoom);
			rooms.put(Pair.of(eastRoom.gridX, eastRoom.gridZ), eastRoom);

			northStack.push(northRoom);
			southStack.push(southRoom);
			eastStack.push(eastRoom);
			westStack.push(westRoom);
			usedSlots += 4;

			isFirstGenerating = false;
		} else {

			int i = 0;
			int size = outOfBoundsRooms.size();

			// seed in a room in the middle of the new grid square

			Pair<Integer, Integer> seedCoords = Pair.of((gridX1 + gridX2 - 1) / 2, (gridZ1 + gridZ2 - 1) / 2);
			RoomNode seedRoom = new RoomNode(seedCoords.first, seedCoords.second, 1, 1, RoomSize.R1x1, Direction.NORTH);

			rooms.put(seedCoords, seedRoom);

			northStack.push(seedRoom);
			eastStack.push(seedRoom);
			southStack.push(seedRoom);
			westStack.push(seedRoom);

			while (i < size) {

				RoomNode possibleRoom = outOfBoundsRooms.get(i);

				if (isInsideBounds(possibleRoom.gridX, possibleRoom.gridZ)) {

					rooms.put(Pair.of(possibleRoom.gridX, possibleRoom.gridZ), possibleRoom);

					boundaryRoomsToConnect.add(possibleRoom);

					outOfBoundsRooms.remove(i);
				} else {
					i++;
				}

				size = outOfBoundsRooms.size();
			}

		}

		usedSlots = rooms.size();
//		int targetSlots = (gridSizeXNegative + gridSizeXPositive + 1) * (gridSizeZNegative + gridSizeZPositive + 1);
		int iterations = 0;

		int stackEmptyStreak = 0;

		boolean roomRequiresConnection = false;


		while (true) {
			RoomNode room;
			Stack<RoomNode> stack;

			if (iterations % 4 == 0) { // use north stack
				stack = northStack;
			} else if (iterations % 4 == 1) { // use east stack
				stack = eastStack;
			} else if (iterations % 4 == 2) { // use south stack
				stack = southStack;
			} else { // use west stack
				stack = westStack;
			}
			iterations++;

			if (stack.isEmpty()) {
				stackEmptyStreak++;
				if (stackEmptyStreak == 4) {
//					System.out.println("PoolWorldState: stopped expansion early; no frontier rooms remain (" + usedSlots + "/" + targetSlots + " slots).");
					break;
				}
				continue;
			}
			stackEmptyStreak = 0;
			room = stack.peek();


			ArrayList<Direction> dirs = new ArrayList<>();

			int gridX = room.gridX +room.northConnectionPosition.first-1;
			int gridZ = room.gridZ +room.northConnectionPosition.second-1;

			if (room.northConnection == null && !rooms.containsKey(Pair.of(gridX, gridZ - 1))) { //  && gridZ != -(gridSizeZNegative-1)
				dirs.add(Direction.NORTH);
			} else if (boundaryRoomsToConnect.contains(rooms.get(Pair.of(gridX, gridZ - 1))) || roomRequiresConnection) {
				RoomNode foundBoundaryRoom = rooms.get(Pair.of(gridX, gridZ - 1));
				foundBoundaryRoom.southConnection = room;
				room.northConnection = foundBoundaryRoom;
				boundaryRoomsToConnect.remove(foundBoundaryRoom);
				continue;
			}
			if (room.eastConnection == null && !rooms.containsKey(Pair.of(gridX + 1, gridZ))) { //  && gridX != gridSizeXPositive
				dirs.add(Direction.EAST);
			} else if (boundaryRoomsToConnect.contains(rooms.get(Pair.of(gridX + 1, gridZ))) || roomRequiresConnection) {
				RoomNode foundBoundaryRoom = rooms.get(Pair.of(gridX + 1, gridZ));
				foundBoundaryRoom.westConnection = room;
				room.eastConnection = foundBoundaryRoom;
				boundaryRoomsToConnect.remove(foundBoundaryRoom);
				continue;
			}
			if (room.southConnection == null && !rooms.containsKey(Pair.of(gridX, gridZ + 1))) { //  && gridZ != gridSizeZPositive
				dirs.add(Direction.SOUTH);
			} else if (boundaryRoomsToConnect.contains(rooms.get(Pair.of(gridX, gridZ + 1))) || roomRequiresConnection) {
				RoomNode foundBoundaryRoom = rooms.get(Pair.of(gridX, gridZ + 1));
				foundBoundaryRoom.northConnection = room;
				room.southConnection = foundBoundaryRoom;
				boundaryRoomsToConnect.remove(foundBoundaryRoom);
				continue;
			}
			if (room.westConnection == null && !rooms.containsKey(Pair.of(gridX - 1, gridZ))) { //  && gridX != -(gridSizeXNegative-1)
				dirs.add(Direction.WEST);
			} else if (boundaryRoomsToConnect.contains(rooms.get(Pair.of(gridX - 1, gridZ))) || roomRequiresConnection) {
				RoomNode foundBoundaryRoom = rooms.get(Pair.of(gridX - 1, gridZ));
				foundBoundaryRoom.eastConnection = room;
				room.westConnection = foundBoundaryRoom;
				boundaryRoomsToConnect.remove(foundBoundaryRoom);
				continue;
			}

			if (dirs.isEmpty()) {
				stack.pop();

				continue;
			}

			Direction dir = dirs.get(random.nextInt(0, dirs.size()));

			int[] offset = new int[2];

			RoomNode newRoom = new RoomNode();

			int availableConnections = availableConnections1x1(room);

			if ((dir == Direction.NORTH && isInsideBounds(gridX, gridZ - 1) || (dir == Direction.NORTH && availableConnections != 1))) {
				offset[1] = -1;
			} else if ((dir == Direction.EAST && isInsideBounds(gridX + 1, gridZ) || (dir == Direction.EAST && availableConnections != 1))) {
				offset[0] = 1;
				room.eastConnection = newRoom;
				newRoom.westConnection = room;
			} else if (((dir == Direction.SOUTH && isInsideBounds(gridX, gridZ + 1) || (dir == Direction.SOUTH && availableConnections != 1)))) {
				offset[1] = 1;
				room.southConnection = newRoom;
				newRoom.northConnection = room;
			} else if (((dir == Direction.WEST && isInsideBounds(gridX - 1, gridZ) || (dir == Direction.WEST && availableConnections != 1)))) {
				offset[0] = -1;
				room.westConnection = newRoom;
				newRoom.eastConnection = room;
			} else {
				stack.pop();
				continue;
			}

			newRoom.gridX = gridX + offset[0];
			newRoom.gridZ = gridZ + offset[1];

			if (check2x2SpaceAvailable(rooms, newRoom.gridX, newRoom.gridZ, dir) && usedSlots % 10 == 0) {
				newRoom.gridLengthX = 2;
				newRoom.gridLengthZ = 2;
				newRoom.roomSize = RoomSize.R2x2;
				roomRequiresConnection = true;
			} else {
				newRoom.gridLengthX = 1;
				newRoom.gridLengthZ = 1;
				newRoom.roomSize = RoomSize.R1x1;
			}

			newRoom.generationDirection = dir;
			newRoom.createConnectionPositions();


			if (!isInsideBounds(newRoom)) { // todo update isInsideBounds() to account for larger rooms
				outOfBoundsRooms.add(newRoom);
				continue;
			}

			rooms.put(Pair.of(newRoom.gridX, newRoom.gridZ), newRoom);
			stack.push(newRoom);
			usedSlots++;

		}

		System.out.println("map gen done");

//		-------------------------------------------------------------

//		while (!frontier.isEmpty() && roomsCount < roomsToGenerate) {
//			RoomNode currentRoom = frontier.poll(); // we need to generate more rooms from this room (branches)
//			int branchesToGenerate = random.nextInt(0,4)+1; // generate 1-2 branches per room
//			if (branchesToGenerate <= 3) branchesToGenerate = 1; else branchesToGenerate = 2;
//			if (currentRoom == start) branchesToGenerate = 4; // if this is the starting room, generate 4 branches
//			List<Direction> dirs = shuffledDirections(random.nextLong(), oppositeOf(currentRoom.generationDirection));
//
////			Loop through all directions
//			for (int i = 0; i < branchesToGenerate; i++) {
//				Direction dir = dirs.get(i);
//
////				If we are not allowed to go in a direction from currentRoom, continue
//				if (dir == Direction.NORTH && !currentRoom.northAllowed) continue;
//				if (dir == Direction.EAST && !currentRoom.eastAllowed) continue;
//				if (dir == Direction.SOUTH && !currentRoom.southAllowed) continue;
//				if (dir == Direction.WEST && !currentRoom.westAllowed) continue;
//
//				int nx = currentRoom.gridX;
//				int nz = currentRoom.gridZ;
//
//				boolean chosenDir = false;
//
//				for (int j = 0; j < 4 && !chosenDir; j++) {
//					switch (dir) {
//						case NORTH:
//							nx = currentRoom.northConnectionPosition.first;
//							nz = currentRoom.northConnectionPosition.second - 1;
//							break;
//						case SOUTH:
//							nx = currentRoom.southConnectionPosition.first; // Move past the bottom edge
//							nz = currentRoom.southConnectionPosition.second + 1;
//							break;
//						case WEST:
//							nx = currentRoom.westConnectionPosition.first - 1;
//							nz = currentRoom.westConnectionPosition.second;
//							break;
//						case EAST:
//							nx = currentRoom.eastConnectionPosition.first + 1;
//							nz = currentRoom.eastConnectionPosition.second;
//							break;
//					}
//
//					if (rooms.containsKey(Pair.of(nx, nz))) { // if we stumbled upon a room
//						if (hasReachedConnectionLimit(rooms.get(Pair.of(nx, nz))) || !dirAllowed(rooms.get(Pair.of(nx, nz)), dir)) { // if this room has maxConnections already
//							if (getOtherDir(dir, currentRoom) != null) { // if there are other directions we can go to
//								dir = getOtherDir(dir, currentRoom);
//								continue;
//							} else {
//								break;
//							}
//						}
//					}
//
//					chosenDir = true;
//				}
//
//				if (!chosenDir) continue;
//
//				Pair<Integer,Integer> key = Pair.of(nx,nz);
//
//				if (!rooms.containsKey(key)) { // we can generate a new room here
//
//					boolean createdRoom = false;
//					boolean failed = false;
//
//					RoomNode newRoom = null;
//
////					2x2
//					if (counter % 7 == 0 && !checkProximity(rooms, Pair.of(nx,nz), 10, RoomSize.R2x2)) { // 2x2 room
//						newRoom = new RoomNode(dir, nx, nz, RoomSize.R2x2);
//						newRoom.createRoomCoords();
//
//						if (check2x2SpaceAvailable(rooms, newRoom)) {
//							roomsCount++;
//							createdRoom = true;
//						}
//					}
//
////					1x2
//					if (!createdRoom && counter % 15 == 0 && !checkProximity(rooms, Pair.of(nx,nz), 10, RoomSize.R1x2)) {
//						newRoom = new RoomNode(dir, nx, nz, RoomSize.R1x2);
//						newRoom.createRoomCoords();
//
//						if (check1x2SpaceAvailable(rooms, newRoom)) {
//							roomsCount++;
//							createdRoom = true;
//						}
//					}
//
////					1x1
//					if (!createdRoom) {
//						newRoom = new RoomNode(nx,nz,1,1,RoomSize.R1x1, dir);
//					}
//
//					newRoom.createConnectionPositions();
//
//					if (dir == Direction.NORTH) { // set connections with CURRENT <-> PREVIOUS
//						currentRoom.northConnection = newRoom;
//						newRoom.southConnection = currentRoom;
//					} else if (dir == Direction.EAST) {
//						currentRoom.eastConnection = newRoom;
//						newRoom.westConnection = currentRoom;
//					} else if (dir == Direction.SOUTH) {
//						currentRoom.southConnection = newRoom;
//						newRoom.northConnection = currentRoom;
//					} else if (dir == Direction.WEST) {
//						currentRoom.westConnection = newRoom;
//						newRoom.eastConnection = currentRoom;
//					}
//
////					Placing room on the map
//					for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
//						for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
//							rooms.put(Pair.of(x, z), newRoom);
//						}
//					}
//
////					Adding room to frontier and finalization
//					frontier.add(newRoom);
//
//				} else {
//					RoomNode existingRoom = rooms.get(key);
//
//// Check if we can add more connections to the existing room
//					if (hasReachedConnectionLimit(existingRoom)) continue;
//
//					// Check if current room can accept more connections
//					if (hasReachedConnectionLimit(currentRoom)) continue;
//
//					if (dir == Direction.NORTH && existingRoom.southAllowed) {
//						if (Objects.equals(Pair.of(nx, nz), existingRoom.northConnectionPosition)) {
//							currentRoom.northConnection = existingRoom;
//							existingRoom.southConnection = currentRoom;
//						}
//					} else if (dir == Direction.EAST && existingRoom.westAllowed) {
//						if (Objects.equals(Pair.of(nx, nz), existingRoom.eastConnectionPosition)) {
//							currentRoom.eastConnection = existingRoom;
//							existingRoom.westConnection = currentRoom;
//						}
//					} else if (dir == Direction.SOUTH && existingRoom.northAllowed) {
//						if (Objects.equals(Pair.of(nx, nz), existingRoom.southConnectionPosition)) {
//							currentRoom.southConnection = existingRoom;
//							existingRoom.northConnection = currentRoom;
//						}
//					} else if (dir == Direction.WEST && existingRoom.eastAllowed) {
//						if (Objects.equals(Pair.of(nx, nz), existingRoom.westConnectionPosition)) {
//							currentRoom.westConnection = existingRoom;
//							existingRoom.eastConnection = currentRoom;
//						}
//					}
//				}
//			}
//
//			counter++;
//		}

//		Room IDs
		for (RoomNode room : rooms.values()) {
			room.structureId = RoomNode.chooseStructure(room);
		}

		generatedGridSquares.add(currentGridSquare);

		return rooms;

	}


//	Helpers -----------

	private boolean isInsideBounds(int gx, int gz) {
		int gridX1 = currentGridSquare.first * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridX2 = currentGridSquare.first * gridSquareLength + (gridSquareLength / 2);
		int gridZ1 = currentGridSquare.second * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridZ2 = currentGridSquare.second * gridSquareLength + (gridSquareLength / 2);

		return gx >= gridX1 &&
			gx <= gridX2 &&
			gz >= gridZ1 &&
			gz <= gridZ2;
	}

	private boolean isInsideBounds(RoomNode room) {
		int gridX1 = currentGridSquare.first * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridX2 = currentGridSquare.first * gridSquareLength + (gridSquareLength / 2);
		int gridZ1 = currentGridSquare.second * gridSquareLength - ((gridSquareLength / 2) - 1);
		int gridZ2 = currentGridSquare.second * gridSquareLength + (gridSquareLength / 2);

		int roomStartX = room.gridX;
		int roomEndX = room.gridX + room.gridLengthX - 1;

		int roomStartZ = room.gridZ;
		int roomEndZ = room.gridZ + room.gridLengthZ - 1;

		return roomStartX >= gridX1 &&
			roomEndX <= gridX2 &&
			roomStartZ >= gridZ1 &&
			roomEndZ <= gridZ2;
	}

	private boolean check2x2SpaceAvailable(Map<Pair<Integer, Integer>, RoomNode> map, int gx, int gz, Direction direction) {
		int xOffset = 0;
		int zOffset = 0;

		if (direction == Direction.SOUTH) {

		} else if (direction == Direction.WEST) {
			xOffset -= 1;
		} else if (direction == Direction.NORTH) {
			xOffset -= 1;
			zOffset -= 1;
		} else if (direction == Direction.EAST) {
			zOffset -= 1;
		}

		if (map.containsKey(Pair.of(gx + xOffset, gz + zOffset))) return false;
		if (map.containsKey(Pair.of(gx + xOffset + 1, gz + zOffset))) return false;
		if (map.containsKey(Pair.of(gx + xOffset, gz + 1 + zOffset))) return false;
		if (map.containsKey(Pair.of(gx + xOffset + 1, gz + 1 + zOffset))) return false;

		return true;

	}

//	private boolean check2x2SpaceAvailable(Map<Pair<Integer, Integer>, RoomNode> map, RoomNode room) {
//		if (map.containsKey(Pair.of(room.gridX, room.gridZ))) return false;
//		if (map.containsKey(Pair.of(room.gridX + room.gridLengthX - 1, room.gridZ))) return false;
//		if (map.containsKey(Pair.of(room.gridX, room.gridZ + room.gridLengthZ))) return false;
//		if (map.containsKey(Pair.of(room.gridX + room.gridLengthX - 1, room.gridZ + room.gridLengthZ))) return false;
//
//		return true;
//	}

	private boolean check1x2SpaceAvailable(Map<Pair<Integer, Integer>, RoomNode> map, int gx, int gz, int lengthX, int lengthZ) {
		if (map.containsKey(Pair.of(gx, gz))) return false;
		if (map.containsKey(Pair.of(gx + lengthX - 1, gz + lengthZ - 1))) return false;

		return true;
	}

	private boolean check1x2SpaceAvailable(Map<Pair<Integer, Integer>, RoomNode> map, RoomNode room) {
		if (map.containsKey(Pair.of(room.gridX, room.gridZ))) return false;
		if (map.containsKey(Pair.of(room.gridX + room.gridLengthX - 1, room.gridZ + room.gridLengthZ - 1)))
			return false;

		return true;
	}

	private List<Direction> shuffledDirections(long seed, Direction exclude) {
		Random random = new Random(seed);

		List<Direction> dirs = new ArrayList<>(Arrays.asList(
			Direction.NORTH,
			Direction.EAST,
			Direction.SOUTH,
			Direction.WEST
		));

		if (exclude != null) {
			dirs.remove(exclude);
			dirs.add(oppositeOf(exclude));
			dirs.add(oppositeOf(exclude));
			dirs.add(oppositeOf(exclude));
		}

		Collections.shuffle(dirs, random);
		return dirs;
	}

	public static Direction oppositeOf(Direction dir) {
		if (dir == Direction.NORTH) return Direction.SOUTH;
		if (dir == Direction.EAST) return Direction.WEST;
		if (dir == Direction.SOUTH) return Direction.NORTH;
		if (dir == Direction.WEST) return Direction.EAST;
		return null;
	}

	private boolean checkProximity(Map<Pair<Integer, Integer>, RoomNode> map, Pair<Integer, Integer> coord, int radius, RoomSize type) {
		final int detail = 10; // the lower, the more accurate

		int originalX = coord.first;
		int originalZ = coord.second;

		for (int degrees = 0; degrees < 360; degrees += detail) {
			double aX = originalX; // actual x
			double aZ = originalZ; // actual z

			double xInterval = Math.sin(Math.toRadians(degrees));
			double zInterval = -Math.cos(Math.toRadians(degrees));

			for (int i = 0; i < radius; i++) {
				aX += xInterval;
				aZ += zInterval;

				if (map.containsKey(Pair.of((int) (aX), (int) (aZ)))) {
					if (map.get(Pair.of((int) (aX), (int) (aZ))).roomSize == type) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Gets the maximum number of connections allowed for a room based on its size
	 */
	private int getMaxConnections(RoomNode room) {
		if (room.roomSize == RoomSize.R1x1) {
			return 3;
		} else if (room.roomSize == RoomSize.R2x2 || room.roomSize == RoomSize.R1x2) {
			return 2;
		} else if (room.roomSize == RoomSize.START) {
			return 4; // or whatever limit you want for start rooms
		}
		return 4; // default for any other room types
	}

	/**
	 * Checks if a room has reached its maximum allowed connections
	 */
	private boolean hasReachedConnectionLimit(RoomNode room) {
		return connectionCount(room) >= getMaxConnections(room);
	}

	private int connectionCount(RoomNode room) {
		int count = 0;
		if (room.northConnection != null) count++;
		if (room.eastConnection != null) count++;
		if (room.southConnection != null) count++;
		if (room.westConnection != null) count++;
		return count;
	}

	private Direction getOtherDir(Direction currDir, RoomNode room) {
		if (room.eastAllowed && currDir != Direction.EAST) {
			return Direction.EAST;
		} else if (room.southAllowed && currDir != Direction.SOUTH) {
			return Direction.SOUTH;
		} else if (room.westAllowed && currDir != Direction.WEST) {
			return Direction.WEST;
		} else if (room.northAllowed && currDir != Direction.NORTH) {
			return Direction.NORTH;
		} else return null;
	}

	private boolean dirAllowed(RoomNode room, Direction dir) {
		if (dir == Direction.NORTH && room.northAllowed) return true;
		else if (dir == Direction.EAST && room.eastAllowed) return true;
		else if (dir == Direction.SOUTH && room.northAllowed) return true;
		else if (dir == Direction.WEST && room.westAllowed) return true;

		return false;
	}

	private int availableConnections1x1(RoomNode room) {
		int available = 0;

		if (room.northConnection == null && !rooms.containsKey(Pair.of(room.gridX, room.gridZ - 1))) available++;
		if (room.eastConnection == null && !rooms.containsKey(Pair.of(room.gridX + 1, room.gridZ))) available++;
		if (room.southConnection == null && !rooms.containsKey(Pair.of(room.gridX, room.gridZ + 1))) available++;
		if (room.westConnection == null && !rooms.containsKey(Pair.of(room.gridX - 1, room.gridZ))) available++;

		return available;
	}

}
