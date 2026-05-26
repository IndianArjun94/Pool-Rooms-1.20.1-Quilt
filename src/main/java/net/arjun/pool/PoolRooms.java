package net.arjun.pool;

import com.ibm.icu.impl.Pair;
import com.mojang.serialization.Codec;
import net.arjun.pool.init.PoolBlockEntities;
import net.arjun.pool.init.PoolBlocks;
import net.arjun.pool.init.PoolModelRenderers;
import net.arjun.pool.worldgen.PoolChunkGenerator;
import net.arjun.pool.worldgen.PoolWorldState;
import net.arjun.pool.worldgen.RoomNode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PoolRooms implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Pool");

	private static MinecraftServer activeServer;

	public static final String MOD_ID = "pool";
	public static String MOD_NAME;

	public static int ticks = 0;

	public static final RegistryKey<Codec<? extends ChunkGenerator>> POOL_CHUNK_GENERATOR =
		RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new Identifier("pool", "pool_chunk_generator"));

	public static final Codec<PoolChunkGenerator> POOL_CHUNK_GENERATOR_CODEC = PoolChunkGenerator.CODEC;

	public static final RegistryKey<World> THE_LIBRARY_KEY =
		RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "library_dimension"));

	public static Map<Pair<Integer,Integer>, RoomNode> currentMap = null;

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

		MOD_NAME = mod.metadata().name();

		PoolBlocks.init();
		PoolBlockEntities.init();
		PoolModelRenderers.init();

		Registry.register(Registries.CHUNK_GENERATOR, new Identifier(MOD_ID, "pool_chunk_generator"), POOL_CHUNK_GENERATOR_CODEC);

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// 1. Get your specific dimension (Replace POOL_DIMENSION_KEY with your actual dimension key)
			ServerWorld poolWorld = server.getWorld(THE_LIBRARY_KEY);

			if (poolWorld != null) {
				// 2. Grab your PersistentState
				PoolWorldState state = PoolWorldState.get(poolWorld);

//				 3. Loop through all players currently in this dimension
//				for (ServerPlayerEntity player : poolWorld.getPlayers()) {
//
//					// 4. Check if the player is getting close to the edge of the current generation
//					if (state.isPlayerNearEdge(player, state)) {
//
//						// 5. Fire the Async Generator!
//						state.expandMapAsync(poolWorld);
//
//						// Break out of the player loop so we don't accidentally fire
//						// multiple generations if two players are near the edge
//						break;
//					}
//				}
			}
		});
	}

	public static Identifier id(String id) {
		return new Identifier("pool", id);
	}

	public static List<Vec3d> getPlayerPositions() {
		MinecraftServer server = activeServer;

		if (server == null) {
			return Collections.emptyList();
		}

		if (server.getPlayerManager() == null) {
			return Collections.emptyList();
		}

		List<ServerPlayerEntity> originalList = server.getPlayerManager().getPlayerList();

		synchronized (originalList) {
			List<Vec3d> positions = new ArrayList<>(originalList.size());
			for (ServerPlayerEntity player : originalList) {
				positions.add(player.getPos());
			}
			return positions;
		}
	}

	public static ServerWorld getServerWorld() {
		return activeServer.getWorld(THE_LIBRARY_KEY);
	}

}
