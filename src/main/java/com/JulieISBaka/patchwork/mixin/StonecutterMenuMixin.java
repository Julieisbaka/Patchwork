package com.JulieISBaka.patchwork.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.WoodcutterMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StonecutterMenu.class)
public abstract class StonecutterMenuMixin {
	@ModifyExpressionValue(method = "setupRecipeList",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/SelectableRecipe$SingleInputSet;selectByInput(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/SelectableRecipe$SingleInputSet;"))
	private SelectableRecipe.SingleInputSet<StonecutterRecipe> patchwork$filterMachineRecipes(
			SelectableRecipe.SingleInputSet<StonecutterRecipe> recipes) {
		boolean woodcutter = (Object) this instanceof WoodcutterMenu;
		return new SelectableRecipe.SingleInputSet<>(recipes.entries().stream()
				.filter(entry -> entry.recipe().recipe().map(holder ->
						holder.id().identifier().getPath().startsWith("woodcutter/") == woodcutter).orElse(false))
				.toList());
	}
}
