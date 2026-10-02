package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.HoglinLaunchTarget;
import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hoglin.class)
public class HoglinChargeMixin {
	@Inject(method = "doHurtTarget", at = @At("RETURN"))
	private void patchwork$launch(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
		Hoglin hoglin = (Hoglin)(Object)this;
		if (!PatchworkConfig.settings().hoglinCharge() || !cir.getReturnValue() || !hoglin.isAdult()
			|| !(target instanceof LivingEntity living)
			|| !(hoglin.isSprinting() || hoglin.getDeltaMovement().horizontalDistanceSqr() >= 0.0324)) {
			return;
		}
		Vec3 away = living.position().subtract(hoglin.position()).multiply(1.0, 0.0, 1.0);
		if (away.lengthSqr() < 0.001) {
			away = hoglin.getLookAngle().multiply(1.0, 0.0, 1.0);
		}
		Vec3 velocity = away.normalize().scale(0.2);
		living.setDeltaMovement(velocity.x, 0.69, velocity.z);
		living.syncVelocity = true;
		if (living instanceof ServerPlayer player) {
			((HoglinLaunchTarget)player).patchwork$trackHoglinLaunch();
		}
	}
}
