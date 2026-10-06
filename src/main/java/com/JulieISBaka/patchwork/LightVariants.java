package com.JulieISBaka.patchwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public final class LightVariants {
	private static final List<Block> BLOCKS = new ArrayList<>();
	private static final List<Item> ITEMS = new ArrayList<>();
	private static final Map<Block, Item> UNLIT_ITEMS = new LinkedHashMap<>();
	public static final CandleBlock SOUL_CANDLE = registerCandle("soul_candle", null, false);
	public static final WeatheringCopperCollection<Block> COPPER_CANDLES = copperFamily("copper_candle",
			LightVariants::registerCandle);
	public static final WeatheringCopperCollection<Block> COPPER_CANDLE_CAKES = createCakes();
	public static final WeatheringCopperCollection<Block> COPPER_CAMPFIRES = copperFamily("copper_campfire",
			LightVariants::registerCampfire);
	public static final WeatheringCopperCollection<Block> COPPER_PUMPKINS = copperFamily("copper_jack_o_lantern",
			LightVariants::registerPumpkin);
	public static final Block SOUL_CANDLE_CAKE = registerCake("soul_candle_cake", SOUL_CANDLE, null, false);

	private LightVariants() {
	}

	public static void register() {
		for (var family : List.of(COPPER_CANDLES, COPPER_CANDLE_CAKES, COPPER_CAMPFIRES, COPPER_PUMPKINS)) {
			OxidizableBlocksRegistry.registerWeatheringCopperBlocks(family);
		}
		for (var block : COPPER_CAMPFIRES.asList()) {
			BlockEntityTypes.CAMPFIRE.addValidBlock(block);
		}
		registerUnlitItem(Blocks.CANDLE);
		for (DyeColor color : DyeColor.values()) {
			registerUnlitItem(BuiltInRegistries.BLOCK.getValue(
					Identifier.withDefaultNamespace(color.getName() + "_candle")));
		}
		registerUnlitItem(SOUL_CANDLE);
		COPPER_CANDLES.forEach(LightVariants::registerUnlitItem);
		registerUnlitItem(Blocks.CAMPFIRE);
		registerUnlitItem(Blocks.SOUL_CAMPFIRE);
		COPPER_CAMPFIRES.forEach(LightVariants::registerUnlitItem);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
				.register(output -> ITEMS.forEach(output::accept));
	}

	public static List<Block> blocks() {
		return List.copyOf(BLOCKS);
	}

	public static Map<Block, Item> unlitItems() {
		return Map.copyOf(UNLIT_ITEMS);
	}

	public static boolean isUnlitItemFor(ItemStack stack, Block block) {
		var item = UNLIT_ITEMS.get(block);
		return item != null && stack.is(item);
	}

	public static Item unlitItem(BlockState state) {
		return state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)
				? UNLIT_ITEMS.get(state.getBlock()) : null;
	}

	public static Block copperPumpkinFor(Item item) {
		int index = CopperTorches.LIT.asList().indexOf(Block.byItem(item));
		return index < 0 ? null : COPPER_PUMPKINS.asList().get(index);
	}

	private interface CopperFactory {
		Block create(String name, WeatherState age, boolean waxed);
	}

	private static WeatheringCopperCollection<Block> copperFamily(String base, CopperFactory factory) {
		var names = WeatheringCopperCollection.prefixWithState(WeatheringCopperCollection.create(base));
		var ages = new WeatheringCopperCollection<>(WeatheringCopperCollection.STATES,
				WeatheringCopperCollection.STATES);
		return WeatheringCopperCollection.zipMap(names, ages,
				(name, age) -> factory.create(name, age, name.startsWith("waxed_")));
	}

	private static WeatheringCopperCollection<Block> createCakes() {
		return copperFamily("copper_candle_cake", (name, age, waxed) -> registerCake(name,
				(CandleBlock) (waxed ? COPPER_CANDLES.waxed() : COPPER_CANDLES.weathering()).pick(age), age, waxed));
	}

	private static BlockBehaviour.Properties properties(String name, Block reference, WeatherState age, boolean waxed) {
		var key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		var properties = BlockBehaviour.Properties.ofFullCopy(reference).setId(key)
				.overrideDescription(key.identifier().toLanguageKey("block"))
				.overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, Patchwork.id("blocks/" + name))));
		if (age != null && !waxed) {
			properties.randomTicks();
		}
		return properties;
	}

	private static <T extends Block> T registerBlock(String name, T block, boolean item) {
		Registry.register(BuiltInRegistries.BLOCK, Patchwork.id(name), block);
		BLOCKS.add(block);
		if (item) {
			var key = ResourceKey.create(Registries.ITEM, Patchwork.id(name));
			var blockItem = Registry.register(BuiltInRegistries.ITEM, key,
					new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
			Item.BY_BLOCK.put(block, blockItem);
			ITEMS.add(blockItem);
		}
		return block;
	}

	private static CandleBlock registerCandle(String name, WeatherState age, boolean waxed) {
		var properties = properties(name, Blocks.CANDLE, age, waxed)
				.lightLevel(state -> state.getValue(CandleBlock.LIT) ? age == null ? 10 : CopperTorches.lightLevel(age) : 0);
		return registerBlock(name, age != null && !waxed
				? new WeatheringCandle(age, properties) : new VariantCandle(age == null, properties), true);
	}

	private static Block registerCake(String name, CandleBlock candle, WeatherState age, boolean waxed) {
		var properties = properties(name, Blocks.CANDLE_CAKE, age, waxed)
				.lightLevel(state -> state.getValue(CandleCakeBlock.LIT) ? age == null ? 10 : CopperTorches.lightLevel(age) : 0);
		return registerBlock(name, age != null && !waxed
				? new WeatheringCake(candle, age, properties) : new VariantCake(candle, age == null, properties), false);
	}

	private static Block registerCampfire(String name, WeatherState age, boolean waxed) {
		var properties = properties(name, Blocks.CAMPFIRE, age, waxed)
				.lightLevel(state -> state.getValue(CampfireBlock.LIT) ? CopperTorches.lightLevel(age) : 0);
		return registerBlock(name, waxed ? new CopperCampfire(properties)
				: new WeatheringCampfire(age, properties), true);
	}

	private static Block registerPumpkin(String name, WeatherState age, boolean waxed) {
		var properties = properties(name, Blocks.JACK_O_LANTERN, age, waxed)
				.lightLevel(state -> CopperTorches.lightLevel(age));
		return registerBlock(name, waxed ? new CarvedPumpkinBlock(properties)
				: new WeatheringPumpkin(age, properties), true);
	}

	private static void registerUnlitItem(Block block) {
		String name = "unlit_" + BuiltInRegistries.BLOCK.getKey(block).getPath();
		var key = ResourceKey.create(Registries.ITEM, Patchwork.id(name));
		var item = Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key)) {
					@Override
					public void registerBlocks(Map<Block, Item> map, Item item) {
						map.putIfAbsent(block, item);
					}

					@Override
					protected BlockState getPlacementState(BlockPlaceContext context) {
						var state = super.getPlacementState(context);
						return state == null ? null : state.setValue(BlockStateProperties.LIT, false);
					}
				});
		UNLIT_ITEMS.put(block, item);
		ITEMS.add(item);
	}

	private static void candleParticles(BlockState state, Level level, BlockPos pos, RandomSource random,
			Iterable<Vec3> offsets, boolean soul) {
		if (!state.getValue(BlockStateProperties.LIT)) {
			return;
		}
		SimpleParticleType flame = soul ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COPPER_FIRE_FLAME;
		for (Vec3 offset : offsets) {
			Vec3 location = offset.add(pos.getX(), pos.getY(), pos.getZ());
			if (random.nextFloat() < 0.3F) {
				level.addParticle(ParticleTypes.SMOKE, location.x, location.y, location.z, 0, 0, 0);
				if (random.nextFloat() < 0.17F) {
					level.playLocalSound(location.x, location.y, location.z, SoundEvents.CANDLE_AMBIENT,
							SoundSource.BLOCKS, 1.0F, 0.7F + random.nextFloat() * 0.3F, false);
				}
			}
			level.addParticle(flame, location.x, location.y, location.z, 0, 0, 0);
		}
	}

	private static class VariantCandle extends CandleBlock {
		private final boolean soul;

		VariantCandle(boolean soul, BlockBehaviour.Properties properties) {
			super(properties);
			this.soul = soul;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			candleParticles(state, level, pos, random, getParticleOffsets(state), soul);
		}
	}

	private static final class WeatheringCandle extends VariantCandle implements WeatheringCopper {
		private final WeatherState age;

		WeatheringCandle(WeatherState age, BlockBehaviour.Properties properties) {
			super(false, properties);
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

	private static class VariantCake extends CandleCakeBlock {
		private final boolean soul;

		VariantCake(CandleBlock candle, boolean soul, BlockBehaviour.Properties properties) {
			super(candle, properties);
			this.soul = soul;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
			candleParticles(state, level, pos, random, getParticleOffsets(state), soul);
		}
	}

	private static final class WeatheringCake extends VariantCake implements WeatheringCopper {
		private final WeatherState age;

		WeatheringCake(CandleBlock candle, WeatherState age, BlockBehaviour.Properties properties) {
			super(candle, false, properties);
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

	private static class CopperCampfire extends CampfireBlock {
		CopperCampfire(BlockBehaviour.Properties properties) {
			super(false, 1, properties);
		}

		@Override
		protected boolean shouldChangedStateKeepBlockEntity(BlockState oldState) {
			return oldState.getBlock() instanceof CopperCampfire;
		}
	}

	private static final class WeatheringCampfire extends CopperCampfire implements WeatheringCopper {
		private final WeatherState age;

		WeatheringCampfire(WeatherState age, BlockBehaviour.Properties properties) {
			super(properties);
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

	private static final class WeatheringPumpkin extends CarvedPumpkinBlock implements WeatheringCopper {
		private final WeatherState age;

		WeatheringPumpkin(WeatherState age, BlockBehaviour.Properties properties) {
			super(properties);
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
