package net.arjun.pool.worldgen;

import com.ibm.icu.impl.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sun.jdi.Mirror;
import net.arjun.pool.PoolRooms;
import net.arjun.pool.init.PoolBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.model.VariantSettings;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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

		PoolWorldState pws = PoolWorldState.get(serverWorld);
		Map<Pair<Integer,Integer>, RoomNode> roomMap = pws.generateRooms(pws.seed);

		int chunkX = chunk.getPos().x;
		int chunkZ = chunk.getPos().z;

		Pair<Integer, Integer> chunkPos = Pair.of(chunkX,chunkZ);

		if (roomMap.containsKey(chunkPos) && !(chunkX >= -1 && chunkX <= 1 && chunkZ >= -1 && chunkZ <= 1)) {

			RoomNode room = roomMap.get(chunkPos);

			StructureTemplateManager manager = serverWorld.getStructureTemplateManager();
			Optional<Structure> _structure = manager.getStructure(new Identifier(PoolRooms.MOD_ID, room.structureId));

			StructurePlacementData placementData = new StructurePlacementData()
				.setRotation(BlockRotation.NONE)
				.setMirror(BlockMirror.NONE)
				.setIgnoreEntities(true);

			if (!_structure.isPresent()) return;

			Structure structure = _structure.get();

			structure.place(world,
				new BlockPos(chunkX * 16, 80, chunkZ * 16),
				new BlockPos(0, 0, 0),
				placementData,
				world.getRandom(),
				0);

//			for (int x = 5; x < 11; x++) {
//				for (int z = 5; z < 11; z++) {
//					chunk.setBlockState(
//						new BlockPos(x,100,z),
//						PoolBlocks.POOL_TILES.getDefaultState(),
//						false
//					);
//				}
//			}
//
//			for (RoomNode neighbor : room.connections) {
//				int minx = 5;
//				int minz = 5;
//				int maxx = 11;
//				int maxz = 11;
//				boolean changed = false;
//
//				if (neighbor.x > room.x) {
//					minx = 11;
//					maxx = 16;
//					changed = true;
//				} if (neighbor.x < room.x) {
//					minx = 0;
//					maxx = 5;
//					changed = true;
//				} if (neighbor.z > room.z) {
//					minz = 11;
//					maxz = 16;
//					changed = true;
//				} if (neighbor.z < room.z) {
//					minz = 0;
//					maxz = 5;
//					changed = true;
//				}
//
//				if (!changed) return;
//
//
//				for (int x = minx; x < maxx; x++) {
//					for (int z = minz; z < maxz; z++) {
//						chunk.setBlockState(
//							new BlockPos(x,100,z),
//							PoolBlocks.POOL_TILES.getDefaultState(),
//							false
//						);
//					}
//				}
//			}
		} else {
			if (chunkX >= -1 && chunkX <= 1 && chunkZ >= -1 && chunkZ <= 1) {
				StructureTemplateManager manager = serverWorld.getStructureTemplateManager();
				Optional<Structure> _structure = manager.getStructure(new Identifier(PoolRooms.MOD_ID, "start"));
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
					new BlockPos(-16, 80-6, -16),
					new BlockPos(0, 0, 0),
					placementData,
					world.getRandom(),
					0);

				structure2.place(world,
					new BlockPos(-16, 80-6+48, -16),
					new BlockPos(0, 0, 0),
					placementData,
					world.getRandom(),
					0);
			}
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
