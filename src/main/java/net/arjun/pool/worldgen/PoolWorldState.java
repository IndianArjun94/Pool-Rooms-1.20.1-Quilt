package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import net.arjun.pool.PoolRooms;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PoolWorldState extends PersistentState {
	public long seed;
	public static PoolWorldState instance;
	public List<Vec3d> playerPoses;

	private PoolWorldState() {
		this.seed = new Random().nextLong(); // new world, random seed
		instance = this;
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
		return state;
	}

	public Map<Pair<Integer,Integer>,RoomNode> generateNewMap() {

		final int roomsToGenerate = 10000;
		int roomsCount = 0;
		int counter = 1;

		Random random = new Random();

		random.setSeed(seed);

		Map<Pair<Integer,Integer>,RoomNode> rooms = new HashMap<>(); // all rooms and their positions
		Deque<RoomNode> frontier = new LinkedList<>(); // rooms that need to generate entrances

//		for (Vec3d pos : playerPoses) {
//			System.out.println("PWS: iterating through this player!");
//			Pair<Integer,Integer> key = Pair.of((int)pos.x/8, (int)pos.z/8);
//			if (PoolRooms.currentMap != null) {
//				if (PoolRooms.currentMap.containsKey(key)) {
//					if (PoolRooms.currentMap.get(key).roomSize == RoomSize.R1x1) {
//						RoomNode room = new RoomNode(key.first, key.second, 1, 1, RoomSize.R1x1);
//						room.northConnection = null;
//						room.eastConnection = null;
//						room.southConnection = null;
//						room.westConnection = null;
//						room.northConnectionPosition = Pair.of(room.gridX, room.gridZ);
//						room.westConnectionPosition = Pair.of(room.gridX, room.gridZ);
//						room.eastConnectionPosition = Pair.of(room.gridX+room.gridLengthX-1, room.gridZ);
//						room.southConnectionPosition = Pair.of(room.gridX, room.gridZ+room.gridLengthZ-1);
//						room.generationDirection = Direction.NORTH;
//						rooms.put(key, room);
//						frontier.add(room);
//					} else {
//						return PoolRooms.currentMap;
//					}
//				} else {
//					return PoolRooms.currentMap;
//				}
//			} // does starting room gen
//		}

		RoomNode start = new RoomNode(-1, -1, 3, 3, RoomSize.START, null);

		start.northConnectionPosition = Pair.of(start.gridX + 1, start.gridZ);
		start.westConnectionPosition = Pair.of(start.gridX, start.gridZ + 1);
		start.eastConnectionPosition = Pair.of(start.gridX + start.gridLengthX - 1, start.gridZ + 1);
		start.southConnectionPosition = Pair.of(start.gridX + 1, start.gridZ + start.gridLengthZ - 1);
		frontier.add(start);
		roomsCount++;


//		CODE TO ADD A ROOM ------------------------------------------
		for (int x = start.gridX; x < start.gridX + start.gridLengthX; x++) {
			for (int z = start.gridZ; z < start.gridZ + start.gridLengthZ; z++) {
				rooms.put(Pair.of(x, z), start);
			}
		}

//		-------------------------------------------------------------

		while (!frontier.isEmpty() && roomsCount < roomsToGenerate) {
			RoomNode currentRoom = frontier.poll(); // we need to generate more rooms from this room (branches)
			int branchesToGenerate = random.nextInt(0,4)+1; // generate 1-2 branches per room
			if (branchesToGenerate <= 3) branchesToGenerate = 1; else branchesToGenerate = 2;
			if (currentRoom == start) branchesToGenerate = 4; // if this is the starting room, generate 4 branches
			List<Direction> dirs = shuffledDirections(random.nextLong(), oppositeOf(currentRoom.generationDirection));

//			Loop through all directions
			for (int i = 0; i < branchesToGenerate; i++) {
				Direction dir = dirs.get(i);

//				If we are not allowed to go in a direction from currentRoom, continue
				if (dir == Direction.NORTH && !currentRoom.northAllowed) continue;
				if (dir == Direction.EAST && !currentRoom.eastAllowed) continue;
				if (dir == Direction.SOUTH && !currentRoom.southAllowed) continue;
				if (dir == Direction.WEST && !currentRoom.westAllowed) continue;

				int nx = currentRoom.gridX;
				int nz = currentRoom.gridZ;

				boolean chosenDir = false;

				for (int j = 0; j < 4 && !chosenDir; j++) {
					switch (dir) {
						case NORTH:
							nx = currentRoom.northConnectionPosition.first;
							nz = currentRoom.northConnectionPosition.second - 1;
							break;
						case SOUTH:
							nx = currentRoom.southConnectionPosition.first; // Move past the bottom edge
							nz = currentRoom.southConnectionPosition.second + 1;
							break;
						case WEST:
							nx = currentRoom.westConnectionPosition.first - 1;
							nz = currentRoom.westConnectionPosition.second;
							break;
						case EAST:
							nx = currentRoom.eastConnectionPosition.first + 1;
							nz = currentRoom.eastConnectionPosition.second;
							break;
					}

					if (rooms.containsKey(Pair.of(nx, nz))) { // if we stumbled upon a room
						if (hasReachedConnectionLimit(rooms.get(Pair.of(nx, nz))) || !dirAllowed(rooms.get(Pair.of(nx, nz)), dir)) { // if this room has maxConnections already
							if (getOtherDir(dir, currentRoom) != null) { // if there are other directions we can go to
								dir = getOtherDir(dir, currentRoom);
								continue;
							} else {
								break;
							}
						}
					}

					chosenDir = true;
				}

				if (!chosenDir) continue;

				Pair<Integer,Integer> key = Pair.of(nx,nz);

				if (!rooms.containsKey(key)) { // we can generate a new room here

					boolean createdRoom = false;
					boolean failed = false;

					RoomNode newRoom = null;

//					2x2
					if (counter % 7 == 0 && !checkProximity(rooms, Pair.of(nx,nz), 10, RoomSize.R2x2)) { // 2x2 room
						newRoom = new RoomNode(dir, nx, nz, RoomSize.R2x2);
						newRoom.createRoomCoords();

						if (check2x2SpaceAvailable(rooms, newRoom)) {
							roomsCount++;
							createdRoom = true;
						}
					}

//					1x2
					if (!createdRoom && counter % 15 == 0 && !checkProximity(rooms, Pair.of(nx,nz), 10, RoomSize.R1x2)) {
						newRoom = new RoomNode(dir, nx, nz, RoomSize.R1x2);
						newRoom.createRoomCoords();

						if (check1x2SpaceAvailable(rooms, newRoom)) {
							roomsCount++;
							createdRoom = true;
						}
					}

//					1x1
					if (!createdRoom) {
						newRoom = new RoomNode(nx,nz,1,1,RoomSize.R1x1, dir);
					}

					newRoom.createConnectionPositions();

					if (dir == Direction.NORTH) { // set connections with CURRENT <-> PREVIOUS
						currentRoom.northConnection = newRoom;
						newRoom.southConnection = currentRoom;
					} else if (dir == Direction.EAST) {
						currentRoom.eastConnection = newRoom;
						newRoom.westConnection = currentRoom;
					} else if (dir == Direction.SOUTH) {
						currentRoom.southConnection = newRoom;
						newRoom.northConnection = currentRoom;
					} else if (dir == Direction.WEST) {
						currentRoom.westConnection = newRoom;
						newRoom.eastConnection = currentRoom;
					}

//					Placing room on the map
					for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
						for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
							rooms.put(Pair.of(x, z), newRoom);
						}
					}

//					Adding room to frontier and finalization
					frontier.add(newRoom);

				} else {
					RoomNode existingRoom = rooms.get(key);

// Check if we can add more connections to the existing room
					if (hasReachedConnectionLimit(existingRoom)) continue;

					// Check if current room can accept more connections
					if (hasReachedConnectionLimit(currentRoom)) continue;

					if (dir == Direction.NORTH && existingRoom.southAllowed) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.northConnectionPosition)) {
							currentRoom.northConnection = existingRoom;
							existingRoom.southConnection = currentRoom;
						}
					} else if (dir == Direction.EAST && existingRoom.westAllowed) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.eastConnectionPosition)) {
							currentRoom.eastConnection = existingRoom;
							existingRoom.westConnection = currentRoom;
						}
					} else if (dir == Direction.SOUTH && existingRoom.northAllowed) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.southConnectionPosition)) {
							currentRoom.southConnection = existingRoom;
							existingRoom.northConnection = currentRoom;
						}
					} else if (dir == Direction.WEST && existingRoom.eastAllowed) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.westConnectionPosition)) {
							currentRoom.westConnection = existingRoom;
							existingRoom.eastConnection = currentRoom;
						}
					}
				}
			}

			counter++;
		}

