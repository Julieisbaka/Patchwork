package com.JulieISBaka.patchwork.mixins.mobs;

import com.JulieISBaka.patchwork.features.mobs.EnderDragonBehavior;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(EndCrystal.class)
public abstract class EnderDragonCrystalMixin {
	@ModifyArg(method = "hurtServer", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"), index = 6)
	private float patchwork$increaseNaturalCrystalExplosion(float power) {
		EndCrystal crystal = (EndCrystal) (Object) this;
		return crystal.getTags().contains(EnderDragonBehavior.NATURAL_CRYSTAL_TAG) ? power * 1.5F : power;
	}
}
