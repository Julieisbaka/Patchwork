package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock.AttachmentType;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class WolfBannerLayer extends RenderLayer<WolfRenderState, WolfModel> {
	private final BannerRenderer bannerRenderer;

	public WolfBannerLayer(RenderLayerParent<WolfRenderState, WolfModel> renderer, EntityModelSet models, SpriteGetter sprites) {
		super(renderer);
		this.bannerRenderer = new BannerRenderer(models, sprites);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodes, int light, WolfRenderState state, float yRot, float xRot) {
		ItemStack banner = ((WolfBannerState)state).patchwork$getBanner();
		if (!PatchworkConfig.settings().wolfBanners() || !(banner.getItem() instanceof BannerItem bannerItem) || state.isInvisible) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.0F, state.isSitting ? 0.56F : 0.72F, state.isSitting ? 0.15F : 0.12F);
		poseStack.scale(0.24F, 0.24F, 0.24F);
		this.bannerRenderer.submitSpecial(
			AttachmentType.GROUND, poseStack, nodes, light, OverlayTexture.NO_OVERLAY,
			bannerItem.getColor(), banner.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY), state.outlineColor
		);
		poseStack.popPose();
	}
}
