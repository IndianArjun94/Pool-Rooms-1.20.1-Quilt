package net.arjun.pool.worldgen;

import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoomNode {
	public int x, z;
	public List<RoomNode> connections;
	public String structureId;

	public RoomNode(int x, int z) {
		this.x = x;
		this.z = z;
		this.connections = new ArrayList<>();
	}

	public static String chooseStructure(RoomNode room) {
		List<Direction> dirs = new ArrayList<>();

		for (RoomNode neighbor : room.connections) {
			int dx = neighbor.x - room.x;
			int dz = neighbor.z - room.z;

			if (dx == 1) dirs.add(Direction.EAST);
			else if (dx == -1) dirs.add(Direction.WEST);
			else if (dz == 1) dirs.add(Direction.SOUTH);
			else if (dz == -1) dirs.add(Direction.NORTH);
		}

		dirs.sort(Comparator.comparing(Enum::ordinal)); // stable ordering
		int c = dirs.size();

		if (c == 1) { // dead end (1 connection)
			Direction d = dirs.get(0);
			if (d == Direction.NORTH) return "1x1_dead_end_south";
			if (d == Direction.EAST) return "1x1_dead_end_west";
			if (d == Direction.SOUTH) return "1x1_dead_end_north";
			if (d == Direction.WEST) return "1x1_dead_end_east";
		} else if (c == 2) { // straight or turn (2 connections)
			Direction a = dirs.get(0);
			Direction b = dirs.get(1);

			// straight?
			if ((a == Direction.NORTH && b == Direction.SOUTH) ||
				(a == Direction.SOUTH && b == Direction.NORTH)) {
				return "1x1_straight_north_south";
			}

			if ((a == Direction.EAST && b == Direction.WEST) ||
				(a == Direction.WEST && b == Direction.EAST)) {
				return "1x1_straight_east_west";
			}

			// turn
			return "1x1_turn_" + dirName(a) + "_" + dirName(b);
		} else if (c == 3) { // "T" pattern (3 connections) [returns the top of "T" direction]
			if (!dirs.contains(Direction.NORTH)) return "1x1_t_north";
			if (!dirs.contains(Direction.SOUTH)) return "1x1_t_south";
			if (!dirs.contains(Direction.WEST)) return "1x1_t_west";
			return "1x1_t_east";
		} else if (c == 4) { // 4 way (4 connections)
			return "1x1_four_way";
		}

		return null;

//		return "1x1_north_south";
	}

	private static String dirName(Direction d) {
		if (d == Direction.NORTH) {
			return "north";
		} else if (d == Direction.EAST) {
			return "east";
		} else if (d == Direction.SOUTH) {
			return "south";
		} else {
			return "west";
		}
	}
}