//		Room IDs
		for (RoomNode room : rooms.values()) {
			room.structureId = RoomNode.chooseStructure(room);
		}

//		Saving the new map
		PoolRooms.currentMap = rooms;

		System.out.println("PWS: finished generating");

		markDirty();

		return rooms;

	}

//	Helpers -----------

	private boolean check2x2SpaceAvailable(Map<Pair<Integer,Integer>, RoomNode> map, int gx, int gz) {
		if (map.containsKey(Pair.of(gx,gz))) return false;
		if (map.containsKey(Pair.of(gx+1,gz))) return false;
		if (map.containsKey(Pair.of(gx,gz+1))) return false;
		if (map.containsKey(Pair.of(gx+1,gz+1))) return false;

		return true;
	}
	private boolean check2x2SpaceAvailable(Map<Pair<Integer,Integer>, RoomNode> map, RoomNode room) {
		if (map.containsKey(Pair.of(room.gridX,room.gridZ))) return false;
		if (map.containsKey(Pair.of(room.gridX+room.gridLengthX-1,room.gridZ))) return false;
		if (map.containsKey(Pair.of(room.gridX,room.gridZ+room.gridLengthZ))) return false;
		if (map.containsKey(Pair.of(room.gridX+room.gridLengthX-1,room.gridZ+room.gridLengthZ))) return false;

		return true;
	}
	private boolean check1x2SpaceAvailable(Map<Pair<Integer,Integer>, RoomNode> map, int gx, int gz, int lengthX, int lengthZ) {
		if (map.containsKey(Pair.of(gx,gz))) return false;
		if (map.containsKey(Pair.of(gx+lengthX-1,gz+lengthZ-1))) return false;

		return true;
	}

	private boolean check1x2SpaceAvailable(Map<Pair<Integer,Integer>, RoomNode> map, RoomNode room) {
		if (map.containsKey(Pair.of(room.gridX,room.gridZ))) return false;
		if (map.containsKey(Pair.of(room.gridX+room.gridLengthX-1,room.gridZ+room.gridLengthZ-1))) return false;

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
	private boolean checkProximity(Map<Pair<Integer,Integer>, RoomNode> map, Pair<Integer,Integer> coord, int radius, RoomSize type) {
		final int detail = 10; // the lower, the more accurate

		int originalX = coord.first;
		int originalZ = coord.second;

		for (int degrees = 0; degrees < 360; degrees+=detail) {
			double aX = originalX; // actual x
			double aZ = originalZ; // actual z

			double xInterval = Math.sin(Math.toRadians(degrees));
			double zInterval = -Math.cos(Math.toRadians(degrees));

			for (int i = 0; i < radius; i++) {
				aX += xInterval;
				aZ += zInterval;

				if (map.containsKey(Pair.of((int)(aX), (int)(aZ)))) {
					if (map.get(Pair.of((int)(aX), (int)(aZ))).roomSize == type) {
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
		if (room.northConnection!=null) count++;
		if (room.eastConnection!=null) count++;
		if (room.southConnection!=null) count++;
		if (room.westConnection!=null) count++;
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
}
