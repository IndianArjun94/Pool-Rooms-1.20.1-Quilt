package net.arjun.pool.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugPortalBlock extends Block {

	private final RegistryKey<World> targetDim;

	public DebugPortalBlock(Settings settings, RegistryKey<World> targetDim) {
		super(settings);
		this.targetDim = targetDim;
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClient && entity instanceof PlayerEntity player) {
			ServerWorld dimension = player.getServer().getWorld(targetDim);

			if (dimension != null) {
				BlockPos spawn = new BlockPos(8, 150, 8);

				dimension.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4); // floor div by 16 using 4 bit-shifts

				player.getServer().execute(() ->
					player.teleport(
						dimension,
						spawn.getX() - 0.5,
						spawn.getY(),
						spawn.getZ() - 0.5,
						MovementFlag.ALL,
						player.getYaw(),
						player.getPitch()
					)
				);
			}
		}
	}
}
