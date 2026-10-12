package com.JulieISBaka.patchwork.mixins.items;

import com.JulieISBaka.patchwork.features.items.BannerCustomization;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


@Mixin(BannerDuplicateRecipe.class)
public class BannerDuplicateRecipeMixin {
	@ModifyConstant(method = {
			"matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
			"assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;" }, constant = @Constant(intValue = 6))
	private int patchwork$copyPatternLimit(int original) {
		return BannerCustomization.MAX_PATTERNS;
	}
}
