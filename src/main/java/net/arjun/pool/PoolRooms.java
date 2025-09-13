package net.arjun.pool;

import net.arjun.pool.init.PoolBlocks;
import net.arjun.pool.init.PoolModelRenderers;
import net.minecraft.util.Identifier;
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

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

		MOD_NAME = mod.metadata().name();

		PoolBlocks.init();
		PoolModelRenderers.init();
	}

	public static Identifier id(String id) {
		return new Identifier("pool", id);
	}

}
