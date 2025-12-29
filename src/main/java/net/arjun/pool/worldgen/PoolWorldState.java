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
	public boolean firstTime = true;
	public boolean pastFirstTime = false;
	public static PoolWorldState instance;
	public List<Vec3d> playerPoses;

	private PoolWorldState() {
		this.seed = new Random().nextLong(); // new world, random seed
		instance = this;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putBoolean("firstTime", firstTime);
		System.out.println("PWS: writing firstTime: " + nbt.getBoolean("firstTime"));
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
		state.firstTime = nbt.getBoolean("firstTime");
		System.out.println("PWS: reading firstTime: " + nbt.getBoolean("firstTime"));
		return state;
	}

	public Map<Pair<Integer,Integer>,RoomNode> generateNewMap() {

		System.out.println("PWS: generating rooms");

		if (!firstTime) pastFirstTime = true;

		final int roomsToGenerate = 2000;
		int roomsCount = 0;
		int counter = 1;

		Random random = new Random();

		if (firstTime) random.setSeed(seed);

		Map<Pair<Integer,Integer>,RoomNode> rooms = new HashMap<>(); // all rooms and their positions
		Deque<RoomNode> frontier = new LinkedList<>(); // rooms that need to generate entrances

		playerPoses = PoolRooms.getPlayerPositions();

		System.out.println("PWS: playerCount: " + playerPoses.size());

		for (Vec3d pos : playerPoses) {
			System.out.println("PWS: iterating through this player!");
			Pair<Integer,Integer> key = Pair.of((int)pos.x/8, (int)pos.z/8);
			if (PoolRooms.currentMap != null) {
				if (PoolRooms.currentMap.containsKey(key)) {
					if (PoolRooms.currentMap.get(key).roomSize == RoomSize.R1x1) {
						RoomNode room = new RoomNode(key.first, key.second, 1, 1, RoomSize.R1x1);
						room.northConnection = null;
						room.eastConnection = null;
						room.southConnection = null;
						room.westConnection = null;
						room.northConnectionPosition = Pair.of(room.gridX, room.gridZ);
						room.westConnectionPosition = Pair.of(room.gridX, room.gridZ);
						room.eastConnectionPosition = Pair.of(room.gridX+room.gridLengthX-1, room.gridZ);
						room.southConnectionPosition = Pair.of(room.gridX, room.gridZ+room.gridLengthZ-1);
						room.generationDirection = Direction.NORTH;
						rooms.put(key, room);
						frontier.add(room);
					} else {
						return PoolRooms.currentMap;
					}
				} else {
					return PoolRooms.currentMap;
				}
			} // does starting room gen
		}

		RoomNode start = new RoomNode(-1, -1, 3, 3, RoomSize.START);

		if (firstTime) {
			start.northConnectionPosition = Pair.of(start.gridX + 1, start.gridZ);
			start.westConnectionPosition = Pair.of(start.gridX, start.gridZ + 1);
			start.eastConnectionPosition = Pair.of(start.gridX + start.gridLengthX - 1, start.gridZ + 1);
			start.southConnectionPosition = Pair.of(start.gridX + 1, start.gridZ + start.gridLengthZ - 1);
			frontier.add(start);
			roomsCount++;
		}


//		CODE TO ADD A ROOM ------------------------------------------
		if (firstTime) {
			for (int x = start.gridX; x < start.gridX + start.gridLengthX; x++) {
				for (int z = start.gridZ; z < start.gridZ + start.gridLengthZ; z++) {
					rooms.put(Pair.of(x, z), start);
				}
			}
		}
//		-------------------------------------------------------------

		while (!frontier.isEmpty() && roomsCount < roomsToGenerate) {
			RoomNode currentRoom = frontier.poll(); // we need to generate more rooms from this room (branches)
			int branchesToGenerate = random.nextInt(0,2)+1; // generate 1-3 branches per room
			if (currentRoom == start) branchesToGenerate = 4; // if this is the starting room, generate 4 branches
			List<Direction> dirs = shuffledDirections(random.nextLong(), oppositeOf(currentRoom.generationDirection));

			for (int i = 0; i < branchesToGenerate; i++) {
				Direction dir = dirs.get(i);
				if (dir == Direction.NORTH) if (!currentRoom.northAllowed) continue;
				if (dir == Direction.EAST) if (!currentRoom.eastAllowed) continue;
				if (dir == Direction.SOUTH) if (!currentRoom.southAllowed) continue;
				if (dir == Direction.WEST) if (!currentRoom.westAllowed) continue;

				int nx = currentRoom.gridX;
				int nz = currentRoom.gridZ;

				// We assume the NEW room is 1x1.
				// If new rooms can be larger, you must account for newRoom.width in the subtraction logic.
				boolean choseDirDone = false;
				int iterations = 0;

				while (!choseDirDone) {
					switch (dir) {
						case NORTH:
							nx = currentRoom.northConnectionPosition.first;
							nz = currentRoom.northConnectionPosition.second - 1;
							if (rooms.containsKey(Pair.of(nx, nz))) {
								if (connectionCount(rooms.get(Pair.of(nx, nz))) == 3) {
									if (getOtherDir(dir, currentRoom) != null) dir = getOtherDir(dir,currentRoom); else continue;
									break;
								}
							}
							choseDirDone = true;
							break;
						case SOUTH:
							nx = currentRoom.southConnectionPosition.first; // Move past the bottom edge
							nz = currentRoom.southConnectionPosition.second + 1;
							if (rooms.containsKey(Pair.of(nx, nz))) {
								if (connectionCount(rooms.get(Pair.of(nx, nz))) == 3) {
									if (getOtherDir(dir, currentRoom) != null) dir = getOtherDir(dir,currentRoom); else continue;
									break;
								}
							}
							choseDirDone = true;
							break;
						case WEST:
							nx = currentRoom.westConnectionPosition.first - 1;
							nz = currentRoom.westConnectionPosition.second;
							if (rooms.containsKey(Pair.of(nx, nz))) {
								if (connectionCount(rooms.get(Pair.of(nx, nz))) == 3) {
									if (getOtherDir(dir, currentRoom) != null) dir = getOtherDir(dir,currentRoom); else continue;
									break;
								}
							}
							choseDirDone = true;
							break;
						case EAST:
							nx = currentRoom.eastConnectionPosition.first + 1;
							nz = currentRoom.eastConnectionPosition.second;
							if (rooms.containsKey(Pair.of(nx, nz))) {
								if (connectionCount(rooms.get(Pair.of(nx, nz))) == 3) {
									if (getOtherDir(dir, currentRoom) != null) dir = getOtherDir(dir,currentRoom); else continue;
									break;
								}
							}
							choseDirDone = true;
							break;
					}
					iterations++;
					if (iterations > 5) {
						break;
					}
				}

				if (!choseDirDone) continue;

				Pair<Integer,Integer> key = Pair.of(nx,nz);

				if (!rooms.containsKey(key)) { // we can generate a new room here
//					int roomType = random.nextInt(0,20);

					if (counter % 10 != 0) { // 0, 1, 2, or 3 forms a 1x1 room
						RoomNode newRoom = new RoomNode(nx,nz,1,1,RoomSize.R1x1);
						newRoom.northConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ);
						newRoom.westConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ);
						newRoom.eastConnectionPosition = Pair.of(newRoom.gridX+newRoom.gridLengthX-1, newRoom.gridZ);
						newRoom.southConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ+newRoom.gridLengthZ-1);
						newRoom.generationDirection = dir;

						if (dir == Direction.NORTH) {
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
						} // set the connections

						rooms.put(Pair.of(newRoom.gridX,newRoom.gridZ), newRoom);
						frontier.add(newRoom);
						roomsCount++;
					} else { // 4 makes a 2x2 room

						if (!checkProximity(rooms, Pair.of(nx,nz), 10, RoomSize.R2x2)) {

							if (dir == Direction.SOUTH) {
								int gridX = nx;
								int gridZ = nz;

								int lengthX = 2;
								int lengthZ = 2;

								if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

								RoomNode newRoom = new RoomNode(gridX, gridZ, lengthX, lengthZ, RoomSize.R2x2);

								newRoom.generationDirection = dir;

								newRoom.eastAllowed = false;
								newRoom.westAllowed = false;

								newRoom.southConnectionPosition = Pair.of(gridX, gridZ + 1);
								newRoom.northConnectionPosition = Pair.of(gridX, gridZ);

								for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
									for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
										rooms.put(Pair.of(x, z), newRoom);
									}
								}
								frontier.addFirst(newRoom);

								currentRoom.southConnection = newRoom;
								newRoom.northConnection = currentRoom;
							} else if (dir == Direction.EAST) {
								int gridX = nx;
								int gridZ = nz - 1;

								int lengthX = 2;
								int lengthZ = 2;

								if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

								RoomNode newRoom = new RoomNode(gridX, gridZ, lengthX, lengthZ, RoomSize.R2x2);

								newRoom.generationDirection = dir;

								newRoom.northAllowed = false;
								newRoom.southAllowed = false;

								newRoom.westConnectionPosition = Pair.of(gridX, gridZ + 1);
								newRoom.eastConnectionPosition = Pair.of(gridX + 1, gridZ + 1);

								for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
									for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
										rooms.put(Pair.of(x, z), newRoom);
									}
								}
								frontier.addFirst(newRoom);

								currentRoom.eastConnection = newRoom;
								newRoom.westConnection = currentRoom;
							} else if (dir == Direction.NORTH) {
								int gridX = nx - 1;
								int gridZ = nz - 1;

								int lengthX = 2;
								int lengthZ = 2;

								if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

								RoomNode newRoom = new RoomNode(gridX, gridZ, lengthX, lengthZ, RoomSize.R2x2);

								newRoom.generationDirection = dir;

								newRoom.eastAllowed = false;
								newRoom.westAllowed = false;

								newRoom.northConnectionPosition = Pair.of(gridX + 1, gridZ);
								newRoom.southConnectionPosition = Pair.of(gridX + 1, gridZ + 1);

								for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
									for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
										rooms.put(Pair.of(x, z), newRoom);
									}
								}
								frontier.addFirst(newRoom);

								currentRoom.northConnection = newRoom;
								newRoom.southConnection = currentRoom;
							} else if (dir == Direction.WEST) {
								int gridX = nx - 1;
								int gridZ = nz;

								int lengthX = 2;
								int lengthZ = 2;

								if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

								RoomNode newRoom = new RoomNode(gridX, gridZ, lengthX, lengthZ, RoomSize.R2x2);

								newRoom.generationDirection = dir;

								newRoom.northAllowed = false;
								newRoom.southAllowed = false;

								newRoom.eastConnectionPosition = Pair.of(gridX + 1, gridZ);
								newRoom.westConnectionPosition = Pair.of(gridX, gridZ);

								for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
									for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
										rooms.put(Pair.of(x, z), newRoom);
									}
								}
								frontier.addFirst(newRoom);
								currentRoom.westConnection = newRoom;
								newRoom.eastConnection = currentRoom;
							}

							roomsCount++;

						}
					}

				} else {
					RoomNode existingRoom = rooms.get(key);

					if (dir == Direction.NORTH && !existingRoom.northAllowed) continue;
					else if (dir == Direction.EAST && !existingRoom.eastAllowed) continue;
					else if (dir == Direction.SOUTH && !existingRoom.southAllowed) continue;
					else if (dir == Direction.WEST && !existingRoom.westAllowed) continue;

					if (connectionCount(existingRoom) >= 3) continue;
					if (connectionCount(currentRoom) >= 3) continue;

					if (dir == Direction.NORTH) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.northConnectionPosition)) {
							currentRoom.northConnection = existingRoom;
							existingRoom.southConnection = currentRoom;
						}
					} else if (dir == Direction.EAST) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.eastConnectionPosition)) {
							currentRoom.eastConnection = existingRoom;
							existingRoom.westConnection = currentRoom;
						}
					} else if (dir == Direction.SOUTH) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.southConnectionPosition)) {
							currentRoom.southConnection = existingRoom;
							existingRoom.northConnection = currentRoom;
						}
					} else if (dir == Direction.WEST) {
						if (Objects.equals(Pair.of(nx, nz), existingRoom.westConnectionPosition)) {
							currentRoom.westConnection = existingRoom;
							existingRoom.eastConnection = currentRoom;
						}
					} // set the connections
				}
			}

			counter++;
		}

		for (RoomNode room : rooms.values()) {
			room.structureId = RoomNode.chooseStructure(room);
		}

		PoolRooms.currentMap = rooms;

		System.out.println("PWS: finished generating");

		firstTime = false;

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
		}

		Collections.shuffle(dirs, random);
		return dirs;
	}
	private int dirX(Direction dir) {
		if (dir == Direction.NORTH || dir == Direction.SOUTH) {
			return 0;
		} else if (dir == Direction.EAST) {
			return 1;
		} else {
			return -1;
		}
	}
	private int dirZ(Direction dir) {
		if (dir == Direction.EAST || dir == Direction.WEST) {
			return 0;
		} else if (dir == Direction.NORTH) {
			return -1;
		} else {
			return 1;
		}
	}
	private Direction oppositeOf(Direction dir) {
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
}
