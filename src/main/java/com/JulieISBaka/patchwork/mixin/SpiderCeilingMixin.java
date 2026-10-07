package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public abstract class SpiderCeilingMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$clingToCeilings(CallbackInfo ci) {
		Spider spider = (Spider) (Object) this;
		BlockPos above = BlockPos.containing(spider.getX(), spider.getY() + spider.getBbHeight() + 0.01,
				spider.getZ());
		boolean ceiling = PatchworkConfig.settings().spiderCeilingClimbing()
				&& spider.level().getBlockState(above).isFaceSturdy(spider.level(), above, Direction.DOWN);
		spider.setNoGravity(ceiling);
		if (ceiling) {
			spider.setClimbing(true);
		}
	}
}
