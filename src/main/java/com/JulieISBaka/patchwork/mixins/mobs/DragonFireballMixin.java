package com.JulieISBaka.patchwork.mixins.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DragonFireball.class)
public abstract class DragonFireballMixin {
	@Inject(method = "onHit", at = @At("HEAD"))
	private void patchwork$damageDirectlyOnHit(HitResult result, CallbackInfo ci) {
		if (!(result instanceof EntityHitResult entityHit)
				|| !(entityHit.getEntity() instanceof LivingEntity target)
				|| !(((DragonFireball) (Object) this).level() instanceof ServerLevel level)) {
			return;
		}

		DragonFireball fireball = (DragonFireball) (Object) this;
		if (fireball.getOwner() != target) {
			target.hurtServer(level, level.damageSources().indirectMagic(fireball, fireball.getOwner()), 6.0F);
		}
	}
}
