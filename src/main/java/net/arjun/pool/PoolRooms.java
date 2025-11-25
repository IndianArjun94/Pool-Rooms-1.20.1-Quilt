package net.arjun.pool;

import com.mojang.serialization.Codec;
import net.arjun.pool.init.PoolBlockEntities;
import net.arjun.pool.init.PoolBlocks;
import net.arjun.pool.init.PoolModelRenderers;
import net.arjun.pool.worldgen.PoolChunkGenerator;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolRooms implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Pool");

	public static final String MOD_ID = "pool";
	public static String MOD_NAME;

	public static final RegistryKey<Codec<? extends ChunkGenerator>> POOL_CHUNK_GENERATOR =
		RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new Identifier("pool", "pool_chunk_generator"));

	public static final Codec<PoolChunkGenerator> POOL_CHUNK_GENERATOR_CODEC = PoolChunkGenerator.CODEC;

	public static final RegistryKey<World> THE_LIBRARY_KEY =
		RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "library_dimension"));

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

		MOD_NAME = mod.metadata().name();


		PoolBlocks.init();
		PoolBlockEntities.registerBlockEntities();
		PoolModelRenderers.init();

		Registry.register(Registries.CHUNK_GENERATOR, new Identifier(MOD_ID, "pool_chunk_generator"), POOL_CHUNK_GENERATOR_CODEC);


	}

	public static Identifier id(String id) {
		return new Identifier("pool", id);
	}

}
