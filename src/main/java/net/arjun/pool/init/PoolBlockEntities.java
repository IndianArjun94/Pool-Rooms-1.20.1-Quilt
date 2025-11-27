package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;

public class PoolBlockEntities {

//    public static final BlockEntityType<OffsetPoolTilesBE> OFFSET_POOL_TILES_BE =
//            Registry.register(Registries.BLOCK_ENTITY_TYPE,
//				new Identifier(PoolRooms.MOD_ID, "offset_pool_tiles_be"),
//				BlockEntityType.Builder.create(
//					(pos, state) -> new OffsetPoolTilesBE(null, pos, state),
//					PoolBlocks.OFFSET_POOL_TILES)
//					.build(null)
//			);

    public static void init() {
        PoolRooms.LOGGER.info("Registering Block Entities for " + PoolRooms.MOD_NAME);
    }
}
