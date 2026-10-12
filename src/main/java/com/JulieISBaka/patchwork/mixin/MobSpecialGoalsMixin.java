package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.BlazeFireSeekingGoal;
import com.JulieISBaka.patchwork.ChickenJockeyAttackGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobSpecialGoalsMixin {
	@Shadow
	@Final
	protected GoalSelector goalSelector;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$addSpecialMobGoals(EntityType<? extends Mob> entityType, Level level, CallbackInfo ci) {
		if ((Object) this instanceof Blaze blaze) {
			this.goalSelector.addGoal(1, new BlazeFireSeekingGoal(blaze));
		} else if ((Object) this instanceof Chicken chicken) {
			this.goalSelector.addGoal(1, new ChickenJockeyAttackGoal(chicken));
		}
	}
}
