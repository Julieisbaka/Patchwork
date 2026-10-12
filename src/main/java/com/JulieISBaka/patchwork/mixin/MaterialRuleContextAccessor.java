package com.JulieISBaka.patchwork.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.level.levelgen.material.MaterialRuleContext")
public interface MaterialRuleContextAccessor {
	@Invoker("surfaceGradientX")
	int patchwork$getSurfaceGradientX();

	@Invoker("surfaceGradientZ")
	int patchwork$getSurfaceGradientZ();
}
