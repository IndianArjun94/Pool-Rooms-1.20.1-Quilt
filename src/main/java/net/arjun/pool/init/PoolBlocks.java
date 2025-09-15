package net.arjun.pool.init;

import net.arjun.pool.PoolRooms;
import net.arjun.pool.block.SkyboxGlassBlock;
import net.arjun.pool.block.CyanLightCubeBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public class PoolBlocks {

	public static final Block POOL_TILES = registerBlock("pool_tiles",
		new Block(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.STONE)));

	public static final Block POOL_TILE_STAIRS = registerBlock("pool_tile_stairs",
		new StairsBlock(PoolBlocks.POOL_TILES.getDefaultState(), AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.STONE)));

	public static final Block POOL_TILE_SLAB = registerBlock("pool_tile_slab",
		new SlabBlock(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.STONE)));

	public static final BlockFamily POOL_TILES_FAMILY =
		new BlockFamily.Builder(PoolBlocks.POOL_TILES)
			.slab(PoolBlocks.POOL_TILE_SLAB)
			.stairs(PoolBlocks.POOL_TILE_STAIRS)
			.build();

	public static final Block LIGHT_LIMINAL_WINDOW = registerBlock("light_liminal_window",
		new SkyboxGlassBlock(QuiltBlockSettings.copyOf(Blocks.GLASS).luminance(3)));

	public static final Block POOL_BOOKSHELF = registerBlock("pool_bookshelf",
		new Block(QuiltBlockSettings.copyOf(Blocks.CHISELED_BOOKSHELF)));

	public static final Block CYAN_LIGHT_CUBE = registerBlock("cyan_light_cube",
		new CyanLightCubeBlock());

	private static Block registerBlock(String name, Block block) {
		registerBlockItem(name, block);
		return Registry.register(Registries.BLOCK, new Identifier(PoolRooms.MOD_ID, name), block);
	}

	private static Item registerBlockItem(String name, Block block) {
		Item item = Registry.register(Registries.ITEM, new Identifier(PoolRooms.MOD_ID, name),
			new BlockItem(block, new FabricItemSettings()));

		return item;
	}

	public static void init() {
		PoolRooms.LOGGER.info("Registering Blocks for " + PoolRooms.MOD_NAME);
	}
}
