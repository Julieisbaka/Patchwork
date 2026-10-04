package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.Patchwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LargeFireball.class)
public class ThrownFireChargeMixin {
	@ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 6.0F))
	private float patchwork$directDamage(float damage) {
		return ((LargeFireball) (Object) this).entityTags().contains(Patchwork.THROWN_FIRE_CHARGE_TAG) ? 2.0F : damage;
	}

	@Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
	private void patchwork$igniteWithoutExplosion(Level level, Entity source, double x, double y, double z,
			float radius, boolean fire, Level.ExplosionInteraction interaction, HitResult hit) {
		if (!((LargeFireball) (Object) this).entityTags().contains(Patchwork.THROWN_FIRE_CHARGE_TAG)) {
			level.explode(source, x, y, z, radius, fire, interaction);
			return;
		}
		if (hit instanceof BlockHitResult blockHit) {
			BlockPos struck = blockHit.getBlockPos();
			BlockState state = level.getBlockState(struck);
			if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
				level.setBlockAndUpdate(struck, state.setValue(BlockStateProperties.LIT, true));
				return;
			}
			Direction face = blockHit.getDirection();
			BlockPos adjacent = struck.relative(face);
			if (!patchwork$placeFire(level, adjacent, face) && face.getAxis().isHorizontal()) {
				patchwork$placeFire(level, struck.above(), Direction.UP);
			}
		}
	}

	@Unique
	private static boolean patchwork$placeFire(Level level, BlockPos pos, Direction face) {
		if (!BaseFireBlock.canBePlacedAt(level, pos, face)) {
			return false;
		}
		return level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
	}
}
