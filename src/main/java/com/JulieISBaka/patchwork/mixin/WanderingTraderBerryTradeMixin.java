package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderBerryTradeMixin {
	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void patchwork$addSweetBerryTrade(ServerLevel level, CallbackInfo ci) {
		if (!PatchworkConfig.settings().sweetBerryTrades()) {
			return;
		}

		WanderingTrader trader = (WanderingTrader) (Object) this;
		boolean alreadyOffered = trader.getOffers().stream()
				.anyMatch(offer -> offer.getResult().is(Items.SWEET_BERRIES));
		if (!alreadyOffered) {
			trader.getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD),
					new ItemStack(Items.SWEET_BERRIES, 6), 12, 1, 0.05F));
		}
	}
}
