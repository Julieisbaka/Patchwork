package com.JulieISBaka.patchwork.features.combat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AbstractHurtingProjectile.class)
public abstract class WitherSkullHomingMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$homeBlueWitherSkull(CallbackInfo ci) {
		AbstractHurtingProjectile projectile = (AbstractHurtingProjectile) (Object) this;
		if (projectile instanceof WitherSkull skull && projectile.level() instanceof ServerLevel level) {
			WitherSkullHoming.steerTowardPlayer(skull, level);
		}
	}
}
