package net.arjun.pool.worldgen.conceptdev;

import java.util.concurrent.ThreadLocalRandom;

public class WorldGenConceptNew {
	public static void main(String[] args) {
		int startingX = 8;
		int startingY = 8;

		PoolRoomsWorldMap map = new PoolRoomsWorldMap(15, 15);

		map.setRoom(RoomType.STARTING, startingX, startingY, startingX, startingY); // starting (center) room

//		Gen Starts Here!!!!!

		int x = startingX-1;
		int y = startingY;

		PoolRoom previousRoom = map.getRoom(startingX,startingY);
		PoolRoom currentRoom = null;

		while (true) {
			int random3 = ThreadLocalRandom.current().nextInt(1, 4);

//			if (random3 == 1) {
//				make room
				if (map.roomIsAvailable(RoomType.R1x1, x,y,x,y)) {
					map.setRoom(RoomType.R1x1, x,y,x,y);
				} else if (map.getSpot(x,y) == null) { // path has encountered a room!!!!!
					break;
				} else { // *CRIES SOBS ALL THE THINGS* path has encountered room bounds (WIDTH*HEIGHT)
					break;
				}

//				NORMAL ROUTE: generate room in the path and connect it (entrances)
				currentRoom = map.getRoom(x,y);

//				make entrances
				if (previousRoom.x+1 == currentRoom.x) { // going right
					previousRoom.ENTRANCE2 = RoomPosition.EAST; // previous room entrance to current
					currentRoom.ENTRANCE1 = RoomPosition.WEST; // current room entrance to previous
				} else if (previousRoom.x-1 == currentRoom.x) { // going left
					previousRoom.ENTRANCE2 = RoomPosition.WEST;
					currentRoom.ENTRANCE1 = RoomPosition.EAST;
				} else if (previousRoom.y+1 == currentRoom.y) { // going up
					previousRoom.ENTRANCE2 = RoomPosition.NORTH;
					currentRoom.ENTRANCE1 = RoomPosition.SOUTH;
				} else if (previousRoom.y-1 == currentRoom.y) { // going down
					previousRoom.ENTRANCE2 = RoomPosition.SOUTH;
					currentRoom.ENTRANCE1 = RoomPosition.NORTH;
				}

//				decide next room location
				int[] check1 = {x,y+1}; //up
				int[] check2 = {x+1,y}; //right
				int[] check3 = {x,y-1}; //down
				int[] check4 = {x-1,y}; //left

				boolean checkDone = false;

				int secondRandom = ThreadLocalRandom.current().nextInt(1, 6);

				if ((secondRandom == 1 || secondRandom == 2 || secondRandom == 3)) { // set next room location
					int thirdRandom = ThreadLocalRandom.current().nextInt(1, 3);
					if (thirdRandom == 1 && map.getRoom(check1[0], check1[1]) == null) {
						x = check1[0];
						y = check1[1];
						checkDone = true;
					} else if (map.getRoom(check3[0], check3[1]) == null){
						x = check3[0];
						y = check3[1];
						checkDone = true;
					}
				} if (secondRandom == 4 && map.getRoom(check2[0], check2[1]) == null) {
					x = check2[0];
					y = check2[1];
					checkDone = true;
				} else if (secondRandom == 5 && map.getRoom(check4[0], check4[1]) == null) {
					x = check4[0];
					y = check4[1];
					checkDone = true;
				} else if (!checkDone) { // if random selected a non-valid room location:
					if (map.getRoom(check2[0],check2[1])==null) {
						x = check2[0];
						y = check2[1];
					} else if (map.getRoom(check1[0],check1[1])==null) {
						x = check1[0];
						y = check1[1];
					} else if (map.getRoom(check3[0],check3[1])==null) {
						x = check3[0];
						y = check3[1];
					} else if (map.getRoom(check4[0],check4[1])==null) {
						x = check4[0];
						y = check4[1];
					}
				}

				previousRoom = currentRoom;
//			}
		}

		map.print();

		System.out.println("cool");

	}
}
