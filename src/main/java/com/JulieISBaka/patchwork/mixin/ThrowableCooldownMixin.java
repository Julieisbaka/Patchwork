package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EggItem.class, SnowballItem.class})
public abstract class ThrowableCooldownMixin {
	@Inject(method = "use", at = @At("RETURN"))
	private void patchwork$addThrowCooldown(Level level, Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (cir.getReturnValue().consumesAction()) {
			Item item = (Item) (Object) this;
			player.getCooldowns().addCooldown(item.getDefaultInstance(), 2);
		}
	}
}
