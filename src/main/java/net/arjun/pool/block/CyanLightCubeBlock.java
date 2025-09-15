package net.arjun.pool.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class CyanLightCubeBlock extends Block {
	public static final VoxelShape SHAPE = Block.createCuboidShape(
		4, 0, 4,
		12, 8, 12
	);

	public CyanLightCubeBlock() {
		super(AbstractBlock.Settings.copy(Blocks.GLASS)
			.strength(0.3f)
			.nonOpaque()
			.luminance(state -> 15)
		);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
}
