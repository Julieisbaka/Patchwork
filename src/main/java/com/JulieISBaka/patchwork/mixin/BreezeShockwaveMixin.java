package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.UnlitTorches;
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
	private void patchwork$shockwave(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		if (!PatchworkConfig.settings().breezeShockwave() || !cir.getReturnValue()
			|| !((Object)this instanceof Breeze breeze) || !breeze.isAlive()
			|| !(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK)
				|| source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.SPEAR))
			|| !(source.getDirectEntity() instanceof LivingEntity attacker) || attacker == breeze
			|| level.getGameTime() < this.patchwork$nextShockwaveTick) {
			return;
		}
		this.patchwork$nextShockwaveTick = level.getGameTime() + 200;
		Vec3 center = breeze.position();
		level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, center.x, center.y + 0.5, center.z,
			1, 0.0, 0.0, 0.0, 0.0);
		level.playSound(null, breeze.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.HOSTILE, 1.0F, 1.0F);

		for (Entity entity : level.getEntitiesOfClass(Entity.class, breeze.getBoundingBox().inflate(5.0))) {
			Vec3 direction = entity.position().subtract(center);
			if (entity == breeze || direction.lengthSqr() > 25.0 || direction.lengthSqr() < 0.01) {
				continue;
			}
			Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
			if (horizontal.lengthSqr() > 0.01) {
				Vec3 push = horizontal.normalize().scale(1.2);
				entity.push(push.x, 0.25, push.z);
			}
		}
		if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return;
		}
		BlockPos origin = breeze.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, -5, -5), origin.offset(5, 5, 5))) {
			if (pos.distSqr(origin) > 25.0) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			BlockState unlit = UnlitTorches.extinguish(state);
			if (unlit != null) {
				level.setBlockAndUpdate(pos, unlit);
			} else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
				CampfireBlock.douse(breeze, level, pos, state);
				level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
			}
		}
	}
}
