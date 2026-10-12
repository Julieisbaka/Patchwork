package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemEntity.class)
public abstract class BlazeRodFireproofMixin {
	@Inject(method = "fireImmune", at = @At("RETURN"), cancellable = true)
	private void patchwork$blazeRodsResistFire(CallbackInfoReturnable<Boolean> cir) {
		if (((ItemEntity) (Object) this).getItem().is(Items.BLAZE_ROD)) {
			cir.setReturnValue(true);
		}
	}
}
