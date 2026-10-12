package com.JulieISBaka.patchwork.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilRenameRepairCostMixin {
	@Shadow
	private boolean onlyRenaming;

	@Inject(method = "createResult", at = @At("TAIL"))
	private void patchwork$preserveRepairCostOnRename(CallbackInfo ci) {
		if (!onlyRenaming) {
			return;
		}
		AnvilMenu menu = (AnvilMenu) (Object) this;
		ItemStack input = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
		ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		if (!input.isEmpty() && !result.isEmpty()) {
			result.set(DataComponents.REPAIR_COST, input.getOrDefault(DataComponents.REPAIR_COST, 0));
		}
	}
}
