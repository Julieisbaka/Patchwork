package com.JulieISBaka.patchwork;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class PumpkinLanterns {
	private static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Patchwork.id("soul_jack_o_lantern"));
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, Patchwork.id("soul_jack_o_lantern"));
	public static final Block SOUL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEY,
		new CarvedPumpkinBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.JACK_O_LANTERN)
			.lightLevel(state -> 10).setId(BLOCK_KEY)));
	public static final Item SOUL_ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
		new BlockItem(SOUL_BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()) {
			@Override
			public InteractionResult place(BlockPlaceContext context) {
				return SoulGolems.withBuilder(context.getPlayer(), () -> super.place(context));
			}
		});

	private PumpkinLanterns() {
	}

	public static void register() {
		Item.BY_BLOCK.put(SOUL_BLOCK, SOUL_ITEM);
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!PatchworkConfig.settings().pumpkinLanterns()) {
				return InteractionResult.PASS;
			}
			BlockState state = level.getBlockState(hit.getBlockPos());
			if (!state.is(Blocks.CARVED_PUMPKIN)) {
				return InteractionResult.PASS;
			}
			ItemStack held = player.getItemInHand(hand);
			Block replacement = held.is(Items.TORCH) ? Blocks.JACK_O_LANTERN
				: held.is(Items.SOUL_TORCH) ? SOUL_BLOCK : null;
			if (replacement == null) {
				return InteractionResult.PASS;
			}
			if (level instanceof ServerLevel serverLevel) {
				SoulGolems.withBuilder(player, () -> serverLevel.setBlockAndUpdate(hit.getBlockPos(),
					replacement.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, state.getValue(CarvedPumpkinBlock.FACING))));
				serverLevel.playSound(null, hit.getBlockPos(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
				serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, hit.getBlockPos());
				held.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
