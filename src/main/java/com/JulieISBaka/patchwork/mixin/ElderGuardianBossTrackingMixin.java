package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.ElderGuardianBossAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ElderGuardianBossTrackingMixin {
	@Inject(method = "startSeenByPlayer", at = @At("TAIL"))
	private void patchwork$showElderGuardianBossBar(ServerPlayer player, CallbackInfo ci) {
		if ((Object) this instanceof ElderGuardian
				&& (Object) this instanceof ElderGuardianBossAccess access) {
			access.patchwork$getBossEvent().addPlayer(player);
		}
	}

	@Inject(method = "stopSeenByPlayer", at = @At("TAIL"))
	private void patchwork$hideElderGuardianBossBar(ServerPlayer player, CallbackInfo ci) {
		if ((Object) this instanceof ElderGuardian
				&& (Object) this instanceof ElderGuardianBossAccess access) {
			access.patchwork$getBossEvent().removePlayer(player);
		}
	}
}
