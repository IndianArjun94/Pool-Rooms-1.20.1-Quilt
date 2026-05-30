package net.arjun.pool.client;

import net.arjun.pool.PoolRooms;
import net.arjun.pool.client.render.SkyboxRenderer;
import net.arjun.pool.init.PoolBlockEntities;
import net.arjun.pool.init.PoolBlocks;
import net.arjun.pool.init.PoolSounds;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

public class PoolRoomsClient implements ClientModInitializer {

//	public static final String SHADERPACK_NAME = "poolshader";


	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockRenderLayerMap
			.put(RenderLayer.getTranslucent(), PoolBlocks.LIGHT_LIMINAL_WINDOW,PoolBlocks.LIGHT_LIMINAL_PANEL,PoolBlocks.TRANSPARENT_BLOCK,PoolBlocks.TRANSLUCENT_BLOCK);

		ClientTickEvents.END.register(client -> {
			PoolSounds.runAmbientSounds(client);

			// any other stuff later goes here
		});

//		{
//			if (client.player == null || client.world == null) return;
//
//			String currentDimension = client.world.getRegistryKey().getValue().toString();
//			if (!currentDimension.equals("pool:pools")) return;
//
//			Vec3d velocity = client.player.getVelocity();
//			double horizontalSpeed = Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z));
//
//			if (horizontalSpeed > 0.05) {
//
//				// 4. The RNG (1 in 1200 chance per tick = roughly once every 60 seconds of movement)
//				if (client.world.random.nextInt(1200) == 0) {
//
//					// 5. The Math: Find the spot 3 blocks directly behind where the player is looking
//					Vec3d lookVec = client.player.getRotationVec(1.0F);
//					Vec3d playerPos = client.player.getPos();
//
//					// We only subtract X and Z so the sound stays at foot level, not up in the air!
//					double behindX = playerPos.x - (lookVec.x * 12.0);
//					double behindY = playerPos.y;
//					double behindZ = playerPos.z - (lookVec.z * 12.0);
//
//					// 6. Play the ghost footstep
//					client.world.playSound(
//						behindX, behindY, behindZ,
//						SoundEvents.ENTITY_PLAYER_SPLASH, // The vanilla splash sound
//						SoundCategory.AMBIENT,
//						0.9f, // Volume
//						0.8f + (client.world.random.nextFloat() * 0.4f), // Randomize pitch so it sounds organic
//						false
//					);
//				}
//			}
//
//
//		});

//		try {
//			if (QuiltLoader.isModLoaded("iris")) {
//				Iris.getIrisConfig().setShaderPackName(SHADERPACK_NAME);
//				Iris.getIrisConfig().setShadersEnabled(true);
//				Iris.getIrisConfig().save();
//				Iris.reload();
//			}
//		} catch (Exception ignored) {
//			PoolRooms.LOGGER.error("Could not load \"" + SHADERPACK_NAME + "\" shaderpack automatically, you can try to load it manually though!");
//			try {
//				if (QuiltLoader.isModLoaded("iris")) {
//					Iris.getIrisConfig().setShaderPackName(SHADERPACK_NAME + ".zip");
//					Iris.getIrisConfig().setShadersEnabled(true);
//					Iris.getIrisConfig().save();
//					Iris.reload();
//				}
//			} catch (Exception e) {
//				PoolRooms.LOGGER.error("Could not load \"" + SHADERPACK_NAME + ".zip\" shaderpack automatically, you can try to load it manually though!");
//			}
//		}

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			BlockState state = world.getBlockState(pos);

			if (state.getBlock() != PoolBlocks.LIGHT_LIMINAL_WINDOW && state.getBlock() != PoolBlocks.LIGHT_LIMINAL_PANEL) return ActionResult.PASS;

			if (player.getAbilities().creativeMode || player.isSpectator()) return ActionResult.PASS;

			return ActionResult.FAIL;
		});
	}

}
