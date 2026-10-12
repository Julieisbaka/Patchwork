package com.JulieISBaka.patchwork;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;

public final class ChickenJockeyAttackGoal extends Goal {
	private final Chicken chicken;
	private Player target;
	private int attackCooldown;

	public ChickenJockeyAttackGoal(Chicken chicken) {
		this.chicken = chicken;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!isBabyZombieJockey(this.chicken)) {
			return false;
		}
		this.target = this.chicken.level().getEntitiesOfClass(Player.class,
				this.chicken.getBoundingBox().inflate(12.0),
				player -> player.isAlive() && !player.isSpectator()).stream()
				.min(java.util.Comparator.comparingDouble(player -> this.chicken.distanceToSqr(player)))
				.orElse(null);
		return this.target != null;
	}

	@Override
	public boolean canContinueToUse() {
		return isBabyZombieJockey(this.chicken) && this.target != null && this.target.isAlive()
				&& this.chicken.distanceToSqr(this.target) < 256.0;
	}

	@Override
	public void stop() {
		this.target = null;
		this.chicken.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.target == null) {
			return;
		}
		this.chicken.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
		this.chicken.getNavigation().moveTo(this.target, 1.2);
		if (this.attackCooldown > 0) {
			this.attackCooldown--;
		} else if (this.chicken.distanceToSqr(this.target) < 2.25
				&& this.chicken.level() instanceof ServerLevel level
				&& this.target.hurtServer(level, level.damageSources().mobAttack(this.chicken), 2.0F)) {
			this.attackCooldown = 20;
		}
	}

	public static boolean isBabyZombieJockey(Chicken chicken) {
		return chicken.getPassengers().stream()
				.anyMatch(passenger -> passenger instanceof Zombie zombie && zombie.isBaby());
	}
}
