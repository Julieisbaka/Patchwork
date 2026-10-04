package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
	@Shadow
	private int count;
	@Shadow
	private int age;

	@Shadow
	public abstract int getValue();

	@Shadow
	protected abstract void setValue(int value);

	@Unique
	private long patchwork$nextMergeTick;

	@Inject(method = "awardWithDirection", at = @At("HEAD"), cancellable = true)
	private static void patchwork$awardSingleOrb(ServerLevel level, Vec3 pos, Vec3 direction, int amount,
		CallbackInfo ci) {
		if (PatchworkConfig.settings().experienceClumping()) {
			if (amount > 0 && !level.addFreshEntity(new ExperienceOrb(level, pos, direction, amount))) {
				throw new IllegalStateException("Could not spawn experience orb at " + pos);
			}
			ci.cancel();
		}
	}

	@Inject(method = "scanForMerges", at = @At("HEAD"), cancellable = true)
	private void patchwork$replaceMergeScan(CallbackInfo ci) {
		if (PatchworkConfig.settings().experienceClumping()) {
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$clump(CallbackInfo ci) {
		ExperienceOrb self = (ExperienceOrb) (Object) this;
		if (!PatchworkConfig.settings().experienceClumping() || self.isRemoved()
			|| !(self.level() instanceof ServerLevel level) || level.getGameTime() < patchwork$nextMergeTick) {
			return;
		}
		patchwork$nextMergeTick = level.getGameTime() + 20;
		for (ExperienceOrb other : level.getEntitiesOfClass(ExperienceOrb.class, self.getBoundingBox().inflate(2.0),
			orb -> orb != self && !orb.isRemoved() && self.distanceToSqr(orb) <= 4.0)) {
			ExperienceOrbAccessor otherData = (ExperienceOrbAccessor) other;
			long total = (long) getValue() * count + (long) other.getValue() * otherData.patchwork$count();
			if (total > 0 && total <= Integer.MAX_VALUE) {
				setValue((int) total);
				count = 1;
				age = Math.min(age, otherData.patchwork$age());
				other.discard();
			}
		}
	}

	@Inject(method = "playerTouch", at = @At("HEAD"))
	private void patchwork$collectAll(Player player, CallbackInfo ci) {
		if (PatchworkConfig.settings().experienceClumping()) {
			long total = (long) getValue() * count;
			if (total > 0 && total <= Integer.MAX_VALUE) {
				setValue((int) total);
				count = 1;
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void patchwork$saveFullValue(ValueOutput output, CallbackInfo ci) {
		output.putInt("Value", getValue());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void patchwork$loadFullValue(ValueInput input, CallbackInfo ci) {
		setValue(input.getIntOr("Value", 0));
	}
}
