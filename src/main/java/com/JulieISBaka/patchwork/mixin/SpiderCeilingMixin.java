package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SpiderCeiling;
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
		boolean ceiling = SpiderCeiling.isClingingToCeiling(spider);
		spider.setNoGravity(ceiling);
		if (ceiling) {
			spider.setClimbing(true);
		}
	}
}
