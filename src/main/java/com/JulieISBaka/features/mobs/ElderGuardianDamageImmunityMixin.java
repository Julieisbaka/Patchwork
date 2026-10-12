package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class ElderGuardianDamageImmunityMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void patchwork$ignoreElderGuardianDamage(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ElderGuardian && (source.is(DamageTypeTags.IS_EXPLOSION)
				|| source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.HOT_FLOOR))) {
			cir.setReturnValue(false);
		}
	}
}
