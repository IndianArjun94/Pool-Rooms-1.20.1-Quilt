package net.arjun.pool.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.arjun.pool.init.PoolBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.irisshaders.iris.Iris;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

public class PoolRoomsClient implements ClientModInitializer {


	@Override
	public void onInitializeClient(ModContainer mod) {
		BlockRenderLayerMap
			.put(RenderLayer.getTranslucent(), PoolBlocks.LIGHT_LIMINAL_WINDOW);

		try {
			if (QuiltLoader.isModLoaded("iris")) {
				Iris.getIrisConfig().setShaderPackName("poolshader");
				Iris.getIrisConfig().setShadersEnabled(true);
				Iris.getIrisConfig().save();
				Iris.reload();
			}
		} catch (Exception e) {
//			throw new RuntimeException(e);
		}

	}

}
