package net.arjun.pool.worldgen.conceptdev;

import java.util.concurrent.ThreadLocalRandom;

import static net.arjun.pool.worldgen.conceptdev.PoolRoomsWorldMap.X_BOUND;
import static net.arjun.pool.worldgen.conceptdev.PoolRoomsWorldMap.Y_BOUND;

public class WorldGenConceptDevOld {

    public static int[] getAvailableRoom() {
        for (int x = 1; x <= X_BOUND; x++) {
            for (int y = 1; y <= Y_BOUND; y++) {
                if (PoolRoomsWorldMap.getInstance().spotIsAvailable(y,x)) {
                    return new int[]{y,x};
                }
            }
        } return new int[]{-1,-1};
    }

    /*TODO: I finished the single room generator in the loop, do this next time:
    *                - Change the room TO A VALID ROOM (empty/unassigned) after single room generation is done
    *                - Test!*/
    public static void main(String[] args) {
        System.out.println("This is WorldGenConceptDev!\n");

        int startingX = 8;
		int startingY = 8;

        int x = 1;
        int y = 1;

        PoolRoomsWorldMap map = new PoolRoomsWorldMap(15, 15);

        map.setRoom(RoomType.R1x1, startingX, startingY, startingX, startingY);

//		ROOM GEN *---------------------

        while (map.slotsFilled != X_BOUND*Y_BOUND) {
            int nextRoomAll = ThreadLocalRandom.current().nextInt(1, 5);
            int nextRoomThree = ThreadLocalRandom.current().nextInt(1,4);
            int nextRoomTwo = ThreadLocalRandom.current().nextInt(1,3);
            int garunteedOne = ThreadLocalRandom.current().nextInt(1,4);

			int rand1to4 = ThreadLocalRandom.current().nextInt(1, 5);

            if (nextRoomAll == 4 && garunteedOne != 1) { // R2x2
				switch (rand1to4) {
					case 1: if (map.roomIsAvailable(RoomType.R2x2, x,y,x+1,y+1)) {
						map.setRoom(RoomType.R2x2, x,y,x+1,y+1);
					}
					case 2: if (map.roomIsAvailable(RoomType.R2x2, x,y,x+1,y-1)) {
						map.setRoom(RoomType.R2x2, x,y,x+1,y-1);
					}
					case 3: if (map.roomIsAvailable(RoomType.R2x2, x,y,x-1,y-1)) {
						map.setRoom(RoomType.R2x2, x,y,x-1,y-1);
					}
					case 4: if (map.roomIsAvailable(RoomType.R2x2, x,y,x-1,y+1)) {
						map.setRoom(RoomType.R2x2, x,y,x-1,y+1);
					}
				}

                if (map.roomIsAvailable(RoomType.R2x2, x,y,x+1,y+1)) {
                    map.setRoom(RoomType.R2x2, x,y,x+1,y+1);
                } else if (map.roomIsAvailable(RoomType.R2x2, x,y,x+1,y-1)) {
                    map.setRoom(RoomType.R2x2, x,y,x+1,y-1);
                } else if (map.roomIsAvailable(RoomType.R2x2, x,y,x-1,y-1)) {
                    map.setRoom(RoomType.R2x2, x,y,x-1,y-1);
                } else if (map.roomIsAvailable(RoomType.R2x2, x,y,x-1,y+1)) {
                    map.setRoom(RoomType.R2x2, x,y,x-1,y+1);
                }
            } else if (nextRoomAll == 3 || nextRoomThree == 3 && garunteedOne != 1) {
				switch (rand1to4) {
					case 1: if (map.roomIsAvailable(RoomType.R1x3, x,y,x+2,y)) {
						map.setRoom(RoomType.R1x3, x,y,x+2,y);
					}
					case 2: if (map.roomIsAvailable(RoomType.R1x3, x,y,x-2,y)) {
						map.setRoom(RoomType.R1x3, x,y,x-2,y);
					}
					case 3: if (map.roomIsAvailable(RoomType.R1x3, x,y,x,y+2)) {
						map.setRoom(RoomType.R1x3, x,y,x,y+2);
					}
					case 4: if (map.roomIsAvailable(RoomType.R1x3, x,y,x,y-2)) {
						map.setRoom(RoomType.R1x3, x,y,x,y-2);
					}
				}

                if (map.roomIsAvailable(RoomType.R1x3, x,y,x+2,y)) {
                    map.setRoom(RoomType.R1x3, x,y,x+2,y);
                } else if (map.roomIsAvailable(RoomType.R1x3, x,y,x-2,y)) {
                    map.setRoom(RoomType.R1x3, x,y,x-2,y);
                } else if (map.roomIsAvailable(RoomType.R1x3, x,y,x,y+2)) {
                    map.setRoom(RoomType.R1x3, x,y,x,y+2);
                } else if (map.roomIsAvailable(RoomType.R1x3, x,y,x,y-2)) {
                    map.setRoom(RoomType.R1x3, x,y,x,y-2);
                }
            } else if (nextRoomAll == 2 || nextRoomThree == 2 || nextRoomTwo == 2 && garunteedOne != 1) {
				switch (rand1to4) {
					case 1: if (map.roomIsAvailable(RoomType.R1x2, x,y,x+1,y)) {
						map.setRoom(RoomType.R1x2, x,y,x+1,y);
					}
					case 2: if (map.roomIsAvailable(RoomType.R1x2, x,y,x-1,y)) {
						map.setRoom(RoomType.R1x2, x,y,x-1,y);
					}
					case 3: if (map.roomIsAvailable(RoomType.R1x2, x,y,x,y+1)) {
						map.setRoom(RoomType.R1x2, x,y,x,y+1);
					}
					case 4: if (map.roomIsAvailable(RoomType.R1x2, x,y,x,y-1)) {
						map.setRoom(RoomType.R1x2, x,y,x,y-1);
					}
				}

                if (map.roomIsAvailable(RoomType.R1x2, x,y,x+1,y)) {
                    map.setRoom(RoomType.R1x2, x,y,x+1,y);
                } else if (map.roomIsAvailable(RoomType.R1x2, x,y,x-1,y)) {
                    map.setRoom(RoomType.R1x2, x,y,x-1,y);
                } else if (map.roomIsAvailable(RoomType.R1x2, x,y,x,y+1)) {
                    map.setRoom(RoomType.R1x2, x,y,x,y+1);
                } else if (map.roomIsAvailable(RoomType.R1x2, x,y,x,y-1)) {
                    map.setRoom(RoomType.R1x2, x,y,x,y-1);
                }
            } else {
                if (map.roomIsAvailable(RoomType.R1x1, x,y,x,y)) {
                    map.setRoom(RoomType.R1x1, x,y,x,y);
                }
            }

            int[] xy = getAvailableRoom();
            x = xy[0];
            y = xy[1];
        }

        map.registerNodes();

//		map.print();

//		ENTRANCE GEN *-------------------

		boolean northDone = false;
		boolean eastDone = false;
		boolean southDone = false;
		boolean westDone = false;

		map.getRoom(startingX,startingY).ENTRANCE1 = RoomPosition.NORTH;
		map.getRoom(startingX,startingY+1).ENTRANCE1 = RoomPosition.SOUTH;

		RoomPosition previousRoom = RoomPosition.SOUTH;

		int prevX = startingX;
		int prevY = startingY;
		x = startingX+1;
		y = startingY;

		while (!northDone) {

			int random1to3 = ThreadLocalRandom.current().nextInt(1,4);
			int index = 1;

			if (map.getRoom(x,y).type == RoomType.R1x1) { // 1x1 rooms
				for (RoomPosition type : RoomPosition.values()) {
					if (type != previousRoom) {
						if (index == random1to3) {
							map.getRoom(x,y).ENTRANCE2 = type;
						}
						index++;
					}
				}
			} else if (map.getRoom(x,y).type == RoomType.R1x2) {
				for (RoomPosition type : RoomPosition.values()) {
					if (type != previousRoom) {
						if (index == random1to3) {
							map.getRoom(x,y).ENTRANCE2 = type;
						}
						index++;
					}
				}
			}
		}
    }
}
