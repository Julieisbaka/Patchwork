package com.JulieISBaka.patchwork.mixins.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class BlazeHealingMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void patchwork$healFromFireAttacks(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Blaze blaze && source.is(DamageTypeTags.IS_FIRE)
				&& !source.is(DamageTypes.LAVA) && !source.is(DamageTypes.HOT_FLOOR)) {
			blaze.heal(Math.max(1.0F, amount));
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
	private void patchwork$rejectHarmfulEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Blaze
				&& (effect.is(MobEffects.POISON) || effect.is(MobEffects.WITHER) || effect.is(MobEffects.HUNGER))) {
			cir.setReturnValue(false);
		}
	}
}
