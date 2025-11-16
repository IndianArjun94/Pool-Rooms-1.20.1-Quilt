package net.arjun.pool.worldgen.conceptdev;

import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class PoolRoom {
    public RoomType type;

    public final int x;
    public final int x2;
    public final int y;
    public final int y2;

    public PoolRoom NORTH;
    public PoolRoom NORTH2;
    public PoolRoom NORTH3;
    public PoolRoom SOUTH;
    public PoolRoom SOUTH2;
    public PoolRoom SOUTH3;
    public PoolRoom EAST;
    public PoolRoom EAST2;
    public PoolRoom EAST3;
    public PoolRoom WEST;
    public PoolRoom WEST2;
    public PoolRoom WEST3;

	public PoolGenMove facing;

	public RoomPosition ENTRANCE1;
	public RoomPosition ENTRANCE2;

	private boolean isFourWayConnectorRoom = false;
	private boolean isThreeWayConnectorRoom = false;

	private boolean isDeadEnd = false;

    public PoolRoom(RoomType type, int x, int y, int x2, int y2) {
        this.type = type;
        this.x = x;
        this.x2 = x2;
        this.y = y;
        this.y2 = y2;
    }

	public void makeFourWayConnectorRoom() {
		this.isFourWayConnectorRoom = true;
		NORTH = null;
		NORTH2 = null;
		NORTH3 = null;
		EAST = null;
		EAST2 = null;
		EAST3 = null;
		SOUTH = null;
		SOUTH2 = null;
		SOUTH3 = null;
		WEST = null;
		WEST2 = null;
		WEST3 = null;
		ENTRANCE1 = null;
		ENTRANCE2 = null;
		this.type = RoomType.R1x1_FOUR_WAY_CONNECTOR;
	}

	public void makeThreeWayConnectorRoom(PoolGenMove facing) {
		this.isThreeWayConnectorRoom = true;
		this.facing = facing;
		NORTH = null;
		NORTH2 = null;
		NORTH3 = null;
		EAST = null;
		EAST2 = null;
		EAST3 = null;
		SOUTH = null;
		SOUTH2 = null;
		SOUTH3 = null;
		WEST = null;
		WEST2 = null;
		WEST3 = null;
		ENTRANCE1 = null;
		ENTRANCE2 = null;
		this.type = RoomType.R1x1_THREE_WAY_CONNECTOR;
	}

	public void makeDeadEnd(PoolGenMove facing) {
		this.isThreeWayConnectorRoom = true;
		this.facing = facing;
		NORTH = null;
		NORTH2 = null;
		NORTH3 = null;
		EAST = null;
		EAST2 = null;
		EAST3 = null;
		SOUTH = null;
		SOUTH2 = null;
		SOUTH3 = null;
		WEST = null;
		WEST2 = null;
		WEST3 = null;
		ENTRANCE1 = null;
		ENTRANCE2 = null;
		this.type = RoomType.DEAD_END;
	}

	public boolean isFourWayConnectorRoom() {
		return this.isFourWayConnectorRoom;
	}

	public boolean isThreeWayConnectorRoom() {
		return this.isThreeWayConnectorRoom;
	}
}
