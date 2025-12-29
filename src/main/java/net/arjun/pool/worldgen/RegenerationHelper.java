package net.arjun.pool.worldgen;

import net.arjun.pool.PoolRooms;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Optional;

public class RegenerationHelper {

	public static int roomsGenerated = 0;

	public static boolean regenerating = false;

	public static final int roomsPerTick = 1;

	public static void reset() {
		regenerating = false;
		roomsGenerated = 0;
	}

	public static void placeRoomsOnTick() {
		ServerWorld world = PoolRooms.getServerWorld();

		if (!regenerating) {
			return;
		}

		System.out.println("RegenerationHelper: placing");

		StructureTemplateManager manager = world.getStructureTemplateManager();

		if (PoolRooms.currentMap == null || PoolWorldState.instance == null) {
			System.out.println("RegenerationHelper: uh-oh! currentMap OR PWS.instance = null!");
			return;
		}

		for (int i = 0; i < roomsPerTick; i++) {
			if (roomsGenerated >= PoolRooms.currentMap.size()) {
				roomsGenerated = 0;
				regenerating = false;
				return;
			}

			RoomNode room = (RoomNode) PoolRooms.currentMap.values().toArray()[roomsGenerated];

			int x = room.gridX * 8;
			int z = room.gridZ * 8;

			ChunkPos cp = new ChunkPos(x, z);
			world.getChunk(cp.x, cp.z);

			clearArea(world, x, z, room.gridLengthX*8, room.gridLengthZ*8, 160);

			Optional<Structure> _structure = manager.getStructure(new Identifier(PoolRooms.MOD_ID, room.structureId));

			if (room.roomSize == RoomSize.START) {
				Optional<Structure> _structure2 = manager.getStructure(new Identifier(PoolRooms.MOD_ID, "start_top"));

				StructurePlacementData placementData = new StructurePlacementData()
					.setRotation(BlockRotation.NONE)
					.setMirror(BlockMirror.NONE)
					.setIgnoreEntities(true);

				if (!_structure.isPresent()) return;
				if (!_structure2.isPresent()) return;

				Structure structure = _structure.get();
				Structure structure2 = _structure2.get();

				structure.place(world,
					new BlockPos(x, 48, z),
					new BlockPos(0, 0, 0),
					placementData,
					world.getRandom(),
					0);

				structure2.place(world,
					new BlockPos(x, 96, z),
					new BlockPos(0, 0, 0),
					placementData,
					world.getRandom(),
					0);

				continue;
			}

			StructurePlacementData placementData = new StructurePlacementData()
				.setRotation(BlockRotation.NONE)
				.setMirror(BlockMirror.NONE)
				.setIgnoreEntities(true);

			if (!_structure.isPresent()) return;

			Structure structure = _structure.get();

			structure.place(world,
				new BlockPos(x, 48, z),
				new BlockPos(0, 0, 0),
				placementData,
				world.getRandom(),
				3);

			roomsGenerated++;
		}

		System.out.println("RegenerationHelper placed: firstTime = " + PoolWorldState.instance.firstTime);
	}

	public static void clearArea(ServerWorld world, int x, int z, int width, int height, int depth) {
		int yStart = 0;     // adjust if needed
		int yEnd = height;  // top of your room

		for (int dx = 0; dx < width; dx++) {
			for (int dz = 0; dz < depth; dz++) {
				for (int y = yStart; y <= yEnd; y++) {
					BlockPos pos = new BlockPos(x + dx, y, z + dz);
					world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
				}
			}
		}
	}

}
