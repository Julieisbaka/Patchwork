package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.AABB;


public final class WitherMinions {
	public static final String MINION_TAG = "patchwork:wither_minion";
	private static final int MAX_NEARBY_MINIONS = 4;

	private WitherMinions() {
	}

	public static int summon(ServerLevel level, WitherBoss wither, LivingEntity target) {
		if (!target.isAlive()) {
			return 0;
		}

		int existing = level.getEntitiesOfClass(WitherSkeleton.class, wither.getBoundingBox().inflate(32.0),
				skeleton -> skeleton.getTags().contains(MINION_TAG)).size();
		int count = Math.min(2, MAX_NEARBY_MINIONS - existing);
		int spawned = 0;
		for (int i = 0; i < count; i++) {
			WitherSkeleton skeleton = net.minecraft.world.entity.EntityTypes.WITHER_SKELETON
					.create(level, EntitySpawnReason.TRIGGERED);
			if (skeleton == null) {
				continue;
			}

			double angle = Math.PI * 2.0 * i / Math.max(1, count);
			skeleton.setPos(wither.getX() + Math.cos(angle) * 2.0, wither.getY(),
					wither.getZ() + Math.sin(angle) * 2.0);
			skeleton.addTag(MINION_TAG);
			skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
			if (level.addFreshEntity(skeleton)) {
				skeleton.setTarget(target);
				spawned++;
			}
		}
		return spawned;
	}
}
