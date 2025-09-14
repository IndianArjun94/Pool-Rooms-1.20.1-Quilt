package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;

public class PoolBlockEntities {

//    public static final BlockEntityType<OffsetPoolTilesBE> OFFSET_POOL_TILES_BE =
//            Registry.register(Registries.BLOCK_ENTITY_TYPE,
//				new Identifier(PoolRooms.MOD_ID, "offset_pool_tiles_be"),
//				BlockEntityType.Builder.create(
//					(pos, state) -> new OffsetPoolTilesBE(null, pos, state),
//					PoolBlocks.OFFSET_POOL_TILES)
//					.build(null)
//			);

    public static void registerBlockEntities() {
        PoolRooms.LOGGER.info("Registering Block Entities for " + PoolRooms.MOD_NAME);
    }
}
