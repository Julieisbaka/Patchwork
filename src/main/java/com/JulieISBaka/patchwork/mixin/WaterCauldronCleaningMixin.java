package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.CauldronCleaningItems;
import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronInteraction.Dispatcher.class)
public class WaterCauldronCleaningMixin {
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private void patchwork$cleanDyedBlocks(ItemStack stack, CallbackInfoReturnable<CauldronInteraction> cir) {
		if ((Object)this != CauldronInteractions.WATER) {
			return;
		}
		Item cleanItem = CauldronCleaningItems.get(stack.getItem());
		if (cleanItem == null) {
			return;
		}
		cir.setReturnValue((state, level, pos, player, hand, held) -> {
			if (!level.isClientSide()) {
				if (!PatchworkConfig.settings().cauldronCleaning()) {
					return InteractionResult.TRY_WITH_EMPTY_HAND;
				}
				ItemStack cleaned = held.transmuteCopy(cleanItem, 1);
				player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, cleaned, false));
				player.awardStat(Stats.USE_CAULDRON);
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
