package com.JulieISBaka.patchwork;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class VillagerRoseOffering {
	private VillagerRoseOffering() {
	}

	public static boolean offerToBaby(ServerLevel level, IronGolem golem, Villager villager) {
		if (!villager.isBaby()) {
			return false;
		}
		ItemEntity rose = new ItemEntity(level, golem.getX(), golem.getY() + 1.0, golem.getZ(),
				new ItemStack(Items.POPPY));
		rose.setPickUpDelay(0);
		rose.setDeltaMovement((villager.getX() - golem.getX()) * 0.1, 0.2,
				(villager.getZ() - golem.getZ()) * 0.1);
		return level.addFreshEntity(rose);
	}
}
