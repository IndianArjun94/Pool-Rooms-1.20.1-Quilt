package net.arjun.pool.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SkyboxMaskBlock extends Block {
	public SkyboxMaskBlock(Settings settings) {
		super(settings);
	}

	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}

	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}
}
