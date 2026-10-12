package com.JulieISBaka.patchwork.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchJumpPotionMixin {
	@ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"), index = 1)
	private Holder<Potion> patchwork$drinkJumpBoostWhenTargetIsHigh(Holder<Potion> potion) {
		Witch witch = (Witch) (Object) this;
		LivingEntity target = witch.getTarget();
		return potion == Potions.SWIFTNESS && target != null && target.getY() > witch.getY() + 2.0
				? Potions.LEAPING
				: potion;
	}

	@Inject(method = "performRangedAttack", at = @At("HEAD"))
	private void patchwork$jumpAndAimAtElevatedTarget(LivingEntity target, float distance, CallbackInfo ci) {
		Witch witch = (Witch) (Object) this;
		double height = target.getEyeY() - 1.1 - witch.getY();
		if (witch.getMainHandItem().is(Items.SPLASH_POTION) && height > 1.0) {
			Vec3 velocity = witch.getDeltaMovement();
			witch.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.42), velocity.z);
			witch.jumpFromGround();
			witch.setDeltaMovement(witch.getDeltaMovement().add(0, Math.min(0.8, height * 0.15), 0));
		}
	}
}
