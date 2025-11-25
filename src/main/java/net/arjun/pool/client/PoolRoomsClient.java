package net.arjun.pool.client;

import net.arjun.pool.PoolRooms;
import net.arjun.pool.init.PoolBlockEntities;
import net.arjun.pool.init.PoolBlocks;
import net.coderbot.iris.Iris;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.ActionResult;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class PoolRoomsClient implements ClientModInitializer {

	public static final String SHADERPACK_NAME = "poolshader";


	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockRenderLayerMap
			.put(RenderLayer.getTranslucent(), PoolBlocks.LIGHT_LIMINAL_WINDOW);

		try {
			if (QuiltLoader.isModLoaded("iris")) {
				Iris.getIrisConfig().setShaderPackName(SHADERPACK_NAME);
				Iris.getIrisConfig().setShadersEnabled(true);
				Iris.getIrisConfig().save();
				Iris.reload();
			}
		} catch (Exception ignored) {
			PoolRooms.LOGGER.error("Could not load \"" + SHADERPACK_NAME + "\" shaderpack automatically, you can try to load it manually though!");
			try {
				if (QuiltLoader.isModLoaded("iris")) {
					Iris.getIrisConfig().setShaderPackName(SHADERPACK_NAME + ".zip");
					Iris.getIrisConfig().setShadersEnabled(true);
					Iris.getIrisConfig().save();
					Iris.reload();
				}
			} catch (Exception alsoIgnored_SSSIIIXXSEEVVVEEENPranavKurupatiIsSkibidi) {
				PoolRooms.LOGGER.error("Could not load \"" + SHADERPACK_NAME + ".zip\" shaderpack automatically, you can try to load it manually though!");
			}
		}

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			BlockState state = world.getBlockState(pos);

			// Only affect your block
			if (state.getBlock() != PoolBlocks.LIGHT_LIMINAL_WINDOW) return ActionResult.PASS;

			// Creative or spectator → allow normal behavior
			if (player.getAbilities().creativeMode || player.isSpectator()) return ActionResult.PASS;

			// Survival player → cancel hit
			return ActionResult.FAIL; // prevents client-side hit particles and break animation
		});

		BlockRenderLayerMap.put(
			RenderLayer.getTranslucent(),
			PoolBlocks.TRANSPARENT_BLOCK
		);

		BlockRenderLayerMap.put(
			RenderLayer.getTranslucent(),
			PoolBlocks.TRANSLUCENT_BLOCK
		);
	}

}
