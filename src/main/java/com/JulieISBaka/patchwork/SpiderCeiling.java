package com.JulieISBaka.patchwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.spider.Spider;

/** Shared ceiling-contact checks for spider movement and rendering. */
public final class SpiderCeiling {
	private SpiderCeiling() {
	}

	/** Returns whether spider ceiling climbing is enabled and the spider touches a sturdy ceiling. */
	public static boolean isClingingToCeiling(Spider spider) {
		BlockPos above = BlockPos.containing(spider.getX(), spider.getY() + spider.getBbHeight() + 0.01,
				spider.getZ());
		return PatchworkConfig.settings().spiderCeilingClimbing()
				&& spider.level().getBlockState(above).isFaceSturdy(spider.level(), above, Direction.DOWN);
	}
}
