package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.HoglinLaunchTarget;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class HoglinMovementMixin {
	@Inject(method = "move", at = @At("RETURN"))
	private void patchwork$impact(MoverType moverType, Vec3 movement, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			((HoglinLaunchTarget) player).patchwork$checkHoglinImpact();
		}
	}
}
