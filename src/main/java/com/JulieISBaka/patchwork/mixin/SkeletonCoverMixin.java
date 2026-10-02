package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class SkeletonCoverMixin {
	@Shadow @Final private Monster mob;
	@Shadow private int attackTime;
	@Unique private BlockPos patchwork$cover;
	@Unique private BlockPos patchwork$peek;

	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$useCoverWhileReloading(CallbackInfo ci) {
		if (!(mob instanceof Skeleton skeleton) || !(mob.level() instanceof ServerLevel level)
			|| !PatchworkConfig.settings().skeletonCover()) {
			patchwork$clear();
			return;
		}
		LivingEntity target = skeleton.getTarget();
		if (!(target instanceof Player) || !target.isAlive()
			|| skeleton.distanceToSqr(target) > 36.0 || !skeleton.isHolding(net.minecraft.world.item.Items.BOW)) {
			patchwork$clear();
			return;
		}
		if (patchwork$cover == null && attackTime > 0 && !skeleton.isUsingItem()) {
			patchwork$peek = skeleton.blockPosition();
			patchwork$cover = patchwork$findCover(level, skeleton, target);
		}
		if (patchwork$cover == null) {
			return;
		}
		if (attackTime > 0 && !skeleton.isUsingItem()) {
			skeleton.getNavigation().moveTo(patchwork$cover.getX() + 0.5,
				patchwork$cover.getY(), patchwork$cover.getZ() + 0.5, 1.15);
		} else {
			if (patchwork$peek != null && skeleton.blockPosition().distSqr(patchwork$peek) > 1) {
				skeleton.getNavigation().moveTo(patchwork$peek.getX() + 0.5,
					patchwork$peek.getY(), patchwork$peek.getZ() + 0.5, 1.15);
			} else {
				patchwork$clear();
			}
		}
	}

	@Unique
	private BlockPos patchwork$findCover(ServerLevel level, Skeleton skeleton, LivingEntity target) {
		BlockPos origin = skeleton.blockPosition();
		BlockPos best = null;
		double bestDistance = Double.POSITIVE_INFINITY;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if ((dx == 0 && dz == 0) || dx * dx + dz * dz > 5) {
					continue;
				}
				BlockPos candidate = origin.offset(dx, 0, dz);
				if (!level.getBlockState(candidate).isAir() || !level.getBlockState(candidate.above()).isAir()
					|| !level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) {
					continue;
				}
				Path path = skeleton.getNavigation().createPath(candidate, 0);
				if (path == null || !path.canReach()) {
					continue;
				}
				Vec3 eye = Vec3.atBottomCenterOf(candidate).add(0, skeleton.getEyeHeight(), 0);
				HitResult obstruction = level.clip(new ClipContext(eye, target.getEyePosition(),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, skeleton));
				if (obstruction.getType() == HitResult.Type.MISS || obstruction.getLocation().distanceToSqr(eye) > 3.0) {
					continue;
				}
				double distance = origin.distSqr(candidate);
				if (distance < bestDistance) {
					best = candidate;
					bestDistance = distance;
				}
			}
		}
		return best;
	}

	@Inject(method = "stop", at = @At("TAIL"))
	private void patchwork$stopCover(CallbackInfo ci) {
		patchwork$clear();
	}

	@Unique
	private void patchwork$clear() {
		patchwork$cover = null;
		patchwork$peek = null;
	}
}
