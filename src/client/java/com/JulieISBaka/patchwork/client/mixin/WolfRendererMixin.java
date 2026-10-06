package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.client.WolfBannerState;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void patchwork$extractBanner(Wolf wolf, WolfRenderState state, float partialTicks, CallbackInfo ci) {
		ItemStack headItem = wolf.getItemBySlot(EquipmentSlot.HEAD);
		((WolfBannerState) state)
				.patchwork$setBanner(headItem.getItem() instanceof BannerItem ? headItem.copy() : ItemStack.EMPTY);
	}
}
