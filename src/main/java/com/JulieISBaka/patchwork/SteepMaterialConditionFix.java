package com.JulieISBaka.patchwork;

public final class SteepMaterialConditionFix {
	private SteepMaterialConditionFix() {
	}

	public static boolean isSteep(int firstHeight, int secondHeight) {
		return Math.abs(firstHeight - secondHeight) >= 4;
	}
}
