package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.SpiderCeiling;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpiderRenderer.class)
public abstract class SpiderRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/spider/Spider;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
			at = @At("TAIL"))
	private void patchwork$flipCeilingSpiders(Spider spider, LivingEntityRenderState state, float partialTick,
			CallbackInfo ci) {
		if (SpiderCeiling.isClingingToCeiling(spider)) {
			state.isUpsideDown = true;
		}
	}
}
