package com.JulieISBaka.patchwork.features.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(targets = "net.minecraft.world.level.levelgen.material.condition.SteepCondition$1")
public interface SteepMaterialConditionAccessor {
	@Accessor("context")
	MaterialRuleContextAccessor patchwork$getContext();
}
