package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerCombatMixin {
	@Shadow
	@Final
	private Inventory inventory;
	@Unique
	private int patchwork$lastSelectedSlot = -1;
	@Unique
	private boolean patchwork$selectedSlotChanged;
	@Unique
	private boolean patchwork$wasSprinting;

	@Inject(method = "tick", at = @At("HEAD"))
	private void patchwork$checkSelectedSlot(CallbackInfo ci) {
		int selectedSlot = this.inventory.getSelectedSlot();
		this.patchwork$selectedSlotChanged = this.patchwork$lastSelectedSlot >= 0
				&& selectedSlot != this.patchwork$lastSelectedSlot;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$rememberSelectedSlot(CallbackInfo ci) {
		this.patchwork$lastSelectedSlot = this.inventory.getSelectedSlot();
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	private void patchwork$preserveAttackStrengthOnSlotSwap(Player player) {
		if (this.patchwork$selectedSlotChanged) {
			((LivingEntitySwapTickerAccessor) (Object) this).patchwork$setItemSwapTicker(0);
		} else {
			player.resetAttackStrengthTicker();
		}
	}

	@Inject(method = "attack", at = @At("HEAD"))
	private void patchwork$rememberSprintState(CallbackInfo ci) {
		this.patchwork$wasSprinting = ((Player) (Object) this).isSprinting();
	}

	@Inject(method = "attack", at = @At("RETURN"))
	private void patchwork$keepSprintingAfterAttack(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (this.patchwork$wasSprinting && !player.isSprinting()) {
			player.setSprinting(true);
		}
	}
}
