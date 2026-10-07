package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LoyalTridentVoidMixin {
	@Inject(method = "checkBelowWorld", at = @At("HEAD"), cancellable = true)
	private void patchwork$returnLoyalTridentFromVoid(CallbackInfo ci) {
		if (!PatchworkConfig.settings().loyalTridentVoidReturn()
				|| !((Object) this instanceof ThrownTrident trident) || trident.level().isClientSide()
				|| !(trident.getOwner() instanceof Player)
				|| EnchantmentHelper.getItemEnchantmentLevel(
						trident.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
								.getOrThrow(Enchantments.LOYALTY),
						trident.getPickupItemStackOrigin()) <= 0) {
			return;
		}

		((AbstractArrowAccessor) trident).patchwork$setInGroundTime(5);
		ci.cancel();
	}
}
