package com.JulieISBaka.patchwork.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneMudMixin {
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void patchwork$growOnMudWithoutWater(BlockState state, LevelReader level, BlockPos pos,
			CallbackInfoReturnable<Boolean> cir) {
		if (level.getBlockState(pos.below()).is(Blocks.MUD)) {
			cir.setReturnValue(true);
		}
	}
}
