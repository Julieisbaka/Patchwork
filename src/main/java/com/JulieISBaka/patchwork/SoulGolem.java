package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class SoulGolem extends IronGolem {
	public SoulGolem(EntityType<? extends SoulGolem> type, Level level) {
		super(type, level);
		setPlayerCreated(true);
		setPersistenceRequired();
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			target.igniteForSeconds(2.0F);
		}
		return hit;
	}
}
