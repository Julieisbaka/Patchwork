package com.JulieISBaka.patchwork;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.phys.Vec3;

public final class WitherSkullHoming {
	private static final double TARGET_RANGE = 48.0;
	private static final double STEERING_STRENGTH = 0.2;

	private WitherSkullHoming() {
	}

	public static Optional<Player> findPlayerTarget(WitherSkull skull, ServerLevel level) {
		return level.getEntitiesOfClass(Player.class, skull.getBoundingBox().inflate(TARGET_RANGE),
				player -> player.isAlive() && !player.isSpectator())
				.stream().min(Comparator.comparingDouble(skull::distanceToSqr));
	}

	public static void steerTowardPlayer(WitherSkull skull, ServerLevel level) {
		if (!skull.isDangerous()) {
			return;
		}
		Optional<Player> target = findPlayerTarget(skull, level);
		if (target.isEmpty()) {
			return;
		}
		Vec3 velocity = skull.getDeltaMovement();
		double speed = velocity.length();
		if (speed < 1.0E-6) {
			return;
		}
		Vec3 desired = target.get().getEyePosition().subtract(skull.position()).normalize();
		Vec3 steered = velocity.scale(1.0 - STEERING_STRENGTH)
				.add(desired.scale(speed * STEERING_STRENGTH));
		if (steered.lengthSqr() > 1.0E-12) {
			skull.setDeltaMovement(steered.normalize().scale(speed));
		}
	}
}
