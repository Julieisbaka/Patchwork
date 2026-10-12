package com.JulieISBaka.patchwork;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.block.Blocks;

public final class BlazeFireSeekingGoal extends Goal {
	private final Blaze blaze;
	private BlockPos target;
	private int healCooldown;
	private int pathRecalculationCooldown;
	private int searchCooldown;

	public BlazeFireSeekingGoal(Blaze blaze) {
		this.blaze = blaze;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!(this.blaze.level() instanceof ServerLevel level)
				|| this.blaze.getHealth() >= this.blaze.getMaxHealth()) {
			return false;
		}
		if (this.searchCooldown > 0) {
			this.searchCooldown--;
			return false;
		}
		this.target = findNearbyFire(level, this.blaze, this.blaze.blockPosition());
		if (this.target == null) {
			this.searchCooldown = 40;
		}
		return this.target != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.blaze.getHealth() < this.blaze.getMaxHealth()
				&& this.blaze.level() instanceof ServerLevel level
				&& (isFire(level, this.target) || this.blaze.distanceToSqr(
						this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5) > 4.0);
	}

	@Override
	public void stop() {
		this.target = null;
		this.blaze.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (!(this.blaze.level() instanceof ServerLevel level) || this.target == null) {
			return;
		}
		if (this.blaze.distanceToSqr(this.target.getX() + 0.5, this.target.getY(),
				this.target.getZ() + 0.5) > 2.25) {
			if (this.pathRecalculationCooldown-- <= 0) {
				this.blaze.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(),
						this.target.getZ() + 0.5, 1.0);
				this.pathRecalculationCooldown = 10;
			}
		} else if (isFire(level, this.target)) {
			if (this.healCooldown-- <= 0) {
				this.blaze.heal(2.0F);
				this.healCooldown = 20;
			}
		}
	}

	private static BlockPos findNearbyFire(ServerLevel level, Blaze blaze, BlockPos origin) {
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -4, -8), origin.offset(8, 4, 8))) {
			if (isFire(level, pos) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(),
					net.minecraft.core.Direction.UP)) {
				Path path = blaze.getNavigation().createPath(pos, 0);
				if (path == null || !path.canReach()) {
					continue;
				}
				double distance = origin.distSqr(pos);
				if (distance < bestDistance) {
					best = pos.immutable();
					bestDistance = distance;
				}
			}
		}
		return best;
	}

	private static boolean isFire(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).is(Blocks.FIRE) || level.getBlockState(pos).is(Blocks.SOUL_FIRE);
	}
}
