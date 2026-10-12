package com.JulieISBaka.patchwork.features.blocks;

import com.JulieISBaka.patchwork.Patchwork;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;


public final class Woodcutter {
	private static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Patchwork.id("woodcutter"));
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, Patchwork.id("woodcutter"));
	private static final ResourceKey<MenuType<?>> MENU_KEY = ResourceKey.create(Registries.MENU,
			Patchwork.id("woodcutter"));

	public static final Block BLOCK = Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEY,
			new WoodcutterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).sound(SoundType.WOOD)
					.setId(BLOCK_KEY)));
	public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
			new BlockItem(BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()));
	public static final MenuType<WoodcutterMenu> MENU_TYPE = Registry.register(BuiltInRegistries.MENU, MENU_KEY,
			new MenuType<>((containerId, inventory) -> new WoodcutterMenu(containerId, inventory),
					FeatureFlags.VANILLA_SET));

	private Woodcutter() {
	}

	public static void register() {
		Item.BY_BLOCK.put(BLOCK, ITEM);
	}
}
