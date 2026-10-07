package com.JulieISBaka.patchwork;

import java.util.function.Function;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public final class GardenBlocks {
	public static final Block WAX_BLOCK = block("wax_block", Blocks.HONEYCOMB_BLOCK, Block::new);
	public static final FlowerBlock PAEONIA = block("paeonia", Blocks.ALLIUM,
			properties -> new FlowerBlock(((FlowerBlock) Blocks.ALLIUM).getSuspiciousEffects(), properties));
	public static final FlowerPotBlock POTTED_PAEONIA = block("potted_paeonia", Blocks.POTTED_ALLIUM,
			properties -> new FlowerPotBlock(PAEONIA, properties));
	public static final FlowerPotBlock POTTED_CACTUS_FLOWER = potted("cactus_flower", Blocks.CACTUS_FLOWER);
	public static final FlowerPotBlock POTTED_ROSE_BUSH = potted("rose_bush", Blocks.ROSE_BUSH);
	public static final FlowerPotBlock POTTED_PEONY = potted("peony", Blocks.PEONY);
	public static final FlowerPotBlock POTTED_LILAC = potted("lilac", Blocks.LILAC);
	public static final FlowerPotBlock POTTED_SUNFLOWER = potted("sunflower", Blocks.SUNFLOWER);
	public static final FlowerPotBlock POTTED_PITCHER_PLANT = potted("pitcher_plant", Blocks.PITCHER_PLANT);
	public static final Item WAX_ITEM = item("wax_block", WAX_BLOCK, false);
	public static final Item PAEONIA_ITEM = item("paeonia", PAEONIA, true);
	public static final ResourceKey<PlacedFeature> PAEONIA_PATCH = ResourceKey.create(Registries.PLACED_FEATURE,
			Patchwork.id("paeonia_patch"));

	private GardenBlocks() {
	}

	private static <T extends Block> T block(String name, Block source,
			Function<BlockBehaviour.Properties, T> factory) {
		var key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key,
				factory.apply(BlockBehaviour.Properties.ofFullCopy(source).setId(key)));
	}

	private static Item item(String name, Block block, boolean compostable) {
		var key = ResourceKey.create(Registries.ITEM, Patchwork.id(name));
		var properties = new Item.Properties().setId(key).useBlockDescriptionPrefix();
		if (compostable) {
			properties.compostable(ContextIntProviders.COMPOSTABLE_MEDIUM);
		}
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, properties));
	}

	private static FlowerPotBlock potted(String name, Block flower) {
		return block("potted_" + name, Blocks.POTTED_ALLIUM,
				properties -> new FlowerPotBlock(flower, properties));
	}

	public static void register() {
		Item.BY_BLOCK.put(WAX_BLOCK, WAX_ITEM);
		Item.BY_BLOCK.put(PAEONIA, PAEONIA_ITEM);
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.FLOWER_FOREST, Biomes.MEADOW),
				GenerationStep.Decoration.VEGETAL_DECORATION, PAEONIA_PATCH);
	}
}
