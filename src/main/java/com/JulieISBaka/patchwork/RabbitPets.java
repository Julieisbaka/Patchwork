package com.JulieISBaka.patchwork;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public final class RabbitPets {
	private RabbitPets() {
	}

	public static boolean teleportToOwner(Rabbit rabbit, LivingEntity owner) {
		if (rabbit.isPassenger() || rabbit.isLeashed() || owner.isSpectator() || owner.level() != rabbit.level()) {
			return false;
		}
		for (int attempt = 0; attempt < 10; attempt++) {
			int x = rabbit.getRandom().nextIntBetweenInclusive(-3, 3);
			int z = rabbit.getRandom().nextIntBetweenInclusive(-3, 3);
			if (Math.abs(x) < 2 && Math.abs(z) < 2) {
				continue;
			}
			BlockPos pos = owner.blockPosition().offset(x, rabbit.getRandom().nextIntBetweenInclusive(-1, 1), z);
			if (!rabbit.level().getChunkSource().hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
					SectionPos.blockToSectionCoord(pos.getZ()))
					|| WalkNodeEvaluator.getPathTypeStatic(rabbit, pos) != PathType.WALKABLE
					|| rabbit.level().getBlockState(pos.below()).getBlock() instanceof LeavesBlock
					|| !rabbit.level().noCollision(rabbit, rabbit.getBoundingBox().move(pos.subtract(rabbit.blockPosition())))) {
				continue;
			}
			rabbit.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rabbit.getYRot(), rabbit.getXRot());
			rabbit.getNavigation().stop();
			return true;
		}
		return false;
	}

	public static final class StayGoal extends Goal {
		private final Rabbit rabbit;
		private final RabbitPet pet;

		public StayGoal(Rabbit rabbit, RabbitPet pet) {
			this.rabbit = rabbit;
			this.pet = pet;
			setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return pet.getOwnerReference() != null && pet.patchwork$isOrderedToStay()
					&& rabbit.onGround() && !rabbit.isInWater();
		}

		@Override
		public void start() {
			rabbit.getNavigation().stop();
			rabbit.setSpeedModifier(0);
			rabbit.setJumping(false);
		}
	}

	public static final class FollowGoal extends Goal {
		private final Rabbit rabbit;
		private final RabbitPet pet;
		private LivingEntity owner;
		private int recalculatePathTicks;
		private float oldWaterCost;

		public FollowGoal(Rabbit rabbit, RabbitPet pet) {
			this.rabbit = rabbit;
			this.pet = pet;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		private boolean canFollow(LivingEntity candidate) {
			return candidate != null && candidate.isAlive() && !candidate.isSpectator()
					&& candidate.level() == rabbit.level() && !pet.patchwork$isOrderedToStay()
					&& !rabbit.isPassenger() && !rabbit.isLeashed();
		}

		@Override
		public boolean canUse() {
			LivingEntity candidate = pet.getOwner();
			if (!canFollow(candidate) || rabbit.distanceToSqr(candidate) < 25) {
				return false;
			}
			owner = candidate;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return canFollow(owner) && rabbit.distanceToSqr(owner) > 4;
		}

		@Override
		public void start() {
			recalculatePathTicks = 0;
			oldWaterCost = rabbit.getPathfindingMalus(PathType.WATER);
			rabbit.setPathfindingMalus(PathType.WATER, 0);
		}

		@Override
		public void stop() {
			owner = null;
			rabbit.getNavigation().stop();
			rabbit.setPathfindingMalus(PathType.WATER, oldWaterCost);
		}

		@Override
		public void tick() {
			rabbit.getLookControl().setLookAt(owner, 10, rabbit.getMaxHeadXRot());
			if (--recalculatePathTicks <= 0) {
				recalculatePathTicks = adjustedTickDelay(10);
				if (rabbit.distanceToSqr(owner) < 144 || !teleportToOwner(rabbit, owner)) {
					rabbit.getNavigation().moveTo(owner, Rabbit.FOLLOW_SPEED_MOD);
				}
			}
		}
	}
}
