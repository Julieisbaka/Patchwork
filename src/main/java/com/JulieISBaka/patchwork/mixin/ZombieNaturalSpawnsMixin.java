package com.JulieISBaka.patchwork.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieNaturalSpawnsMixin {
	@Unique
	private boolean patchwork$pendingJockey;
	@Unique
	private boolean patchwork$pendingLeader;
	@Unique
	private boolean patchwork$spawnHandled;

	@Inject(method = "finalizeSpawn", at = @At("TAIL"))
	private void patchwork$rollRareZombieVariants(net.minecraft.world.level.ServerLevelAccessor level,
			net.minecraft.world.DifficultyInstance difficulty, EntitySpawnReason reason,
			net.minecraft.world.entity.SpawnGroupData data,
			CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
		if (reason != EntitySpawnReason.NATURAL || ((Zombie) (Object) this).isBaby()) {
			return;
		}
		float roll = ((Zombie) (Object) this).getRandom().nextFloat();
		this.patchwork$pendingLeader = roll < 0.01F;
		this.patchwork$pendingJockey = !this.patchwork$pendingLeader && roll < 0.06F;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void patchwork$spawnZombieRiders(CallbackInfo ci) {
		if (this.patchwork$spawnHandled || (!this.patchwork$pendingLeader && !this.patchwork$pendingJockey)
				|| !(((Zombie) (Object) this).level() instanceof ServerLevel level)) {
			return;
		}
		this.patchwork$spawnHandled = true;
		Zombie zombie = (Zombie) (Object) this;
		if (this.patchwork$pendingLeader) {
			var horse = EntityTypes.ZOMBIE_HORSE.create(level, EntitySpawnReason.JOCKEY);
			if (horse == null) {
				throw new IllegalStateException("Could not create a zombie horse for a zombie leader");
			}
			horse.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()),
					EntitySpawnReason.JOCKEY, null);
			horse.setPos(zombie.position());
			horse.setPersistenceRequired();
			ItemStack banner = Items.BANNER.black().getDefaultInstance();
			banner.set(DataComponents.CUSTOM_NAME, Component.literal("Zombie Leader Banner"));
			zombie.setItemSlot(EquipmentSlot.HEAD, banner);
			zombie.setCustomNameVisible(true);
			level.addFreshEntity(horse);
			zombie.startRiding(horse, true, true);
			return;
		}
		var rider = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.JOCKEY);
		if (rider == null) {
			throw new IllegalStateException("Could not create a baby zombie rider");
		}
		rider.setBaby(true);
		rider.setPos(zombie.position());
		level.addFreshEntity(rider);
		rider.startRiding(zombie, true, true);
	}
}
