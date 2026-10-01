package com.JulieISBaka.patchwork.mixin;

import java.util.Optional;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerVariantAccessor {
	@Invoker("setVariant")
	void patchwork$setVariant(Optional<DyeColor> color);
}
