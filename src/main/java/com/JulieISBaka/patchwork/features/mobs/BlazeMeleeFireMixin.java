package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Mob.class)
public abstract class BlazeMeleeFireMixin {
	@Inject(method = "doHurtTarget", at = @At("RETURN"))
	private void patchwork$igniteTargets(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Blaze && cir.getReturnValue() && target instanceof net.minecraft.world.entity.LivingEntity living) {
			living.igniteForSeconds(4.0F);
		}
	}
}
