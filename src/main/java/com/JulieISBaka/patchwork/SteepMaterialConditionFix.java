package com.JulieISBaka.patchwork;

public final class SteepMaterialConditionFix {
	private SteepMaterialConditionFix() {
	}

	public static boolean isSteep(int firstHeight, int secondHeight) {
		return Math.abs((long) firstHeight - secondHeight) >= 4;
	}
}
