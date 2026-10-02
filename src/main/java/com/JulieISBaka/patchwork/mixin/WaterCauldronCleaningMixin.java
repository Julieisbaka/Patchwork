package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronInteraction.Dispatcher.class)
public class WaterCauldronCleaningMixin {
	private static final Map<Item, Item> PATCHWORK_CLEANED_ITEMS = createCleanedItems();

	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private void patchwork$cleanDyedBlocks(ItemStack stack, CallbackInfoReturnable<CauldronInteraction> cir) {
		if ((Object)this != CauldronInteractions.WATER) {
			return;
		}
		Item cleanItem = PATCHWORK_CLEANED_ITEMS.get(stack.getItem());
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

	private static Map<Item, Item> createCleanedItems() {
		Map<Item, Item> items = new HashMap<>();
		Items.WOOL.forEach(wool -> {
			if (wool != Items.WOOL.white()) {
				items.put(wool, Items.WOOL.white());
			}
		});
		Items.DYED_TERRACOTTA.forEach(terracotta -> items.put(terracotta, Items.TERRACOTTA));
		Items.STAINED_GLASS.forEach(glass -> items.put(glass, Items.GLASS));
		return Map.copyOf(items);
	}
}
