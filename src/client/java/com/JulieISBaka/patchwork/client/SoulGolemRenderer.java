package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.Patchwork;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

public class SoulGolemRenderer extends IronGolemRenderer {
	private static final Identifier TEXTURE = Patchwork.id("textures/entity/soul_golem.png");

	public SoulGolemRenderer(EntityRendererProvider.Context context) {
		super(context);
		layers.removeIf(layer -> layer instanceof IronGolemCrackinessLayer);
	}

	@Override
	public Identifier getTextureLocation(IronGolemRenderState state) {
		return TEXTURE;
	}
}
