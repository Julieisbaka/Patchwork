package com.JulieISBaka.patchwork.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context$SteepMaterialCondition")
public interface SteepMaterialConditionAccessor {
	@Accessor("context")
	SurfaceRulesContextAccessor patchwork$getContext();
}
