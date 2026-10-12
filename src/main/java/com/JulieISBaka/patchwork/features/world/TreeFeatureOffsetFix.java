package com.JulieISBaka.patchwork.features.world;

public final class TreeFeatureOffsetFix {
	private TreeFeatureOffsetFix() {
	}

	public static int correctStartOffset(int offset, int sizeAtBase) {
		return offset == -1 && sizeAtBase == 1 ? 0 : offset;
	}
}
