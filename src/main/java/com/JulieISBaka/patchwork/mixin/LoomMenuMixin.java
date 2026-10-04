package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.BannerCustomization;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomMenu.class)
public class LoomMenuMixin {
	@ModifyConstant(method = "slotsChanged", constant = @Constant(intValue = 6))
	private int patchwork$patternLimit(int original) {
		return BannerCustomization.MAX_PATTERNS;
	}

	@Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true)
	private void patchwork$rejectExtraPattern(Holder<BannerPattern> pattern, CallbackInfo ci) {
		LoomMenu menu = (LoomMenu)(Object)this;
		if (menu.getBannerSlot().getItem().getOrDefault(DataComponents.BANNER_PATTERNS,
			BannerPatternLayers.EMPTY).layers().size() >= BannerCustomization.MAX_PATTERNS) {
			menu.getResultSlot().set(ItemStack.EMPTY);
			ci.cancel();
		}
	}
}
