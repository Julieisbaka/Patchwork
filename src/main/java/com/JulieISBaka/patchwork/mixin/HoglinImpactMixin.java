package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.HoglinLaunchTarget;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class HoglinImpactMixin implements HoglinLaunchTarget {
	@Unique
	private int patchwork$impactTicks;
	@Unique
	private static final ResourceKey<DamageType> PATCHWORK$IMPACT = ResourceKey.create(Registries.DAMAGE_TYPE,
			Patchwork.id("hoglin_impact"));

	@Override
	public void patchwork$trackHoglinLaunch() {
		this.patchwork$impactTicks = 20;
	}

	@Override
	public void patchwork$checkHoglinImpact() {
		if (this.patchwork$impactTicks <= 0) {
			return;
		}
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (!PatchworkConfig.settings().hoglinCharge() || !player.isAlive() || player.onGround()) {
			this.patchwork$impactTicks = 0;
		} else if (player.horizontalCollision || (player.verticalCollision && !player.verticalCollisionBelow)) {
			this.patchwork$impactTicks = 0;
			player.hurtServer(player.level(), player.damageSources().source(PATCHWORK$IMPACT), 2.0F);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$expireImpact(CallbackInfo ci) {
		if (this.patchwork$impactTicks > 0) {
			this.patchwork$impactTicks--;
		}
	}
}
