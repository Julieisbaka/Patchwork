package com.JulieISBaka.patchwork.features.mobs;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public final class EnderDragonBehavior {
	public static final String NATURAL_CRYSTAL_TAG = "patchwork:natural_end_crystal";

	private EnderDragonBehavior() {
	}

	public static Vec3 increaseFlightSpeed(Vec3 movement) {
		return movement.scale(1.5);
	}

	public static void markNaturalCrystal(EndCrystal crystal) {
		crystal.addTag(NATURAL_CRYSTAL_TAG);
	}

	public static float crystalExplosionPower(float vanillaPower, EndCrystal crystal) {
		return crystal.entityTags().contains(NATURAL_CRYSTAL_TAG) ? vanillaPower * 1.5F : vanillaPower;
	}

	public static void reinforceCrystalCage(ServerLevelAccessor level, EndSpikeFeature.EndSpike spike) {
		if (!spike.isGuarded()) {
			return;
		}

		int centerX = spike.getCenterX();
		int centerZ = spike.getCenterZ();
		int baseY = spike.getHeight();
		var obsidian = Blocks.OBSIDIAN.defaultBlockState();
		for (int y = baseY; y <= baseY + 4; y++) {
			for (int offset = -3; offset <= 3; offset++) {
				level.setBlock(new BlockPos(centerX - 3, y, centerZ + offset), obsidian, 3);
				level.setBlock(new BlockPos(centerX + 3, y, centerZ + offset), obsidian, 3);
				level.setBlock(new BlockPos(centerX + offset, y, centerZ - 3), obsidian, 3);
				level.setBlock(new BlockPos(centerX + offset, y, centerZ + 3), obsidian, 3);
			}
		}
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				level.setBlock(new BlockPos(centerX + x, baseY + 5, centerZ + z), obsidian, 3);
			}
		}
	}

	public static int enrageIslandEndermen(ServerLevel level, EnderDragon dragon) {
		BlockPos origin = dragon.getFightOrigin();
		if (origin == null) {
			origin = BlockPos.ZERO;
		}
		BlockPos fightCenter = origin;
		AABB island = new AABB(fightCenter.getX() - 128.0, level.getMinY(), fightCenter.getZ() - 128.0,
				fightCenter.getX() + 128.0, level.getMaxY(), fightCenter.getZ() + 128.0);
		var target = level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, island,
				player -> player.isAlive()
						&& player.distanceToSqr(fightCenter.getX(), player.getY(), fightCenter.getZ()) <= 128 * 128)
				.stream().min(Comparator.comparingDouble(dragon::distanceToSqr))
				.orElse(null);
		if (target == null) {
			return 0;
		}

		AABB searchArea = target.getBoundingBox().inflate(64.0, 32.0, 64.0);
		int angered = 0;
		for (Enderman enderman : level.getEntitiesOfClass(Enderman.class, searchArea,
				mob -> mob.distanceToSqr(fightCenter.getX(), mob.getY(), fightCenter.getZ()) <= 128 * 128)) {
			enderman.setTarget(target);
			angered++;
		}
		return angered;
	}
}
