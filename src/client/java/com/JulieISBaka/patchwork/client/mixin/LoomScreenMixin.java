package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.BannerCustomization;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoomScreen.class)
public class LoomScreenMixin {
	@ModifyConstant(method = "containerChanged", constant = @Constant(intValue = 6))
	private int patchwork$patternLimit(int original) {
		return BannerCustomization.MAX_PATTERNS;
	}
}
