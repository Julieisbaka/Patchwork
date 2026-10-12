package com.JulieISBaka.patchwork.mixins.mobs;

import com.JulieISBaka.patchwork.features.mobs.EnderDragonBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature.EndSpike;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EndSpikeFeature.class)
public abstract class EndSpikeFeatureMixin {
	@ModifyArg(method = "placeSpike", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), index = 0)
	private Entity patchwork$markNaturalCrystal(Entity entity) {
		if (entity instanceof EndCrystal crystal) {
			EnderDragonBehavior.markNaturalCrystal(crystal);
		}
		return entity;
	}

	@Inject(method = "placeSpike", at = @At("TAIL"))
	private void patchwork$reinforceCrystalCage(ServerLevelAccessor level, RandomSource random, EndSpike spike,
			CallbackInfo ci) {
		EnderDragonBehavior.reinforceCrystalCage(level, spike);
	}
}
