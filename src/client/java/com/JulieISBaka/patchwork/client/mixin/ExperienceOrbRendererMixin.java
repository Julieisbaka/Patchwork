package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.client.ExperienceOrbState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbRenderer.class)
public class ExperienceOrbRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/ExperienceOrb;Lnet/minecraft/client/renderer/entity/state/ExperienceOrbRenderState;F)V",
		at = @At("TAIL"))
	private void patchwork$extractScale(ExperienceOrb orb, ExperienceOrbRenderState state, float partialTick,
		CallbackInfo ci) {
		((ExperienceOrbState)state).patchwork$setScale(
			1.0F + Math.min(2.0F, (float)Math.log1p(Math.max(0, orb.getValue() - 1)) / 4.0F));
	}

	@ModifyArgs(method = "submit(Lnet/minecraft/client/renderer/entity/state/ExperienceOrbRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
	private void patchwork$scaleOrb(Args args, ExperienceOrbRenderState state, PoseStack poses,
		SubmitNodeCollector collector, CameraRenderState camera) {
		float scale = ((ExperienceOrbState)state).patchwork$scale();
		for (int i = 0; i < 3; i++) {
			args.set(i, (float)args.get(i) * scale);
		}
	}
}
