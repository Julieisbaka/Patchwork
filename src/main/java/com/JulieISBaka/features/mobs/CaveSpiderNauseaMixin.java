package com.JulieISBaka.patchwork.features.mobs;

import com.JulieISBaka.patchwork.config.NumericSetting;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CaveSpider.class)
public abstract class CaveSpiderNauseaMixin {
	@Inject(method = "doHurtTarget", at = @At("RETURN"))
	private void patchwork$inflictNausea(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
		if (PatchworkConfig.settings().caveSpiderNausea() && cir.getReturnValue()
				&& target instanceof LivingEntity living
				&& ((CaveSpider) (Object) this).getRandom().nextFloat() < NumericSetting.CAVE_SPIDER_NAUSEA_CHANCE.get()) {
			living.addEffect(new MobEffectInstance(MobEffects.NAUSEA,
					NumericSetting.CAVE_SPIDER_NAUSEA_DURATION.intValue()), (CaveSpider) (Object) this);
		}
	}
}
