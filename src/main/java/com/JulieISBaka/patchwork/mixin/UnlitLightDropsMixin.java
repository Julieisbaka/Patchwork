package com.JulieISBaka.patchwork.mixin;

import java.util.List;
import com.JulieISBaka.patchwork.LightVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class UnlitLightDropsMixin {
	@Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
	private void patchwork$unlitDrops(BlockState state, LootParams.Builder params,
			CallbackInfoReturnable<List<ItemStack>> cir) {
		var item = LightVariants.unlitItem(state);
		if (item != null) {
			cir.setReturnValue(cir.getReturnValue().stream().map(stack -> stack.is(state.getBlock().asItem())
					? stack.transmuteCopy(item) : stack).toList());
		}
	}

	@Inject(method = "getCloneItemStack", at = @At("RETURN"), cancellable = true)
	private void patchwork$pickUnlit(LevelReader level, BlockPos pos, BlockState state, boolean includeData,
			CallbackInfoReturnable<ItemStack> cir) {
		var item = LightVariants.unlitItem(state);
		if (item != null) {
			cir.setReturnValue(cir.getReturnValue().transmuteCopy(item));
		}
	}
}
