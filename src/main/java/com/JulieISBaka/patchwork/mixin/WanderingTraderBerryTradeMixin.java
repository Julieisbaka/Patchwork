package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.WanderingTraderTrades;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderBerryTradeMixin {
	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void patchwork$addSweetBerryTrade(ServerLevel level, CallbackInfo ci) {
		WanderingTraderTrades.addSweetBerries((WanderingTrader) (Object) this);
	}
}
