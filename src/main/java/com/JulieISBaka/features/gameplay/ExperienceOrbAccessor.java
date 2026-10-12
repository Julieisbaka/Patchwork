package com.JulieISBaka.patchwork.features.gameplay;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
	@Accessor("count")
	int patchwork$count();

	@Accessor("count")
	void patchwork$setCount(int count);

	@Accessor("age")
	int patchwork$age();
}
