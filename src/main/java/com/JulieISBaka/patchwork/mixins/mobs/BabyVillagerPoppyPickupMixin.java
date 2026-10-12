package com.JulieISBaka.patchwork.mixins.mobs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Villager.class)
public abstract class BabyVillagerPoppyPickupMixin {
	@Inject(method = "wantsToPickUp", at = @At("RETURN"), cancellable = true)
	private void patchwork$babyVillagersAcceptPoppies(ServerLevel level, ItemStack itemStack,
			CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && (Object) this instanceof Villager villager && villager.isBaby()
				&& itemStack.is(Items.POPPY) && villager.getInventory().canAddItem(itemStack)) {
			cir.setReturnValue(true);
		}
	}
}
