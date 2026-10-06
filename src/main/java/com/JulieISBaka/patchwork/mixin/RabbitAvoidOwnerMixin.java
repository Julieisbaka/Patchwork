package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.RabbitPet;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvoidEntityGoal.class)
public abstract class RabbitAvoidOwnerMixin {
	@Shadow @Final protected PathfinderMob mob;
	@Shadow @Final protected Predicate<? super LivingEntity> avoidPredicate;
	@Shadow @Final protected Predicate<? super LivingEntity> predicateOnAvoidEntity;
	@Shadow @Final private TargetingConditions avoidEntityTargeting;
	@Shadow protected LivingEntity toAvoid;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$excludeRabbitOwner(CallbackInfo ci) {
		if (mob instanceof RabbitPet pet) {
			avoidEntityTargeting.selector((target, level) -> predicateOnAvoidEntity.test(target)
					&& avoidPredicate.test(target) && !pet.patchwork$isOwnedBy(target));
		}
	}

	@Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
	private void patchwork$stopAvoidingNewOwner(CallbackInfoReturnable<Boolean> cir) {
		if (mob instanceof RabbitPet pet && toAvoid != null && pet.patchwork$isOwnedBy(toAvoid)) {
			cir.setReturnValue(false);
		}
	}
}
