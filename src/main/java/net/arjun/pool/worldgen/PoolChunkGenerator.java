package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arjun.pool.PoolRooms;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PoolChunkGenerator extends ChunkGenerator {
	public static final Codec<PoolChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
	).apply(instance, PoolChunkGenerator::new));

	public PoolChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public void carve(ChunkRegion chunkRegion, long seed, RandomState randomState, BiomeAccess biomeAccess, StructureManager structureManager, Chunk chunk, GenerationStep.Carver generationStep) {

	}

	public static void fillBlock(Chunk chunk, int minX, int maxX, int minZ, int maxZ, int y, int chunkX, int chunkZ, BlockState state) {
		for (int x = minX; x < maxX; x++) {
			for (int z = minZ; z < maxZ; z++) {
				chunk.setBlockState(
					new BlockPos(x, y, z),
					state,
					false
				);
			}
		}
	}

	@Override
	public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureManager structureManager) {
		ServerWorld serverWorld = null;
		if (world instanceof ServerWorld sw) {
			serverWorld = sw;
		} else if (world instanceof ChunkRegion region) {
			serverWorld = region.toServerWorld(); // region has getWorld() in 1.20.1
		}

		if (serverWorld == null) {
//			err, serverWorld = null
			return; // fail-safe
		} // checks and loads

		PoolWorldState state = PoolWorldState.instance;
		Map<Pair<Integer, Integer>, RoomNode> roomMap = PoolRooms.currentMap;

		if (PoolRooms.currentMap == null) {
			state.currentGridSquare = Pair.of(0, 0);
			PoolRooms.currentMap = state.generateMap();
			roomMap = PoolRooms.currentMap; // Re-assign local variable so it doesn't crash!
		}

		int chunkX = chunk.getPos().x;
		int chunkZ = chunk.getPos().z;

		// 2. Get the absolute room coordinates for the 4 quadrants of this chunk
		int rX1 = chunkX * 2;
		int rX2 = chunkX * 2 + 1;
		int rZ1 = chunkZ * 2;
		int rZ2 = chunkZ * 2 + 1;

		// 3. Map the room coordinates to your custom [-9 to +10] Grid Squares
		// Math.floorDiv mathematically guarantees negatives round cleanly to the correct square
		int sqX1 = Math.floorDiv(rX1 + 9, state.gridSquareLength);
		int sqX2 = Math.floorDiv(rX2 + 9, state.gridSquareLength);
		int sqZ1 = Math.floorDiv(rZ1 + 9, state.gridSquareLength);
		int sqZ2 = Math.floorDiv(rZ2 + 9, state.gridSquareLength);

		// 4. A single chunk might straddle 1, 2, or 4 different grid squares!
		// Put them in a HashSet so we only generate unique squares.
		Set<Pair<Integer, Integer>> requiredSquares = new HashSet<>();
		requiredSquares.add(Pair.of(sqX1, sqZ1));
		requiredSquares.add(Pair.of(sqX2, sqZ1));
		requiredSquares.add(Pair.of(sqX1, sqZ2));
		requiredSquares.add(Pair.of(sqX2, sqZ2));

		// 5. Generate any grid squares that this chunk needs that don't exist yet
		for (Pair<Integer, Integer> square : requiredSquares) {
			if (!state.generatedGridSquares.contains(square)) {
				System.out.println("Chunk crossed border! Generating new Grid Square: " + square.first + ", " + square.second);
				state.currentGridSquare = square;
				PoolRooms.currentMap = state.generateMap();
			}
		}

		Pair<Integer, Integer> q1 = Pair.of(chunkX * 2, chunkZ * 2);
		Pair<Integer, Integer> q2 = Pair.of(chunkX * 2 + 1, chunkZ * 2);
		Pair<Integer, Integer> q3 = Pair.of(chunkX * 2, chunkZ * 2 + 1);
		Pair<Integer, Integer> q4 = Pair.of(chunkX * 2 + 1, chunkZ * 2 + 1);

		Pair<Integer, Integer>[] quadrants = new Pair[]{q1, q2, q3, q4};

		for (int i = 0; i < 4; i++) {
			Pair<Integer, Integer> currentQuadrant = quadrants[i];

			RoomNode room = roomMap.get(currentQuadrant);

			if (room == null) {
				continue;
			}

			int x = room.gridX * 8;
			int z = room.gridZ * 8;

			StructureTemplateManager manager = serverWorld.getStructureTemplateManager();
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
				0);

		}

	}

	@Override
	public void buildSurface(ChunkRegion region, StructureManager structureManager, RandomState randomState, Chunk chunk) {

	}

	@Override
	public void populateEntities(ChunkRegion region) {

	}

	@Override
	public int getWorldHeight() {
		return 0;
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, Chunk chunk) {
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinimumY() {
		return 0;
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, RandomState randomState) {
		return 0;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, RandomState randomState) {
		return null;
	}

	@Override
	public void method_40450(List<String> list, RandomState randomState, BlockPos pos) {

	}
}
