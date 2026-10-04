package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$initializeHealth(EntityType<? extends WitherBoss> type, Level level, CallbackInfo ci) {
		if (!level.isClientSide() && PatchworkConfig.settings().witherDifficultyHealth()) {
			this.patchwork$updateHealth(level.getDifficulty());
		}
	}

	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void patchwork$updateHealthForDifficulty(ServerLevel level, CallbackInfo ci) {
		if (PatchworkConfig.settings().witherDifficultyHealth()) {
			this.patchwork$updateHealth(level.getDifficulty());
		}
	}

	@Redirect(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
	private void patchwork$boostBirthExplosion(ServerLevel level, Entity source, double x, double y, double z,
		float radius, boolean fire, Level.ExplosionInteraction interaction) {
		if (!PatchworkConfig.settings().witherBirthExplosion()) {
			level.explode(source, x, y, z, radius, fire, interaction);
			return;
		}
		level.explode(source, Explosion.getDefaultDamageSource(level, source),
			new EntityBasedExplosionDamageCalculator(source) {
				@Override
				public float getEntityDamageAmount(Explosion explosion, Entity entity, float exposure) {
					return super.getEntityDamageAmount(explosion, entity, exposure) + 3.0F;
				}
			}, x, y, z, radius, fire, interaction);
	}

	private void patchwork$updateHealth(Difficulty difficulty) {
		WitherBoss wither = (WitherBoss) (Object) this;
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
