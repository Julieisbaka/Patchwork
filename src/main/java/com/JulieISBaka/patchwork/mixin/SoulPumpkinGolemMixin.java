package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PumpkinLanterns;
import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.SoulGolems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public class SoulPumpkinGolemMixin {
	@Inject(method = "trySpawnGolem", at = @At("HEAD"), cancellable = true)
	private void patchwork$spawnSoulGolem(Level level, BlockPos pos, CallbackInfo ci) {
		if (level.getBlockState(pos).is(PumpkinLanterns.SOUL_BLOCK)
				&& (!PatchworkConfig.settings().soulGolems()
						|| !PatchworkConfig.settings().soulCopperLightBlocks())) {
			ci.cancel();
		} else if (SoulGolems.trySpawn(level, pos)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = { "getOrCreateIronGolemFull",
			"getOrCreateCopperGolemFull" }, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/CarvedPumpkinBlock;PUMPKINS_PREDICATE:Ljava/util/function/Predicate;"))
	private Predicate<BlockState> patchwork$acceptSoulLantern(Predicate<BlockState> original) {
		return PatchworkConfig.settings().soulCopperLightBlocks()
				? original.or(state -> state.is(PumpkinLanterns.SOUL_BLOCK)) : original;
	}
}
