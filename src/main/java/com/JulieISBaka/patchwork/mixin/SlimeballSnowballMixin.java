package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.NumericSetting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public class SlimeballSnowballMixin {
	@Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
	private void patchwork$onSlimeballHit(EntityHitResult hit, CallbackInfo ci) {
		Snowball projectile = (Snowball) (Object) this;
		if (!PatchworkConfig.settings().throwableSlimeballs() || !projectile.getItem().is(Items.SLIME_BALL)) {
			return;
		}

		if (projectile.level() instanceof ServerLevel level) {
			Entity target = hit.getEntity();
			if (target instanceof Slime slime) {
				slime.heal(NumericSetting.SLIMEBALL_HEALING.floatValue());
				slime.addEffect(new MobEffectInstance(MobEffects.SPEED,
						NumericSetting.SLIMEBALL_EFFECT_DURATION.intValue(),
						NumericSetting.SLIMEBALL_EFFECT_AMPLIFIER.intValue()), projectile.getOwner());
			} else if (target.hurtServer(level, projectile.damageSources().thrown(projectile, projectile.getOwner()),
					NumericSetting.SLIMEBALL_DAMAGE.floatValue()) && target instanceof LivingEntity living) {
				living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,
						NumericSetting.SLIMEBALL_EFFECT_DURATION.intValue(),
						NumericSetting.SLIMEBALL_EFFECT_AMPLIFIER.intValue()), projectile.getOwner());
			}
		}
		ci.cancel();
	}
}
