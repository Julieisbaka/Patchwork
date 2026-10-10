package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.NumericSetting;
import com.JulieISBaka.patchwork.UnlitTorches;
import com.JulieISBaka.patchwork.UnlitLanterns;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class BreezeShockwaveMixin {
	@Unique
	private long patchwork$nextShockwaveTick;

	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void patchwork$shockwave(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Boolean> cir) {
		if (!PatchworkConfig.settings().breezeShockwave() || !cir.getReturnValue()
				|| !((Object) this instanceof Breeze breeze) || !breeze.isAlive()
				|| !(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK)
						|| source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.SPEAR))
				|| !(source.getDirectEntity() instanceof LivingEntity attacker) || attacker == breeze
				|| level.getGameTime() < this.patchwork$nextShockwaveTick) {
			return;
		}
		this.patchwork$nextShockwaveTick = level.getGameTime() + NumericSetting.BREEZE_SHOCKWAVE_COOLDOWN.intValue();
		int radius = NumericSetting.BREEZE_SHOCKWAVE_RADIUS.intValue();
		Vec3 center = breeze.position();
		level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, center.x, center.y + 0.5, center.z, 1, 0.0, 0.0, 0.0,
				0.0);
		level.playSound(null, breeze.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.HOSTILE,
				1.0F, 1.0F);

		for (Entity entity : level.getEntitiesOfClass(Entity.class, breeze.getBoundingBox().inflate(radius))) {
			Vec3 direction = entity.position().subtract(center);
			if (entity == breeze || direction.lengthSqr() > radius * radius || direction.lengthSqr() < 0.01) {
				continue;
			}
			Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
			if (horizontal.lengthSqr() > 0.01) {
				Vec3 push = horizontal.normalize().scale(NumericSetting.BREEZE_HORIZONTAL_PUSH.get());
				entity.push(push.x, NumericSetting.BREEZE_VERTICAL_PUSH.get(), push.z);
			}
		}
		if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return;
		}
		BlockPos origin = breeze.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
				origin.offset(radius, radius, radius))) {
			if (pos.distSqr(origin) > radius * radius) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			BlockState unlit = PatchworkConfig.settings().breezeTorchExtinguishing() ? UnlitTorches.extinguish(state)
					: null;
			if (unlit == null && PatchworkConfig.settings().breezeTorchExtinguishing()) {
				unlit = UnlitLanterns.extinguish(state);
			}
			if (unlit != null) {
				level.setBlockAndUpdate(pos, unlit);
			} else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
				CampfireBlock.douse(breeze, level, pos, state);
				level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
			}
		}
	}
}
