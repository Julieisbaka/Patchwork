package com.JulieISBaka.patchwork;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class UnlitLanterns {
	private static final Map<Block, Block> UNLIT = new LinkedHashMap<>();
	public static final Block LANTERN = create(Blocks.LANTERN);
	public static final Block SOUL_LANTERN = create(Blocks.SOUL_LANTERN);
	public static final WeatheringCopperCollection<Block> COPPER_LANTERN = Blocks.COPPER_LANTERN
			.map(UnlitLanterns::create);

	private UnlitLanterns() {
	}

	public static void register() {
		OxidizableBlocksRegistry.registerWeatheringCopperBlocks(COPPER_LANTERN);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
				.register(output -> UNLIT.forEach((lit, unlit) -> output.insertAfter(lit.asItem(), unlit.asItem())));
	}

	public static BlockState extinguish(BlockState lit) {
		Block unlit = UNLIT.get(lit.getBlock());
		return unlit == null ? null : unlit.withPropertiesOf(lit);
	}

	private static Block create(Block lit) {
		String name = "unlit_" + BuiltInRegistries.BLOCK.getKey(lit).getPath();
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(lit).lightLevel(state -> 0)
				.setId(blockKey);
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey,
				lit instanceof WeatheringCopper copper
						? new UnlitWeatheringLanternBlock(lit, copper.getAge(), properties)
						: new UnlitLanternBlock(lit, properties));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Patchwork.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		Item.BY_BLOCK.put(block, item);
		UNLIT.put(lit, block);
		return block;
	}

	private static class UnlitLanternBlock extends LanternBlock {
		private final Block lit;

		UnlitLanternBlock(Block lit, BlockBehaviour.Properties properties) {
			super(properties);
			this.lit = lit;
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
				Player player, InteractionHand hand, BlockHitResult hit) {
			if (!stack.is(Items.FIRE_CHARGE) || state.getValue(WATERLOGGED)) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(pos, lit.withPropertiesOf(state));
				stack.consume(1, player);
				Patchwork.awardAdvancement(player, "adventure/let_there_be_light", "relight_with_fire_charge");
				level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			}
			return InteractionResult.SUCCESS;
		}
	}

	private static final class UnlitWeatheringLanternBlock extends UnlitLanternBlock implements WeatheringCopper {
		private final WeatherState age;

		UnlitWeatheringLanternBlock(Block lit, WeatherState age, BlockBehaviour.Properties properties) {
			super(lit, properties);
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
