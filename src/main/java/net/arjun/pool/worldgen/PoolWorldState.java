package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;

import java.util.*;

public class PoolWorldState extends PersistentState {
	public long seed;

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

		RoomNode start = new RoomNode(0,0);
		rooms.put(Pair.of(0,0), start);
		frontier.add(start);

		int roomsToGenerate = 2000;

		while (!frontier.isEmpty() && rooms.size() < roomsToGenerate) {
			RoomNode room = frontier.poll(); // gets first element and removes it

			int branches = 1+random.nextInt(3); // 1-3 branches per room

			if (room == start) {
				branches = 4;
			}

			List<Direction> dirs = shuffledDirections(random.nextLong());

			for (int i = 0; i < branches; i++) {
				Direction dir = dirs.get(i);
				int nx = room.x + dirX(dir);
				int nz = room.z + dirZ(dir);

				Pair<Integer,Integer> key = Pair.of(nx,nz);

				if (!rooms.containsKey(key)) { // we are on an empty room
					RoomNode newRoom = new RoomNode(nx,nz);
					rooms.put(key, newRoom);

					room.connections.add(newRoom);
					newRoom.connections.add(room);

					frontier.add(newRoom);
				} else { // we are on an already-generated room
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
