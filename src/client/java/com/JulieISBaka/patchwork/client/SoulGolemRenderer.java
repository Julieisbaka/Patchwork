package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PumpkinLanterns;
import com.JulieISBaka.patchwork.SoulGolem;
import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.client.renderer.entity.layers.SnowGolemHeadLayer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.resources.Identifier;

public class SoulGolemRenderer extends MobRenderer<SoulGolem, SnowGolemRenderState, SnowGolemModel> {
	private static final Identifier TEXTURE = Patchwork.id("textures/entity/soul_golem.png");
	private final BlockModelResolver blockModelResolver;

	public SoulGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new SnowGolemModel(context.bakeLayer(ModelLayers.SNOW_GOLEM)), 0.5F);
		blockModelResolver = context.getBlockModelResolver();
		addLayer(new SnowGolemHeadLayer(this));
	}

	@Override
	public Identifier getTextureLocation(SnowGolemRenderState state) {
		return TEXTURE;
	}

	@Override
	public SnowGolemRenderState createRenderState() {
		return new SnowGolemRenderState();
	}

	@Override
	public void extractRenderState(SoulGolem golem, SnowGolemRenderState state, float partialTick) {
		super.extractRenderState(golem, state, partialTick);
		blockModelResolver.update(state.headBlock, PumpkinLanterns.SOUL_BLOCK.defaultBlockState(),
			SnowGolemRenderer.BLOCK_DISPLAY_CONTEXT);
	}
}
