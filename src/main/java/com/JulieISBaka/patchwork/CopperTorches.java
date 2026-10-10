package com.JulieISBaka.patchwork;

import java.util.Optional;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;

public final class CopperTorches {
	public static final WeatheringCopperCollection<Block> LIT = createCollection(false, false);
	public static final WeatheringCopperCollection<Block> LIT_WALL = createCollection(false, true);
	public static final WeatheringCopperCollection<Block> UNLIT = createCollection(true, false);
	public static final WeatheringCopperCollection<Block> UNLIT_WALL = createCollection(true, true);

	static {
		WeatheringCopperCollection.zipApply(LIT, LIT_WALL, CopperTorches::registerItem);
		WeatheringCopperCollection.zipApply(UNLIT, UNLIT_WALL, CopperTorches::registerItem);
	}

	private CopperTorches() {
	}

	public static int lightLevel(WeatherState age) {
		return switch (age) {
			case UNAFFECTED -> 14;
			case EXPOSED -> 12;
			case WEATHERED -> 10;
			case OXIDIZED -> 8;
		};
	}

	public static void register() {
		for (var collection : java.util.List.of(LIT, LIT_WALL, UNLIT, UNLIT_WALL)) {
			OxidizableBlocksRegistry.registerWeatheringCopperBlocks(collection);
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
			if (PatchworkConfig.settings().soulCopperLightBlocks()) {
				for (Block block : LIT.asList()) {
					if (block != Blocks.COPPER_TORCH) {
						output.accept(block.asItem());
					}
				}
			}
			if (PatchworkConfig.settings().unlitLights()) {
				for (Block block : UNLIT.asList()) {
					if (block != UNLIT.weathering().unaffected()) {
						output.accept(block.asItem());
					}
				}
			}
		});
	}

	public static BlockState extinguish(BlockState state) {
		if (!PatchworkConfig.settings().unlitLights()) {
			return null;
		}
		Block unlit = counterpart(state.getBlock(), LIT, UNLIT);
		if (unlit == null) {
			unlit = counterpart(state.getBlock(), LIT_WALL, UNLIT_WALL);
		}
		return unlit == null ? null : unlit.withPropertiesOf(state);
	}

	private static Block counterpart(Block block, WeatheringCopperCollection<Block> source,
			WeatheringCopperCollection<Block> target) {
		int index = source.asList().indexOf(block);
		return index < 0 ? null : target.asList().get(index);
	}

	private static BlockState relit(BlockState state, boolean wall) {
		Block lit = counterpart(state.getBlock(), wall ? UNLIT_WALL : UNLIT, wall ? LIT_WALL : LIT);
		if (lit == null) {
			throw new IllegalStateException("Unknown unlit copper torch: " + state);
		}
		return lit.withPropertiesOf(state);
	}

	private static WeatheringCopperCollection<Block> createCollection(boolean unlit, boolean wall) {
		var names = WeatheringCopperCollection
				.prefixWithState(WeatheringCopperCollection.create(wall ? "copper_wall_torch" : "copper_torch"));
		var ages = new WeatheringCopperCollection<>(WeatheringCopperCollection.STATES,
				WeatheringCopperCollection.STATES);
		return WeatheringCopperCollection.zipMap(names, ages, (name, age) -> {
			if (!unlit && name.equals("copper_torch")) {
				return Blocks.COPPER_TORCH;
			}
			if (!unlit && name.equals("copper_wall_torch")) {
				return Blocks.COPPER_WALL_TORCH;
			}
			String id = (unlit ? "unlit_" : "") + name;
			ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Patchwork.id(id));
			var properties = BlockBehaviour.Properties
					.ofFullCopy(wall ? Blocks.COPPER_WALL_TORCH : Blocks.COPPER_TORCH)
					.lightLevel(state -> unlit ? 0 : lightLevel(age))
					.overrideDescription(key.identifier().toLanguageKey("block"))
					.setId(key);
			if (wall) {
				ResourceKey<LootTable> loot = ResourceKey.create(Registries.LOOT_TABLE,
						Patchwork.id("blocks/" + id.replace("_wall_torch", "_torch")));
				properties.overrideLootTable(Optional.of(loot));
			}
			boolean waxed = name.startsWith("waxed_");
			if (!waxed) {
				properties.randomTicks();
			}
			Block block = wall
					? (waxed ? new CopperWallTorchBlock(unlit, properties)
							: new WeatheringWallTorchBlock(age, unlit, properties))
					: (waxed ? new CopperTorchBlock(unlit, properties)
							: new WeatheringTorchBlock(age, unlit, properties));
			return Registry.register(BuiltInRegistries.BLOCK, key, block);
		});
	}

	private static void registerItem(Block standing, Block wall) {
		if (standing == Blocks.COPPER_TORCH) {
			return;
		}
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(standing));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, new StandingAndWallBlockItem(standing, wall,
				Direction.DOWN, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
		Item.BY_BLOCK.put(standing, item);
		Item.BY_BLOCK.put(wall, item);
	}

	private static class CopperTorchBlock extends TorchBlock {
		private final boolean unlit;

		CopperTorchBlock(boolean unlit, BlockBehaviour.Properties properties) {
			super(ParticleTypes.COPPER_FIRE_FLAME, properties);
			this.unlit = unlit;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			if (!unlit) {
				super.animateTick(state, level, pos, random);
			}
		}

		@Override
		protected boolean isRandomlyTicking(BlockState state) {
			return false;
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
				Player player, InteractionHand hand, BlockHitResult hit) {
			return unlit ? UnlitTorches.relight(stack, level, pos, player, hand, relit(state, false))
					: super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
	}

	private static class CopperWallTorchBlock extends WallTorchBlock {
		private final boolean unlit;

		CopperWallTorchBlock(boolean unlit, BlockBehaviour.Properties properties) {
			super(ParticleTypes.COPPER_FIRE_FLAME, properties);
			this.unlit = unlit;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			if (!unlit) {
				super.animateTick(state, level, pos, random);
			}
		}

		@Override
		protected boolean isRandomlyTicking(BlockState state) {
			return false;
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
				Player player, InteractionHand hand, BlockHitResult hit) {
			return unlit ? UnlitTorches.relight(stack, level, pos, player, hand, relit(state, true))
					: super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
	}

	public static final class WeatheringTorchBlock extends CopperTorchBlock implements WeatheringCopper {
		private final WeatherState age;

		public WeatheringTorchBlock(WeatherState age, boolean unlit, BlockBehaviour.Properties properties) {
			super(unlit, properties);
			this.age = age;
		}

		@Override
		public WeatherState getAge() {
			return age;
		}

		@Override
		protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
			changeOverTime(state, level, pos, random);
		}

		@Override
		protected boolean isRandomlyTicking(BlockState state) {
			return age != WeatherState.OXIDIZED;
		}
	}

	public static final class WeatheringWallTorchBlock extends CopperWallTorchBlock implements WeatheringCopper {
		private final WeatherState age;

		public WeatheringWallTorchBlock(WeatherState age, boolean unlit, BlockBehaviour.Properties properties) {
			super(unlit, properties);
			this.age = age;
		}

		@Override
		public WeatherState getAge() {
			return age;
		}

		@Override
		protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
			changeOverTime(state, level, pos, random);
		}

		@Override
		protected boolean isRandomlyTicking(BlockState state) {
			return age != WeatherState.OXIDIZED;
		}
	}
}
