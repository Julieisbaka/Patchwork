package com.JulieISBaka.patchwork.client.mixin;

import java.util.List;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
	@Accessor("layers")
	List<?> patchwork$getLayers();
}
