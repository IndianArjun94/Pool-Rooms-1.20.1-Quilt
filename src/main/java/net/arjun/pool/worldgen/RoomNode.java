package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import net.minecraft.util.math.Direction;

public class RoomNode {
	public int gridX, gridZ; // on the [8x8 per tile] grid of rooms
	public int gridLengthX, gridLengthZ; // how long each side is where [1 = 8 blocks]

	public RoomSize roomSize;

	public RoomNode northConnection = null; // neighboring rooms
	public RoomNode eastConnection = null;
	public RoomNode southConnection = null;
	public RoomNode westConnection = null;

	public boolean northAllowed = true;
	public boolean eastAllowed = true;
	public boolean southAllowed = true;
	public boolean westAllowed = true;

	public Pair<Integer,Integer> northConnectionPosition; // where the neighboring rooms are
	public Pair<Integer,Integer> eastConnectionPosition; // these go by chunks
	public Pair<Integer,Integer> southConnectionPosition; // a 2x2 room may want an entrance on the left or right side of the wall
	public Pair<Integer,Integer> westConnectionPosition; // so that room would have something other than 0 here

	public RoomNode(int gridX, int gridZ, int gridLengthX, int gridLengthZ, RoomSize roomSize) {
		this.roomSize = roomSize;

		this.gridX = gridX;
		this.gridZ = gridZ;

		this.gridLengthX = gridLengthX;
		this.gridLengthZ = gridLengthZ;
	}

//	public static String chooseStructure(RoomNode room) {
//		List<Direction> dirs = new ArrayList<>();
//
//		if (room.roomSize == RoomSize.R1x1) {
//			for (RoomNode neighbor : room.connections) {
//				int dx = neighbor.x - room.x;
//				int dz = neighbor.z - room.z;
//
//				if (dx >= 1) dirs.add(Direction.EAST);
//				else if (dx <= -1) dirs.add(Direction.WEST);
//				else if (dz >= 1) dirs.add(Direction.SOUTH);
//				else if (dz <= -1) dirs.add(Direction.NORTH);
//			}
//			dirs.sort(Comparator.comparing(Enum::ordinal)); // stable ordering
//			int c = dirs.size();
//
//			if (c == 1) { // dead end (1 connection)
//				Direction d = dirs.get(0);
//				if (d == Direction.NORTH) return "1x1_dead_end_south";
//				if (d == Direction.EAST) return "1x1_dead_end_west";
//				if (d == Direction.SOUTH) return "1x1_dead_end_north";
//				if (d == Direction.WEST) return "1x1_dead_end_east";
//			} else if (c == 2) { // straight or turn (2 connections)
//				Direction a = dirs.get(0);
//				Direction b = dirs.get(1);
//
//				// straight?
//				if ((a == Direction.NORTH && b == Direction.SOUTH) ||
//					(a == Direction.SOUTH && b == Direction.NORTH)) {
//					return "1x1_straight_north_south";
//				}
//
//				if ((a == Direction.EAST && b == Direction.WEST) ||
//					(a == Direction.WEST && b == Direction.EAST)) {
//					return "1x1_straight_east_west";
//				}
//
//				// turn
//				return "1x1_turn_" + dirName(a) + "_" + dirName(b);
//			} else if (c == 3) { // "T" pattern (3 connections) [returns the top of "T" direction]
//				if (!dirs.contains(Direction.NORTH)) return "1x1_t_north";
//				if (!dirs.contains(Direction.SOUTH)) return "1x1_t_south";
//				if (!dirs.contains(Direction.WEST)) return "1x1_t_west";
//				return "1x1_t_east";
//			} else if (c == 4) { // 4 way (4 connections)
//				return "1x1_four_way";
//			}
//		} else if (room.roomSize == RoomSize.R2x2) {
//			for (RoomNode neighbor : room.connections) {
//				int dx = neighbor.x - room.x;
//				int dz = neighbor.z - room.z;
//
//				if (dx >= 1) dirs.add(Direction.EAST);
//				else if (dx <= -1) dirs.add(Direction.WEST);
//				else if (dz >= 1) dirs.add(Direction.SOUTH);
//				else if (dz <= -1) dirs.add(Direction.NORTH);
//			}
//			dirs.sort(Comparator.comparing(Enum::ordinal)); // stable ordering
//			int c = dirs.size();
//
//			if (c == 1) { // dead end (1 connection)
//				Direction d = dirs.get(0);
//				if (d == Direction.NORTH) return "2x2_dead_end_south"; // name dead end room as the direction the dead end faces
//				if (d == Direction.EAST) return "2x2_dead_end_west";
//				if (d == Direction.SOUTH) return "2x2_dead_end_north";
//				if (d == Direction.WEST) return "2x2_dead_end_east";
//			} else if (c == 2) { // straight or turn (2 connections)
//				Direction a = dirs.get(0);
//				Direction b = dirs.get(1);
//
//				// straight?
//				if ((a == Direction.NORTH && b == Direction.SOUTH) ||
//					(a == Direction.SOUTH && b == Direction.NORTH)) {
//					return "2x2_straight_north_south";
//				}
//
//				if ((a == Direction.EAST && b == Direction.WEST) ||
//					(a == Direction.WEST && b == Direction.EAST)) {
//					return "2x2_straight_east_west";
//				}
//
//				// turn
//				return "2x2_turn_" + dirName(a) + "_" + dirName(b);
//			} else if (c == 3) { // "T" pattern (3 connections) [returns the top of "T" direction]
//				if (!dirs.contains(Direction.NORTH)) return "2x2_t_north";
//				if (!dirs.contains(Direction.SOUTH)) return "2x2_t_south";
//				if (!dirs.contains(Direction.WEST)) return "2x2_t_west";
//				return "2x2_t_east";
//			} else if (c == 4) { // 4 way (4 connections)
//				return "2x2_four_way";
//			}
//		}
//
//
//		return null;
//
////		return "1x1_north_south";
//	} // TODO update this

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
