package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PoolWorldState extends PersistentState {
	public long seed;

	public static ArrayList<Map<Pair<Integer,Integer>, RoomNode>> roomMaps;
	public static ArrayList<Long> seeds;

	private PoolWorldState() {
		this.seed = new Random().nextLong(); // new world, random seed
	}

	public static PoolWorldState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(
			PoolWorldState::fromNbt,
			PoolWorldState::new,
			"pool_world_state"
		);
	}

	private static PoolWorldState fromNbt(NbtCompound nbt) {
		PoolWorldState state = new PoolWorldState();
		state.seed = nbt.getLong("seed");
		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putLong("seed", seed);
		return nbt;
	}

	public Map<Pair<Integer,Integer>,RoomNode> generateRooms(long seed) {
		final int roomsToGenerate = 20000;

		Random random = new Random(seed);

		Map<Pair<Integer,Integer>,RoomNode> rooms = new HashMap<>(); // all rooms and their positions
		Queue<RoomNode> frontier = new LinkedList<>(); // rooms that need to generate entrances

		RoomNode start = new RoomNode(-1,-1,3,3,RoomSize.START);
		start.northConnectionPosition = Pair.of(start.gridX+1, start.gridZ);
		start.westConnectionPosition = Pair.of(start.gridX, start.gridZ+1);
		start.eastConnectionPosition = Pair.of(start.gridX+start.gridLengthX-1, start.gridZ+1);
		start.southConnectionPosition = Pair.of(start.gridX+1, start.gridZ+start.gridLengthZ-1);
		frontier.add(start);

//		CODE TO ADD A ROOM ------------------------------------------
		for (int x = start.gridX; x < start.gridX + start.gridLengthX; x++) {
			for (int z = start.gridZ; z < start.gridZ + start.gridLengthZ; z++) {
				rooms.put(Pair.of(x,z), start);
			}
		}
//		-------------------------------------------------------------

		while (!frontier.isEmpty() && rooms.size() < roomsToGenerate) {
			RoomNode currentRoom = frontier.poll(); // we need to generate more rooms from this room (branches)
			int branchesToGenerate = random.nextInt(0,3)+1; // generate 1-3 branches per room
			if (currentRoom == start) branchesToGenerate = 4; // if this is the starting room, generate 4 branches
			List<Direction> dirs = shuffledDirections(random.nextLong());

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
				switch (dir) {
					case NORTH:
						nx = currentRoom.northConnectionPosition.first;
						nz = currentRoom.northConnectionPosition.second-1;
						break;
					case SOUTH:
						nx = currentRoom.southConnectionPosition.first; // Move past the bottom edge
						nz = currentRoom.southConnectionPosition.second+1;
						break;
					case WEST:
						nx = currentRoom.westConnectionPosition.first-1;
						nz = currentRoom.westConnectionPosition.second;
						break;
					case EAST:
						nx = currentRoom.eastConnectionPosition.first+1;
						nz = currentRoom.eastConnectionPosition.second;
						break;
				}

				Pair<Integer,Integer> key = Pair.of(nx,nz);

				if (!rooms.containsKey(key)) { // we can generate a new room here
					int roomType = random.nextInt(0,20);

					if (roomType != 19) { // 0, 1, 2, or 3 forms a 1x1 room
						RoomNode newRoom = new RoomNode(nx,nz,1,1,RoomSize.R1x1);
						newRoom.northConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ);
						newRoom.westConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ);
						newRoom.eastConnectionPosition = Pair.of(newRoom.gridX+newRoom.gridLengthX-1, newRoom.gridZ);
						newRoom.southConnectionPosition = Pair.of(newRoom.gridX, newRoom.gridZ+newRoom.gridLengthZ-1);

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
					} else { // 4 makes a 2x2 room
						if (dir == Direction.SOUTH) {
							int gridX = nx;
							int gridZ = nz;

							int lengthX = 2;
							int lengthZ = 2;

							if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

							RoomNode newRoom = new RoomNode(gridX,gridZ,lengthX,lengthZ,RoomSize.R2x2);

							newRoom.eastAllowed = false;
							newRoom.westAllowed = false;

							newRoom.southConnectionPosition = Pair.of(gridX, gridZ+1);
							newRoom.northConnectionPosition = Pair.of(gridX, gridZ);

							for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
								for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
									rooms.put(Pair.of(x,z), newRoom);
								}
							}
							frontier.add(newRoom);

							currentRoom.southConnection = newRoom;
							newRoom.northConnection = currentRoom;
						} else if (dir == Direction.EAST) {
							int gridX = nx;
							int gridZ = nz-1;

							int lengthX = 2;
							int lengthZ = 2;

							if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

							RoomNode newRoom = new RoomNode(gridX,gridZ,lengthX,lengthZ,RoomSize.R2x2);

							newRoom.northAllowed = false;
							newRoom.southAllowed = false;

							newRoom.westConnectionPosition = Pair.of(gridX, gridZ+1);
							newRoom.eastConnectionPosition = Pair.of(gridX+1, gridZ+1);

							for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
								for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
									rooms.put(Pair.of(x,z), newRoom);
								}
							}
							frontier.add(newRoom);

							currentRoom.eastConnection = newRoom;
							newRoom.westConnection = currentRoom;
						} else if (dir == Direction.NORTH) {
							int gridX = nx-1;
							int gridZ = nz-1;

							int lengthX = 2;
							int lengthZ = 2;

							if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

							RoomNode newRoom = new RoomNode(gridX,gridZ,lengthX,lengthZ,RoomSize.R2x2);

							newRoom.eastAllowed = false;
							newRoom.westAllowed = false;

							newRoom.northConnectionPosition = Pair.of(gridX+1, gridZ);
							newRoom.southConnectionPosition = Pair.of(gridX+1, gridZ+1);

							for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
								for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
									rooms.put(Pair.of(x,z), newRoom);
								}
							}
							frontier.add(newRoom);

							currentRoom.northConnection = newRoom;
							newRoom.southConnection = currentRoom;
						} else if (dir == Direction.WEST) {
							int gridX = nx-1;
							int gridZ = nz;

							int lengthX = 2;
							int lengthZ = 2;

							if (!check2x2SpaceAvailable(rooms, gridX, gridZ)) continue;

							RoomNode newRoom = new RoomNode(gridX,gridZ,lengthX,lengthZ,RoomSize.R2x2);

							newRoom.northAllowed = false;
							newRoom.southAllowed = false;

							newRoom.eastConnectionPosition = Pair.of(gridX+1, gridZ);
							newRoom.westConnectionPosition = Pair.of(gridX, gridZ);

							for (int x = newRoom.gridX; x < newRoom.gridX + newRoom.gridLengthX; x++) {
								for (int z = newRoom.gridZ; z < newRoom.gridZ + newRoom.gridLengthZ; z++) {
									rooms.put(Pair.of(x,z), newRoom);
								}
							}
							frontier.add(newRoom);
							currentRoom.westConnection = newRoom;
							newRoom.eastConnection = currentRoom;
						}
					}

				} else {
					RoomNode existingRoom = rooms.get(key);

					if (dir == Direction.NORTH && !existingRoom.northAllowed) continue;
					else if (dir == Direction.EAST && !existingRoom.eastAllowed) continue;
					else if (dir == Direction.SOUTH && !existingRoom.southAllowed) continue;
					else if (dir == Direction.WEST && !existingRoom.westAllowed) continue;

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
		}

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
	private List<Direction> shuffledDirections(long seed) {
		Random random = new Random(seed);
		List<Direction> dirs = new ArrayList<>(Arrays.asList(
			Direction.NORTH,
			Direction.EAST,
			Direction.SOUTH,
			Direction.WEST
		));
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
}
