package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.PotionCauldrons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class PotionArrowItemMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$dipDroppedArrows(CallbackInfo ci) {
		ItemEntity item = (ItemEntity) (Object) this;
		if (!PatchworkConfig.settings().potionCauldrons() || item.isRemoved()
			|| !(item.level() instanceof ServerLevel level) || !item.getItem().is(Items.ARROW)) {
			return;
		}
		BlockPos pos = item.blockPosition();
		if (level.getBlockState(pos).is(PotionCauldrons.BLOCK) && item.getX() > pos.getX() + 0.125
			&& item.getX() < pos.getX() + 0.875 && item.getZ() > pos.getZ() + 0.125 && item.getZ() < pos.getZ() + 0.875
			&& item.getY() < pos.getY() + (6
				+ 3 * level.getBlockState(pos).getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL))
				/ 16.0) {
			PotionCauldrons.dipDropped(level, pos, item);
		}
	}
}
