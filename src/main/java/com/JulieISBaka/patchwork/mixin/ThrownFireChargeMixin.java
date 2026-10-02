package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.Patchwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LargeFireball.class)
public class ThrownFireChargeMixin {
	@ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 6.0F))
	private float patchwork$directDamage(float damage) {
		return ((LargeFireball)(Object)this).getTags().contains(Patchwork.THROWN_FIRE_CHARGE_TAG) ? 2.0F : damage;
	}

	@Redirect(
		method = "onHit",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V")
	)
	private void patchwork$igniteWithoutExplosion(
		Level level, Entity source, double x, double y, double z, float radius, boolean fire,
		Level.ExplosionInteraction interaction, HitResult hit
	) {
		if (!((LargeFireball)(Object)this).getTags().contains(Patchwork.THROWN_FIRE_CHARGE_TAG)) {
			level.explode(source, x, y, z, radius, fire, interaction);
			return;
		}
		if (hit instanceof BlockHitResult blockHit) {
			BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
			if (level.isEmptyBlock(pos)) {
				level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
			}
		}
	}
}
