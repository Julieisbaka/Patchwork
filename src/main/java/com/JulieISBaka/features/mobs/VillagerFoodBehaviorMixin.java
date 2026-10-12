package com.JulieISBaka.patchwork.features.mobs;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Villager.class)
public abstract class VillagerFoodBehaviorMixin {
	@Unique
	private int patchwork$healFoodCooldown;
	@Unique
	private boolean patchwork$notifiedOutOfFood;

	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$eatToHealAndWarnWhenHungry(CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		if (!(villager.level() instanceof ServerLevel level)) {
			return;
		}
		boolean hasFood = VillagerFoodBehavior.hasFood(villager);
		if (!hasFood && !this.patchwork$notifiedOutOfFood && villager.getVillagerXp() > 0
				&& villager.hasCustomName()) {
			level.getServer().getPlayerList().broadcastSystemMessage(
					Component.translatable("chat.patchwork.villager_out_of_food", villager.getCustomName()), false);
			this.patchwork$notifiedOutOfFood = true;
		} else if (hasFood) {
			this.patchwork$notifiedOutOfFood = false;
		}
		if (!hasFood || villager.getHealth() >= villager.getMaxHealth()) {
			return;
		}
		if (this.patchwork$healFoodCooldown > 0) {
			this.patchwork$healFoodCooldown--;
			return;
		}
		VillagerFoodBehavior.eatToHeal(villager);
		this.patchwork$healFoodCooldown = 100;
	}
}
