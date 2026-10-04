package com.JulieISBaka.patchwork;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class SoulFireSupport {
	public static final BooleanProperty CHARGE_PLACED = BooleanProperty.create("patchwork_charge_placed");

	private SoulFireSupport() {
	}

	public static BlockState chargeFire() {
		return Blocks.SOUL_FIRE.defaultBlockState().setValue(CHARGE_PLACED, true);
	}
}
