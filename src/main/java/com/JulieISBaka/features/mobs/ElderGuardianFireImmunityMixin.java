package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class ElderGuardianFireImmunityMixin {
	@Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
	private void patchwork$makeElderGuardiansFireproof(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ElderGuardian) {
			cir.setReturnValue(true);
		}
	}
}
