package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShieldKnockbackMixin {
	@Inject(method = "blockedByItem", at = @At("HEAD"), cancellable = true)
	private void patchwork$avoidVanillaBlockKnockback(LivingEntity attacker, DamageSource source, float damage,
			boolean fullyBlocked, CallbackInfo ci) {
		LivingEntity defender = (LivingEntity) (Object) this;
		if (defender.getItemBlockingWith() != null) {
			ci.cancel();
		}
	}

	@Inject(method = "applyItemBlocking", at = @At("RETURN"))
	private void patchwork$knockBackAttackerOnShieldBlock(net.minecraft.server.level.ServerLevel level,
			DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
		LivingEntity defender = (LivingEntity) (Object) this;
		ItemStack blockingItem = defender.getItemBlockingWith();
		if (cir.getReturnValue() <= 0.0F || blockingItem == null
				|| !(source.getDirectEntity() instanceof LivingEntity attacker)) {
			return;
		}

		attacker.knockback(0.5, attacker.getX() - defender.getX(), attacker.getZ() - defender.getZ(), source, damage);
	}

	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void patchwork$interruptItemUseWhenDamaged(net.minecraft.server.level.ServerLevel level,
			DamageSource source, float damage,
			org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (cir.getReturnValue() && entity.isUsingItem() && entity.getItemBlockingWith() == null) {
			entity.stopUsingItem();
		}
	}
}
