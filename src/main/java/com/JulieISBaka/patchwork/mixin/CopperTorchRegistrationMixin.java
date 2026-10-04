package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.CopperTorches;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Blocks.class)
public abstract class CopperTorchRegistrationMixin {
	@WrapOperation(method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
		at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
	private static Object patchwork$weatherCopperTorches(Function<BlockBehaviour.Properties, Block> factory,
		Object value, Operation<Object> original, ResourceKey<Block> key,
		Function<BlockBehaviour.Properties, Block> originalFactory, BlockBehaviour.Properties properties) {
		if (key.identifier().getNamespace().equals("minecraft")) {
			if (key.identifier().getPath().equals("copper_torch")) {
				return new CopperTorches.WeatheringTorchBlock(WeatherState.UNAFFECTED, false, properties.randomTicks());
			} else if (key.identifier().getPath().equals("copper_wall_torch")) {
				return new CopperTorches.WeatheringWallTorchBlock(WeatherState.UNAFFECTED, false, properties.randomTicks());
			}
		}
		return original.call(factory, value);
	}
}
