package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.features.blocks.PotionCauldronEntity;
import com.JulieISBaka.patchwork.features.blocks.PotionCauldrons;
import com.JulieISBaka.patchwork.features.blocks.Woodcutter;
import com.JulieISBaka.patchwork.features.items.SoulFireCharges;
import com.JulieISBaka.patchwork.features.mobs.SoulGolems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.item.alchemy.PotionContents;


public class PatchworkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(Woodcutter.MENU_TYPE, StonecutterScreen::new);
		EntityRenderers.register(SoulGolems.TYPE, SoulGolemRenderer::new);
		EntityRenderers.register(SoulFireCharges.PROJECTILE,
				context -> new ThrownItemRenderer<>(context, 0.5F, true));
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, layers, context) -> {
			if (renderer instanceof WolfRenderer wolfRenderer) {
				layers.register(new WolfBannerLayer(wolfRenderer, context.getModelSet(), context.getSprites()));
			}
		});
		BlockColorRegistry.register((state, world, pos, colors) -> {
			if (world.getBlockEntity(pos) instanceof PotionCauldronEntity cauldron) {
				colors.add(cauldron.potion().getColor());
			} else {
				colors.add(PotionContents.BASE_POTION_COLOR);
			}
		}, PotionCauldrons.BLOCK);
	}
}
