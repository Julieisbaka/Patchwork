package com.JulieISBaka.patchwork.mixins.mobs;

import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.Pillager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(RangedCrossbowAttackGoal.class)
public abstract class PillagerCrossbowRangeMixin {
	@Shadow
	@Final
	@Mutable
	private float attackRadiusSqr;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$extendPillagerCrossbowRange(Monster mob, double speed, float attackRadius,
			CallbackInfo ci) {
		if (mob instanceof Pillager) {
			this.attackRadiusSqr = 15.0F * 15.0F;
		}
	}
}
