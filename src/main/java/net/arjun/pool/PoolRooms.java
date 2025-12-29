package net.arjun.pool;

import com.ibm.icu.impl.Pair;
import com.mojang.serialization.Codec;
import com.sun.jna.platform.win32.OaIdl;
import net.arjun.pool.init.PoolBlockEntities;
import net.arjun.pool.init.PoolBlocks;
import net.arjun.pool.init.PoolModelRenderers;
import net.arjun.pool.worldgen.PoolChunkGenerator;
import net.arjun.pool.worldgen.PoolWorldState;
import net.arjun.pool.worldgen.RegenerationHelper;
import net.arjun.pool.worldgen.RoomNode;
import net.minecraft.util.collection.Pool;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
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
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
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

		ServerWorldTickEvents.END.register((server, world) -> {
			if (world.getRegistryKey().equals(THE_LIBRARY_KEY) && !world.getPlayers().isEmpty()) {
				ticks++;
				RegenerationHelper.placeRoomsOnTick();

				if (!PoolWorldState.instance.pastFirstTime && ticks == 10) {
					PoolWorldState.instance.generateNewMap();
					RegenerationHelper.regenerating = true;
					System.out.println("PoolRooms: starting replacement");
				}
			}
		});

		ServerWorldLoadEvents.LOAD.register(((server, world) -> {
			if (world.getRegistryKey() == THE_LIBRARY_KEY) {
				System.out.println("PoolRooms: creating server");
				activeServer = server;
				ticks = 0;

				PoolWorldState.instance = PoolWorldState.get(world);

				if (PoolWorldState.instance.firstTime) {
					PoolWorldState.instance.generateNewMap();
				}
			}
		}));

		ServerWorldLoadEvents.UNLOAD.register(((server, world) ->{
			if (world.getRegistryKey() == THE_LIBRARY_KEY) {
				System.out.println("PoolRooms: deleting server");
				activeServer = null;
				ticks = 0;
				RegenerationHelper.reset();
			}
		}));
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
