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
	@WrapOperation(method = "<clinit>", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/Blocks;register(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;"))
	private static Block patchwork$weatherCopperTorches(ResourceKey<Block> key,
		Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties, Operation<Block> original) {
		if (key.identifier().getNamespace().equals("minecraft")) {
			if (key.identifier().getPath().equals("copper_torch")) {
				factory = props -> new CopperTorches.WeatheringTorchBlock(WeatherState.UNAFFECTED, false, props);
				properties.randomTicks();
			} else if (key.identifier().getPath().equals("copper_wall_torch")) {
				factory = props -> new CopperTorches.WeatheringWallTorchBlock(WeatherState.UNAFFECTED, false, props);
				properties.randomTicks();
			}
		}
		return original.call(key, factory, properties);
	}
}
