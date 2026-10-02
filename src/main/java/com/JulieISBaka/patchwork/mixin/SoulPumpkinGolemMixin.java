package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PumpkinLanterns;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.function.Predicate;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CarvedPumpkinBlock.class)
public class SoulPumpkinGolemMixin {
	@ModifyExpressionValue(
		method = {"getOrCreateSnowGolemFull", "getOrCreateIronGolemFull", "getOrCreateCopperGolemFull"},
		at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/CarvedPumpkinBlock;PUMPKINS_PREDICATE:Ljava/util/function/Predicate;")
	)
	private Predicate<BlockState> patchwork$acceptSoulLantern(Predicate<BlockState> original) {
		return original.or(state -> state.is(PumpkinLanterns.SOUL_BLOCK));
	}
}
