package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.BannerCustomization;
import net.minecraft.world.inventory.LoomMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoomMenu.class)
public class LoomMenuMixin {
	@ModifyConstant(method = "slotsChanged", constant = @Constant(intValue = 6))
	private int patchwork$patternLimit(int original) {
		return BannerCustomization.MAX_PATTERNS;
	}
}
