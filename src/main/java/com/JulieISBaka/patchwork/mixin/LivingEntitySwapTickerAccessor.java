package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntitySwapTickerAccessor {
	@Accessor("itemSwapTicker")
	void patchwork$setItemSwapTicker(int itemSwapTicker);
}
