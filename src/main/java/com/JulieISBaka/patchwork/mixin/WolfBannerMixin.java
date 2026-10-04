package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfBannerMixin {
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void patchwork$interactWithBanner(Player player, InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir) {
		Wolf wolf = (Wolf) (Object) this;
		if (!PatchworkConfig.settings().wolfBanners() || !wolf.isTame() || !wolf.isOwnedBy(player)) {
			return;
		}

		ItemStack held = player.getItemInHand(hand);
		ItemStack equipped = wolf.getItemBySlot(EquipmentSlot.HEAD);
		boolean hasBanner = equipped.getItem() instanceof BannerItem;
		if (held.getItem() instanceof BannerItem && (equipped.isEmpty() || hasBanner)) {
			if (wolf.level() instanceof ServerLevel level) {
				wolf.setItemSlot(EquipmentSlot.HEAD, held.copyWithCount(1));
				wolf.setGuaranteedDrop(EquipmentSlot.HEAD);
				held.consume(1, player);
				if (hasBanner) {
					patchwork$returnBanner(player, level, equipped);
				}
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
		} else if (hasBanner && held.isEmpty() && player.isSecondaryUseActive()) {
			if (wolf.level() instanceof ServerLevel level) {
				wolf.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
				patchwork$returnBanner(player, level, equipped);
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
		}
	}

	private static void patchwork$returnBanner(Player player, ServerLevel level, ItemStack banner) {
		if (!player.addItem(banner) && !banner.isEmpty()) {
			player.spawnAtLocation(level, banner);
		}
	}
}
