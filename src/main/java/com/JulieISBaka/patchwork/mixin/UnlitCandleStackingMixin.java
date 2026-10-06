package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.LightVariants;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CandleBlock.class)
public class UnlitCandleStackingMixin {
	@Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
	private void patchwork$stackUnlit(BlockState state, BlockPlaceContext context,
			CallbackInfoReturnable<Boolean> cir) {
		if (!context.isSecondaryUseActive() && state.getValue(CandleBlock.CANDLES) < 4
				&& LightVariants.isUnlitItemFor(context.getItemInHand(), state.getBlock())) {
			cir.setReturnValue(true);
		}
	}
}
