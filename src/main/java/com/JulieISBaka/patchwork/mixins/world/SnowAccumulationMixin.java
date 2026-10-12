package com.JulieISBaka.patchwork.mixins.world;

import com.JulieISBaka.patchwork.features.world.SnowAccumulation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerLevel.class)
public abstract class SnowAccumulationMixin {
	@Inject(method = "tickPrecipitation", at = @At("TAIL"))
	private void patchwork$accumulateSnowUnderLeaves(BlockPos pos, CallbackInfo ci) {
		SnowAccumulation.onPrecipitation((ServerLevel) (Object) this, pos);
	}
}
