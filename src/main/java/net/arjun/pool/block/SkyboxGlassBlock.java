package net.arjun.pool.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.lang.reflect.Field;

public class SkyboxGlassBlock extends GlassBlock {

	public SkyboxGlassBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos,
										ShapeContext context) {
		Entity entity = context instanceof EntityShapeContext esc ? esc.getEntity() : null;
		if (entity instanceof PlayerEntity player) {
			if (player.getAbilities().creativeMode && !player.isSpectator()) {
				return VoxelShapes.empty();
			}
		}
		return state.getOutlineShape(world, pos);
	}
}
