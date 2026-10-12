package com.JulieISBaka.patchwork.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class WitherKilledSkeletonMixin {
	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void patchwork$convertSkeletonFromWitherDamage(DamageSource source, CallbackInfo ci) {
		if (!((Object) this instanceof Skeleton skeleton) || !(skeleton.level() instanceof ServerLevel level)
				|| !(source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
						|| source.is(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL)
						|| source.getEntity() instanceof WitherBoss || source.getDirectEntity() instanceof WitherSkull)) {
			return;
		}
		WitherSkeleton converted = EntityTypes.WITHER_SKELETON.create(level, EntitySpawnReason.CONVERSION);
		if (converted == null) {
			throw new IllegalStateException("Could not create a wither skeleton conversion");
		}
		converted.setPos(skeleton.position());
		converted.setYRot(skeleton.getYRot());
		converted.setXRot(skeleton.getXRot());
		converted.setCustomName(skeleton.getCustomName());
		converted.setCustomNameVisible(skeleton.isCustomNameVisible());
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			converted.setItemSlot(slot, skeleton.getItemBySlot(slot).copy());
		}
		converted.setHealth(Math.min(converted.getMaxHealth(), 8.0F));
		if (!level.addFreshEntity(converted)) {
			throw new IllegalStateException("Could not add the converted wither skeleton");
		}
		skeleton.discard();
		ci.cancel();
	}
}
