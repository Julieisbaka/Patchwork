package com.JulieISBaka.patchwork;

import java.util.Set;
import com.JulieISBaka.patchwork.mixin.PotionArrowItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public final class PotionCauldrons {
	public static final Block BLOCK = Registry.register(BuiltInRegistries.BLOCK,
		ResourceKey.create(Registries.BLOCK, Patchwork.id("potion_cauldron")),
		new PotionCauldronBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.WATER_CAULDRON)
			.overrideLootTable(Blocks.CAULDRON.getLootTable()).overrideDescription(Blocks.CAULDRON.getDescriptionId())
			.setId(ResourceKey.create(Registries.BLOCK, Patchwork.id("potion_cauldron")))));
	public static final BlockEntityType<PotionCauldronEntity> TYPE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Patchwork.id("potion_cauldron")),
		new BlockEntityType<>(PotionCauldronEntity::new, Set.of(BLOCK)));

	private PotionCauldrons() {
	}

	public static void register() {
	}

	public static InteractionResult pour(BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, ItemStack bottle) {
		PotionContents contents = bottle.get(DataComponents.POTION_CONTENTS);
		if (!state.is(Blocks.CAULDRON) || contents == null || contents.equals(PotionContents.EMPTY)
			|| contents.is(Potions.WATER)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			level.setBlockAndUpdate(pos, BLOCK.defaultBlockState());
			PotionCauldronEntity cauldron = entity(level, pos);
			cauldron.setPotion(contents);
			player.setItemInHand(hand, ItemUtils.createFilledResult(bottle, player, new ItemStack(Items.GLASS_BOTTLE)));
			player.awardStat(Stats.USE_CAULDRON);
			player.awardStat(Stats.ITEM_USED.get(Items.POTION));
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	public static boolean dip(ServerLevel level, BlockPos pos, ItemStack arrows, Player player, InteractionHand hand) {
		if (arrows.isEmpty() || !arrows.is(Items.ARROW)) {
			return false;
		}
		PotionCauldronEntity cauldron = entity(level, pos);
		if (cauldron.potion().equals(PotionContents.EMPTY)) {
			return false;
		}
		ItemStack tipped = new ItemStack(Items.TIPPED_ARROW);
		tipped.set(DataComponents.POTION_CONTENTS, cauldron.potion());
		arrows.shrink(1);
		LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);
		if (arrows.isEmpty()) {
			player.setItemInHand(hand, tipped);
		} else if (!player.addItem(tipped)) {
			player.spawnAtLocation(level, tipped);
		}
		return true;
	}

	public static void dipDropped(ServerLevel level, BlockPos pos, ItemEntity item) {
		BlockState state = level.getBlockState(pos);
		PotionContents contents = entity(level, pos).potion();
		if (contents.equals(PotionContents.EMPTY)) {
			return;
		}
		int count = Math.min(item.getItem().getCount(), state.getValue(LayeredCauldronBlock.LEVEL));
		ItemStack tipped = new ItemStack(Items.TIPPED_ARROW, count);
		tipped.set(DataComponents.POTION_CONTENTS, contents);
		int remaining = item.getItem().getCount() - count;
		if (remaining > 0) {
			ItemEntity remainder = new ItemEntity(level, item.getX(), item.getY(), item.getZ(),
				item.getItem().copyWithCount(remaining));
			PotionArrowItemAccessor original = (PotionArrowItemAccessor) item;
			PotionArrowItemAccessor split = (PotionArrowItemAccessor) remainder;
			split.patchwork$setAge(item.getAge());
			remainder.setPickUpDelay(original.patchwork$getPickupDelay());
			remainder.setTarget(original.patchwork$getTarget());
			split.patchwork$setThrower(original.patchwork$getThrower());
			remainder.setDeltaMovement(item.getDeltaMovement());
			if (!level.addFreshEntity(remainder)) {
				throw new IllegalStateException("Could not split dropped arrows at " + pos);
			}
		}
		// Keep the converted entity's identity, motion, and pickup timer instead of
		// respawning it.
		item.setItem(tipped);
		for (int i = 0; i < count; i++) {
			LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);
		}
	}

	private static PotionCauldronEntity entity(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PotionCauldronEntity cauldron) {
			return cauldron;
		}
		throw new IllegalStateException("Potion cauldron missing block entity at " + pos);
	}

	private static final class PotionCauldronBlock extends LayeredCauldronBlock implements EntityBlock {
		PotionCauldronBlock(BlockBehaviour.Properties properties) {
			super(Biome.Precipitation.NONE, net.minecraft.core.cauldron.CauldronInteractions.EMPTY, properties);
		}

		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new PotionCauldronEntity(pos, state);
		}

		@Override
		protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos,
			BlockState state, boolean includeData) {
			return new ItemStack(Items.CAULDRON);
		}

		@Override
		public void handlePrecipitation(BlockState state, Level level, BlockPos pos,
			Biome.Precipitation precipitation) {
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
			if (!PatchworkConfig.settings().potionCauldrons()) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
			if (stack.is(Items.ARROW)) {
				if (level instanceof ServerLevel serverLevel) {
					dip(serverLevel, pos, stack, player, hand);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.GLASS_BOTTLE)) {
				if (!level.isClientSide()) {
					ItemStack potion = new ItemStack(Items.POTION);
					potion.set(DataComponents.POTION_CONTENTS, entity(level, pos).potion());
					player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, potion));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
					LayeredCauldronBlock.lowerFillLevel(state, level, pos);
					level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.POTION)) {
				PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
				if (contents == null || contents.equals(PotionContents.EMPTY) || state.getValue(LEVEL) == 3
					|| !contents.equals(entity(level, pos).potion())) {
					return InteractionResult.TRY_WITH_EMPTY_HAND;
				}
				if (!level.isClientSide()) {
					level.setBlockAndUpdate(pos, state.cycle(LEVEL));
					player.setItemInHand(hand,
						ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(Items.POTION));
					level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
	}
}
