package net.arjun.pool.worldgen.conceptdev;

import java.util.Objects;

public class PoolRoomsWorldMap {
    public static int X_BOUND;
    public static int Y_BOUND;

    public PoolRoom[][] rooms;

    private int instances = 0;

    private static PoolRoomsWorldMap instance;

    public int slotsFilled = 0;

    public int roomsFilled = 0;

    public PoolRoomsWorldMap(int xbound, int ybound) {
        if (instances > 0) {
            System.err.println("Only 1 instance allowed of PoolRoomsWorldMap");
            System.exit(1);
        }

        X_BOUND = xbound;
        Y_BOUND = ybound;

        rooms = new PoolRoom[xbound][ybound];

        instance = this;

    }

    public static PoolRoomsWorldMap getInstance() {
        return instance;
    }

    private void updateRoom(PoolRoom room, int x, int y) {
		rooms[(Y_BOUND) - y][x - 1] = room;
		slotsFilled++;
//            System.out.println("Set: (" + x + "," + y + ")");
    }

    public RoomType getSpot(int x1, int y1) {
        if ((Y_BOUND)-y1 <= Y_BOUND-1 && (Y_BOUND)-y1 >= 0 && x1-1 <= X_BOUND-1 && x1-1 >= 0) {
            if (rooms[(Y_BOUND) - y1][x1 - 1] != null) {
                return rooms[(Y_BOUND) - y1][x1 - 1].type;
            } else {
                return null;
            }
        } else {
            return RoomType.OUT_OF_BOUNDS;
        }
    }

    public PoolRoom getRoom(int x1, int y1) {
        if ((Y_BOUND)-y1 <= Y_BOUND-1 && (Y_BOUND)-y1 >= 0 && x1-1 <= X_BOUND-1 && x1-1 >= 0) {
            if (rooms[(Y_BOUND) - y1][x1 - 1] != null) {
                return rooms[(Y_BOUND) - y1][x1 - 1];
            }
        }
        return null;
    }

    public void setRoom(RoomType roomType, int x1, int y1, int x2, int y2) {
        roomsFilled++;

        PoolRoom room = new PoolRoom(roomType, x1, y1, x2, y2);

        if (roomType == RoomType.R1x1 || roomType == RoomType.STARTING) {
            updateRoom(room, x1, y1);
        } else if (roomType == RoomType.R1x2) {
            updateRoom(room, x1, y1);
            updateRoom(room, x2, y2);
        } else if (roomType == RoomType.R1x3) {
            updateRoom(room, x1, y1);
            updateRoom(room, (x1+x2)/2, (y1+y2)/2);
            updateRoom(room, x2, y2);
        } else if (roomType == RoomType.R2x2) {
            updateRoom(room, x1, y1);
            updateRoom(room, x1, y2);
            updateRoom(room, x2, y1);
            updateRoom(room, x2, y2);
        }
    }

    public boolean spotIsAvailable(int x1, int y1) {
        return getSpot(x1, y1) == null;
    }

    public boolean roomIsAvailable(RoomType roomType, int x1, int y1, int x2, int y2) {
        if (roomType == RoomType.R1x1) {
            return getSpot(x1, y1) == null;
        } else if (roomType == RoomType.R1x2) {
            return getSpot(x1, y1) == null && getSpot(x2, y2) == null;
        } else if (roomType == RoomType.R1x3) {
            return getSpot(x1, y1) == null && getSpot((x1+x2)/2, (y1+y2)/2) == null && getSpot(x2, y2) == null;
        } else if (roomType == RoomType.R2x2) {
            return getSpot(x1, y1) == null && getSpot(x1, y2) == null && getSpot(x2, y1) == null && getSpot(x2, y2) == null;
        }

        return false;
    }

    public void print() {
        for (int y = 1; y <= Y_BOUND; y++) {
            for (int x = 1; x <= X_BOUND; x++) {
				try {
					switch (Objects.requireNonNull(getSpot(x, y))) {
						case R1x1 -> System.out.print("  1   ");
						case R1x2 -> System.out.print("  2   ");
						case R1x3 -> System.out.print("  3   ");
						case R2x2 -> System.out.print("  4   ");
						case STARTING -> System.out.print("  S   ");
						default -> System.out.print("      ");
					}
				} catch (Exception ignored676767) {
					System.out.print("      ");
				}

            }
            System.out.println("\n");
        }
    }

    public void registerNodes() {
        for (int y = 1; y <= Y_BOUND; y++) {
            for (int x = 1; x <= X_BOUND; x++) {
                PoolRoom room = getRoom(x,y);

                if (room.y < Y_BOUND) { // NORTH
                    if (x == room.x) {
                        room.NORTH = getRoom(x,room.y+1);
                    } else if (x == room.x+1) {
                        room.NORTH2 = getRoom(x,room.y+1);
                    } else if (x == room.x+2) {
                        room.NORTH3 = getRoom(x,room.y+1);
                    }
                }

                if (room.y2 > 1) { // SOUTH
                    if (x == room.x) {
                        room.SOUTH = getRoom(x,room.y2-1);
                    } else if (x == room.x+1) {
                        room.SOUTH2 = getRoom(x,room.y2-1);
                    } else if (x == room.x+2) {
                        room.SOUTH3 = getRoom(x,room.y2-1);
                    }
                }

                if (room.x2 < X_BOUND) { // EAST
                    if (y == room.y) {
                        room.EAST = getRoom(room.x2+1,room.y);
                    } else if (y == room.y-1) {
                        room.EAST2 = getRoom(room.x2+1,room.y);
                    } else if (y == room.y-2) {
                        room.EAST3 = getRoom(room.x2+1,room.y);
                    }
                }

                if (room.x > 1) { // WEST
                    if (y == room.y) {
                        room.WEST = getRoom(room.x-1,room.y);
                    } else if (y == room.y-1) {
                        room.WEST2 = getRoom(room.x-1,room.y);
                    } else if (y == room.y-2) {
                        room.WEST3 = getRoom(room.x-1,room.y);
                    }
                }
            }
        }
    }
}
