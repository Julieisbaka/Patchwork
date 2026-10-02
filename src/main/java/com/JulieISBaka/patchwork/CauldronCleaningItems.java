package com.JulieISBaka.patchwork;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class CauldronCleaningItems {
	private static final class CleanedItems {
		private static final Map<Item, Item> VALUES = createCleanedItems();
	}

	private CauldronCleaningItems() {
	}

	public static Item get(Item item) {
		return CleanedItems.VALUES.get(item);
	}

	private static Map<Item, Item> createCleanedItems() {
		Map<Item, Item> items = new HashMap<>();
		Items.WOOL.forEach(wool -> {
			if (wool != Items.WOOL.white()) {
				items.put(wool, Items.WOOL.white());
			}
		});
		Items.DYED_TERRACOTTA.forEach(terracotta -> items.put(terracotta, Items.TERRACOTTA));
		Items.STAINED_GLASS.forEach(glass -> items.put(glass, Items.GLASS));
		return Map.copyOf(items);
	}
}
