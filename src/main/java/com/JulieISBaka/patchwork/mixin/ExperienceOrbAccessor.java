package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
	@Accessor("count") int patchwork$count();
	@Accessor("age") int patchwork$age();
}
