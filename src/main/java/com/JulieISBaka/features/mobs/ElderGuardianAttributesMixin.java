package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ElderGuardian.class)
public abstract class ElderGuardianAttributesMixin {
	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void patchwork$makeElderGuardianABoss(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		cir.getReturnValue().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.ARMOR, 8.0)
				.add(Attributes.MOVEMENT_SPEED, 0.33);
	}
}
