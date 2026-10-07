package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.monster.creaking.Creaking.class)
public class CreakingAttributesMixin {
	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void patchwork$increaseAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		cir.getReturnValue().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.ATTACK_DAMAGE, 4.0);
	}
}
