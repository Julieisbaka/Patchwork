package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	@Accessor("inGroundTime")
	void patchwork$setInGroundTime(int ticks);
}
