package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SoulGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IronGolem.class)
public class SoulGolemDamageMixin {
	@ModifyArg(method = "doHurtTarget",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
		index = 2)
	private float patchwork$quarterSoulDamage(float damage) {
		return (Object)this instanceof SoulGolem ? damage * 0.25F : damage;
	}
}
