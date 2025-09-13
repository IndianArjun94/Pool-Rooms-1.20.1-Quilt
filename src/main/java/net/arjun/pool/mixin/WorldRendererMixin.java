package net.arjun.pool.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arjun.pool.init.PoolBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(
		method = "drawBlockOutline",
		at = @At("HEAD"),
		cancellable = true
	)
	private void skipOutline(MatrixStack matrices, VertexConsumer consumer, Entity entity, double offsetX, double offsetY, double offsetZ, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (blockState.isOf(PoolBlocks.LIGHT_LIMINAL_WINDOW)) {
			ci.cancel();
		}
	}
}
