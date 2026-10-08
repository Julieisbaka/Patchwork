package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public final class WanderingTraderTrades {
	private WanderingTraderTrades() {
	}

	public static void addSweetBerries(WanderingTrader trader) {
		if (!PatchworkConfig.settings().sweetBerryTrades()) {
			return;
		}
		if (trader.getOffers().stream().noneMatch(offer -> offer.getResult().is(Items.SWEET_BERRIES))) {
			trader.getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD),
					new ItemStack(Items.SWEET_BERRIES, 6), 12, 1, 0.05F));
		}
	}
}
