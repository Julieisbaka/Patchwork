package com.JulieISBaka.patchwork.mixins.mobs;

import com.JulieISBaka.patchwork.features.mobs.EnderDragonBehavior;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {
	@ModifyConstant(method = "createAttributes", constant = @Constant(doubleValue = 200.0))
	private static double patchwork$increaseHealth(double health) {
		return 500.0;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$increaseKnockbackResistance(CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		AttributeInstance resistance = dragon.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (resistance != null) {
			resistance.setBaseValue(Math.min(1.0, resistance.getBaseValue() + 0.5));
		}
	}

	@ModifyArg(method = "aiStep", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"), index = 1)
	private Vec3 patchwork$increaseFlightSpeed(Vec3 movement) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		return dragon.getPhaseManager().getCurrentPhase().isSitting()
				? movement
				: EnderDragonBehavior.increaseFlightSpeed(movement);
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void patchwork$enrageIslandEndermen(CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (!dragon.level().isClientSide() && dragon.tickCount % 600 == 0
				&& dragon.level() instanceof net.minecraft.server.level.ServerLevel level) {
			EnderDragonBehavior.enrageIslandEndermen(level, dragon);
		}
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void patchwork$ignoreCrystalExplosions(ServerLevel level, DamageSource source, float amount,
			CallbackInfoReturnable<Boolean> cir) {
		if (source.getDirectEntity() instanceof EndCrystal) {
			cir.setReturnValue(false);
		}
	}
}
