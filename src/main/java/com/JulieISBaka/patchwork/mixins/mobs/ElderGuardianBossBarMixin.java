package com.JulieISBaka.patchwork.mixins.mobs;

import com.JulieISBaka.patchwork.features.mobs.ElderGuardianBossAccess;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ElderGuardian.class)
public abstract class ElderGuardianBossBarMixin implements ElderGuardianBossAccess {
	@Unique
	private ServerBossEvent patchwork$bossEvent;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void patchwork$createBossBar(CallbackInfo ci) {
		this.patchwork$bossEvent = new ServerBossEvent(UUID.randomUUID(),
				Component.translatable("entity.minecraft.elder_guardian"), BossEvent.BossBarColor.BLUE,
				BossEvent.BossBarOverlay.PROGRESS);
	}

	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void patchwork$updateBossBar(net.minecraft.server.level.ServerLevel level, CallbackInfo ci) {
		ElderGuardian guardian = (ElderGuardian) (Object) this;
		this.patchwork$bossEvent.setProgress(guardian.getHealth() / guardian.getMaxHealth());
	}

	@Override
	public ServerBossEvent patchwork$getBossEvent() {
		return this.patchwork$bossEvent;
	}
}
