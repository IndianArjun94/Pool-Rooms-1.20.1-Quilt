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

	public Map<Pair<Integer, Integer>, RoomNode> generateRooms(long seed) {
		Random random = new Random(seed);

		Map<Pair<Integer, Integer>, RoomNode> rooms = new HashMap<>();
		Queue<RoomNode> frontier = new LinkedList<>();

		RoomNode start = new RoomNode(2,0);
		RoomNode start2 = new RoomNode(-2,0);
		RoomNode start3 = new RoomNode(0,2);
		RoomNode start4 = new RoomNode(0,-2);
		rooms.put(Pair.of(2,0), start);
		rooms.put(Pair.of(-2,0), start2);
		rooms.put(Pair.of(0,2), start3);
		rooms.put(Pair.of(0,-2), start4);
		frontier.add(start);
		frontier.add(start2);
		frontier.add(start3);
		frontier.add(start4);

		int roomsToGenerate = 250;

		while (!frontier.isEmpty() && rooms.size() < roomsToGenerate) {
			RoomNode room = frontier.poll(); // gets first element and removes it

			int branches = 1+random.nextInt(3); // 1-3 branches per room

			List<Direction> dirs = shuffledDirections(random.nextLong());

			if (room == start || room == start2 || room == start3 || room == start4) {
				branches = 1;
			}

			for (int i = 0; i < branches; i++) {
				Direction dir = dirs.get(i);
				if (room == start) {
					dir = Direction.EAST;
					room.connections.add(new RoomNode(1,0));
				} else if (room == start2) {
					dir = Direction.WEST;
					room.connections.add(new RoomNode(-1,0));
				} else if (room == start3) {
					dir = Direction.SOUTH;
					room.connections.add(new RoomNode(0,1));
				} else if (room == start4) {
					dir = Direction.NORTH;
					room.connections.add(new RoomNode(0,-1));
				}

				int nx = room.x + dirX(dir);
				int nz = room.z + dirZ(dir);

				Pair<Integer,Integer> key = Pair.of(nx,nz);

				if (!rooms.containsKey(key) && !onStartingRoom(key)) { // we are on an empty, non-starting room
					RoomNode newRoom = new RoomNode(nx,nz);
					rooms.put(key, newRoom);

					room.connections.add(newRoom);
					newRoom.connections.add(room);

					frontier.add(newRoom);
				} else if (rooms.containsKey(key)){ // we are on an already-generated non-starting room
					RoomNode currentRoom = rooms.get(key);
					if (!room.connections.contains(currentRoom)) {
						room.connections.add(currentRoom);
						currentRoom.connections.add(room);
					}
				}
			}
		}

		for (RoomNode room : rooms.values()) {
			room.structureId = RoomNode.chooseStructure(room);
		}

		return rooms;
	}

//	Helpers -----------

	private boolean onStartingRoom(Pair<Integer,Integer> key) {
		return key.first >= -1 && key.first <= 1 &&
			key.second >= -1 && key.second <= 1;
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
