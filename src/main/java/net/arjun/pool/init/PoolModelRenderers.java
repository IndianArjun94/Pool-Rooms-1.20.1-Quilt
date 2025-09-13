package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;
import net.arjun.pool.client.render.SkyboxRenderer;
import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.minecraft.registry.Registry;

public class PoolModelRenderers {

	public static final SpecialModelRenderer LIGHT_SKYBOX_RENDERER = get("light_skybox",
		new SkyboxRenderer("light"));

	public static void init() {
		PoolRooms.LOGGER.info("Registering Model Renderers for " + PoolRooms.MOD_NAME);
	}

	public static <S extends SpecialModelRenderer> S get(String id, S modelRenderer) {
		return Registry.register(SpecialModelRenderer.SPECIAL_MODEL_RENDERER, PoolRooms.id(id), modelRenderer);
	}

}
