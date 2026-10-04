package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.client.ExperienceOrbState;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ExperienceOrbRenderState.class)
public class ExperienceOrbRenderStateMixin implements ExperienceOrbState {
	@Unique private float patchwork$scale = 1.0F;

	@Override
	public float patchwork$scale() {
		return patchwork$scale;
	}

	@Override
	public void patchwork$setScale(float scale) {
		patchwork$scale = scale;
	}
}
