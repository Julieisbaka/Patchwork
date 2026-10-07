package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.AnimalFoodGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class AnimalFoodMixin {
	@Shadow
	@Final
	protected GoalSelector goalSelector;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$addFoodSeekingGoal(EntityType<? extends Mob> entityType, Level level, CallbackInfo ci) {
		if (!level.isClientSide() && (Object) this instanceof Animal animal) {
			this.goalSelector.addGoal(4, new AnimalFoodGoal(animal));
		}
	}
}
