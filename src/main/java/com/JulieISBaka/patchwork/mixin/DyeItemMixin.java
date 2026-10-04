package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeItem.class)
public class DyeItemMixin {
	@Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
	private void patchwork$dyeShulker(ItemStack dye, Player player, LivingEntity target, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (PatchworkConfig.settings().shulkerDyeing() && target instanceof Shulker shulker && shulker.isAlive()) {
			DyeColor color = dye.get(DataComponents.DYE);
			if (color != null && shulker.getColor() != color) {
				shulker.level().playSound(player, shulker, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
				if (!shulker.level().isClientSide()) {
					((ShulkerVariantAccessor) shulker).patchwork$setVariant(Optional.of(color));
					dye.consume(1, player);
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}
}
