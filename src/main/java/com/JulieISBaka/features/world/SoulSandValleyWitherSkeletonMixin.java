package com.JulieISBaka.patchwork.features.world;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(WitherSkeleton.class)
public abstract class SoulSandValleyWitherSkeletonMixin {
	@Inject(method = "finalizeSpawn", at = @At("TAIL"))
	private void patchwork$equipBowInSoulSandValleys(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason spawnReason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
		WitherSkeleton skeleton = (WitherSkeleton) (Object) this;
		if (SoulSandValleySpawns.isSoulSandValley(level, skeleton.blockPosition())) {
			SoulSandValleySpawns.equipBow(skeleton);
		}
	}
}
