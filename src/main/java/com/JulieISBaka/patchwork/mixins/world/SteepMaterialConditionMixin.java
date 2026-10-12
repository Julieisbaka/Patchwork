package com.JulieISBaka.patchwork.mixins.world;

import com.JulieISBaka.patchwork.features.world.SteepMaterialConditionFix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(targets = "net.minecraft.world.level.levelgen.material.condition.SteepCondition$1")
public abstract class SteepMaterialConditionMixin implements SteepMaterialConditionAccessor {

	@Overwrite
	protected boolean compute() {
		MaterialRuleContextAccessor context = patchwork$getContext();
		return SteepMaterialConditionFix.isSteep(context.patchwork$getSurfaceGradientX(),
				context.patchwork$getSurfaceGradientZ());
	}
}
