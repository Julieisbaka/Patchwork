package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class VillagerFoodBehavior {
	private static final Item[] FOOD_ITEMS = { Items.BREAD, Items.CARROT, Items.POTATO, Items.BEETROOT };

	private VillagerFoodBehavior() {
	}

	public static boolean hasFood(Villager villager) {
		return villager.getInventory().getItems().stream()
				.anyMatch(stack -> !stack.isEmpty() && isFoodItem(stack.getItem()));
	}

	public static int foodItemCount(Villager villager) {
		return villager.getInventory().getItems().stream()
				.filter(stack -> !stack.isEmpty() && isFoodItem(stack.getItem()))
				.mapToInt(stack -> stack.getCount()).sum();
	}

	public static boolean eatToHeal(Villager villager) {
		if (villager.getHealth() >= villager.getMaxHealth() || !hasFood(villager)) {
			return false;
		}
		int foodCount = foodItemCount(villager);
		villager.eatAndDigestFood();
		if (foodItemCount(villager) >= foodCount) {
			return false;
		}
		villager.heal(2.0F);
		return true;
	}

	private static boolean isFoodItem(Item item) {
		for (Item food : FOOD_ITEMS) {
			if (item == food) {
				return true;
			}
		}
		return false;
	}
}
