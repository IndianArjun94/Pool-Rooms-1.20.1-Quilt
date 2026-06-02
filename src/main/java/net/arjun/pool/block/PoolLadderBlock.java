package net.arjun.pool.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class PoolLadderBlock extends Block implements Waterloggable {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public static final BooleanProperty TOP = BooleanProperty.of("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.of("bottom");

	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 16.0D, 10.0D);
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 14.0D);

	protected static final VoxelShape TOP_EAST_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 0.0D, 10.0D, 9.0D, 16.0D);
	protected static final VoxelShape TOP_WEST_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 14.0D, 9.0D, 16.0D);
	protected static final VoxelShape TOP_SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 9.0D, 10.0D);
	protected static final VoxelShape TOP_NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 9.0D, 14.0D);

	public PoolLadderBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(WATERLOGGED, false)
			.with(TOP, false)
			.with(BOTTOM, false));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		boolean isTop = state.get(TOP);
		switch (state.get(FACING)) {
			case NORTH: return isTop ? TOP_NORTH_SHAPE : NORTH_SHAPE;
			case SOUTH: return isTop ? TOP_SOUTH_SHAPE : SOUTH_SHAPE;
			case WEST: return isTop ? TOP_WEST_SHAPE : WEST_SHAPE;
			case EAST:
			default: return isTop ? TOP_EAST_SHAPE : EAST_SHAPE;
		}
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		Direction facing = ctx.getPlayer() != null ? ctx.getPlayer().getHorizontalFacing().getOpposite() : Direction.NORTH;

		BlockPos pos = ctx.getBlockPos();
		WorldAccess world = ctx.getWorld();

		boolean hasLadderAbove = world.getBlockState(pos.up()).isOf(this);
		boolean hasLadderBelow = world.getBlockState(pos.down()).isOf(this);
		boolean isTouchingWall = !world.isAir(pos.offset(Direction.NORTH))
			|| !world.isAir(pos.offset(Direction.SOUTH))
			|| !world.isAir(pos.offset(Direction.EAST))
			|| !world.isAir(pos.offset(Direction.WEST));

		boolean isTouchingWater = world.isWater(pos.offset(Direction.NORTH))
			|| world.isWater(pos.offset(Direction.SOUTH))
			|| world.isWater(pos.offset(Direction.EAST))
			|| world.isWater(pos.offset(Direction.WEST));

		return this.getDefaultState()
			.with(FACING, facing)
			.with(WATERLOGGED, isTouchingWater)
			.with(TOP, !hasLadderAbove)
			.with(BOTTOM, !hasLadderBelow && isTouchingWall && hasLadderAbove);

	}

	// Handles water flowing in and out
	// Recalculates the curves dynamically if a neighbor block is broken/placed
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		Direction facing = state.get(FACING);
		boolean hasLadderAbove = world.getBlockState(pos.up()).isOf(this);
		boolean hasLadderBelow = world.getBlockState(pos.down()).isOf(this);
		boolean isTouchingWall = !world.isAir(pos.offset(Direction.NORTH))
			|| !world.isAir(pos.offset(Direction.SOUTH))
			|| !world.isAir(pos.offset(Direction.EAST))
			|| !world.isAir(pos.offset(Direction.WEST));

		return state
			.with(TOP, !hasLadderAbove)
			.with(BOTTOM, !hasLadderBelow && isTouchingWall);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, TOP, BOTTOM);
	}



}
