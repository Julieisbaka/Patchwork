package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.CauldronCleaningItems;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.PotionCauldrons;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronInteraction.Dispatcher.class)
public class WaterCauldronCleaningMixin {
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private void patchwork$cleanDyedBlocks(ItemStack stack, CallbackInfoReturnable<CauldronInteraction> cir) {
		if (PatchworkConfig.settings().potionCauldrons() && stack.is(Items.POTION)) {
			PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
			if (contents != null && !contents.equals(PotionContents.EMPTY) && !contents.is(Potions.WATER)) {
				if ((Object) this == CauldronInteractions.EMPTY) {
					cir.setReturnValue(PotionCauldrons::pour);
				} else if ((Object) this == CauldronInteractions.WATER) {
					cir.setReturnValue(
							(state, level, pos, player, hand, held) -> InteractionResult.TRY_WITH_EMPTY_HAND);
				}
			}
			return;
		}
		if ((Object) this != CauldronInteractions.WATER) {
			return;
		}
		Item cleanItem = CauldronCleaningItems.get(stack.getItem());
		boolean cleanBundle = stack.is(Items.BUNDLE) && stack.has(DataComponents.DYED_COLOR);
		if (cleanItem == null && !cleanBundle) {
			return;
		}
		cir.setReturnValue((state, level, pos, player, hand, held) -> {
			if (!level.isClientSide()) {
				if (!PatchworkConfig.settings().cauldronCleaning()) {
					return InteractionResult.TRY_WITH_EMPTY_HAND;
				}
				if (cleanBundle) {
					ItemStack cleaned = held.copy();
					cleaned.remove(DataComponents.DYED_COLOR);
					player.setItemInHand(hand, cleaned);
					Patchwork.awardAdvancement(player, "adventure/restored_to_color", "wash_dyed_bundle");
				} else {
					ItemStack cleaned = held.transmuteCopy(cleanItem, 1);
					player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, cleaned, false));
				}
				player.awardStat(Stats.USE_CAULDRON);
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
