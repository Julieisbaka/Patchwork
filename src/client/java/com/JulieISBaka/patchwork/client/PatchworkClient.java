package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.PotionCauldronEntity;
import com.JulieISBaka.patchwork.PotionCauldrons;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.item.alchemy.PotionContents;

public class PatchworkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockColorRegistry.register((state, world, pos, colors) -> {
			if (world.getBlockEntity(pos) instanceof PotionCauldronEntity cauldron) {
				colors.add(cauldron.potion().getColor());
			} else {
				colors.add(PotionContents.BASE_POTION_COLOR);
			}
		}, PotionCauldrons.BLOCK);
	}
}
