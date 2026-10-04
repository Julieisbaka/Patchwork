package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;

public class SoulGolem extends IronGolem {
	public SoulGolem(EntityType<? extends SoulGolem> type, Level level) {
		super(type, level);
		setPlayerCreated(true);
		setPersistenceRequired();
	}
}
