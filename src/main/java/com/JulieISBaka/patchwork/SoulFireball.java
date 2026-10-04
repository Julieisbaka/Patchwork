package com.JulieISBaka.patchwork;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SoulFireball extends LargeFireball {
	public SoulFireball(EntityType<? extends SoulFireball> type, Level level) {
		super(type, level);
		setItem(new ItemStack(SoulFireCharges.ITEM));
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.SOUL_FIRE_FLAME;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setItem(input.read("Item", ItemStack.CODEC).orElseGet(() -> new ItemStack(SoulFireCharges.ITEM)));
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (getOwner() instanceof SoulGolem golem && entity instanceof LivingEntity living
				&& golem.isProtected(living)) {
			return false;
		}
		return super.canHitEntity(entity);
	}

	@Override
	protected void onHit(HitResult hit) {
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		if (hit instanceof EntityHitResult entityHit) {
			Entity target = entityHit.getEntity();
			if (!(getOwner() instanceof SoulGolem golem && target instanceof LivingEntity living
					&& golem.isProtected(living))
					&& target.hurtServer(level, damageSources().fireball(this, getOwner()), 3.0F)) {
				target.igniteForSeconds(2.0F);
				if (getOwner() instanceof LivingEntity shooter) {
					shooter.setLastHurtMob(target);
				}
			}
		} else if (hit instanceof BlockHitResult blockHit
				&& (!(getOwner() instanceof SoulGolem) || level.getGameRules().get(GameRules.MOB_GRIEFING))) {
			SoulFireCharges.ignite(level, blockHit);
		}
		discard();
	}
}
