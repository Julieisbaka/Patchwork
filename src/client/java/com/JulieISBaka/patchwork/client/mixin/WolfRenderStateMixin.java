package com.JulieISBaka.patchwork.client.mixin;

import com.JulieISBaka.patchwork.client.WolfBannerState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WolfRenderState.class)
public class WolfRenderStateMixin implements WolfBannerState {
	@Unique
	private ItemStack patchwork$banner = ItemStack.EMPTY;

	@Override
	public ItemStack patchwork$getBanner() {
		return this.patchwork$banner;
	}

	@Override
	public void patchwork$setBanner(ItemStack banner) {
		this.patchwork$banner = banner;
	}
}
