package com.JulieISBaka.patchwork.mixins.mobs;

import com.JulieISBaka.patchwork.features.mobs.VillagerRoseOffering;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(OfferFlowerGoal.class)
public abstract class BabyVillagerRoseMixin {
	@Shadow
	@Final
	private IronGolem golem;

	@Shadow
	@Nullable
	private LivingEntity entity;

	@Shadow
	private int tick;

	@Inject(method = "stop", at = @At("HEAD"))
	private void patchwork$offerRoseToBabyVillager(CallbackInfo ci) {
		if (this.tick == 0 && this.entity instanceof Villager villager
				&& this.golem.level() instanceof ServerLevel level) {
			VillagerRoseOffering.offerToBaby(level, this.golem, villager);
		}
	}
}
