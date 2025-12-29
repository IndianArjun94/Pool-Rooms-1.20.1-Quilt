package net.arjun.pool.block;

import net.arjun.pool.worldgen.PoolWorldState;
import net.arjun.pool.worldgen.RegenerationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TranslucentBlock extends TransparentBlock {

	private static final int STEP = 25;
	private static final int MIN = 25;
	private static final int MAX = 75;

	public static final IntProperty TRANSPARENCY = IntProperty.of("transparency", 0, 100);

	public TranslucentBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(TRANSPARENCY, 25));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TRANSPARENCY);
	}

	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}

	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			int current = state.get(TRANSPARENCY);
			int next = current + STEP;
			if (next > MAX) next = MIN;

			// Update the block state in the world. This syncs to the client automatically.
			world.setBlockState(pos, state.with(TRANSPARENCY, next), Block.NOTIFY_ALL);

			if (!RegenerationHelper.regenerating) {
				PoolWorldState.instance.generateNewMap();

				RegenerationHelper.regenerating = true;
				System.out.println("TranslucentBlock: Starting RegenerationHelper");
			} else {
				System.out.print("TranslucentBlock: cannot restart RegenerationHelper placement; it has already started");
			}


			return ActionResult.SUCCESS;
		}
		return ActionResult.CONSUME; // Return PASS on the client side
	}
}
