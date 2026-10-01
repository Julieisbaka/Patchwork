package com.JulieISBaka.patchwork.mixin;

import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$initializeHealth(EntityType<? extends WitherBoss> type, Level level, CallbackInfo ci) {
		if (!level.isClientSide()) {
			this.patchwork$updateHealth(level.getDifficulty());
		}
	}

	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void patchwork$updateHealthForDifficulty(ServerLevel level, CallbackInfo ci) {
		this.patchwork$updateHealth(level.getDifficulty());
	}

	private void patchwork$updateHealth(Difficulty difficulty) {
		WitherBoss wither = (WitherBoss)(Object)this;
		AttributeInstance maxHealth = Objects.requireNonNull(wither.getAttribute(Attributes.MAX_HEALTH));
		double targetHealth = switch (difficulty) {
			case EASY, PEACEFUL -> 300.0;
			case NORMAL -> 450.0;
			case HARD -> 600.0;
		};

		if (maxHealth.getBaseValue() != targetHealth) {
			float healthFraction = wither.getHealth() / wither.getMaxHealth();
			maxHealth.setBaseValue(targetHealth);
			wither.setHealth(healthFraction * wither.getMaxHealth());
		}
	}
}
