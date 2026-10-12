package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SteepMaterialConditionFix;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.level.levelgen.material.condition.SteepCondition$1")
public abstract class SteepMaterialConditionMixin implements SteepMaterialConditionAccessor {

	@Overwrite
	protected boolean compute() {
		MaterialRuleContextAccessor context = patchwork$getContext();
		return SteepMaterialConditionFix.isSteep(context.patchwork$getSurfaceGradientX(),
				context.patchwork$getSurfaceGradientZ());
	}
}
