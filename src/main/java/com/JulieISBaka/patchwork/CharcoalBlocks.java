package com.JulieISBaka.patchwork;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public final class CharcoalBlocks {
	private static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK,
			Patchwork.id("charcoal_block"));
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM,
			Patchwork.id("charcoal_block"));
	public static final Block BLOCK = Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEY,
			new Block(BlockBehaviour.Properties.ofLegacyCopy(Blocks.COAL_BLOCK).setId(BLOCK_KEY)));
	public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
			new BlockItem(BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()
					.cookingFuel(ContextIntProviders.COOKING_TIME_COAL_BLOCK)));

	private CharcoalBlocks() {
	}

	public static void register() {
		Item.BY_BLOCK.put(BLOCK, ITEM);
	}
}
