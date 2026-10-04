package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InstrumentItem.class)
public class CallGoatHornMixin {
	@Inject(method = "use", at = @At("RETURN"))
	private void patchwork$recallPets(Level level, Player player, InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir) {
		if (!PatchworkConfig.settings().callHornRecall() || !(level instanceof ServerLevel serverLevel)
			|| !cir.getReturnValue().consumesAction()) {
			return;
		}

		ItemStack stack = player.getItemInHand(hand);
		InstrumentComponent instrument = stack.get(DataComponents.INSTRUMENT);
		if (!stack.is(Items.GOAT_HORN) || instrument == null
			|| !instrument.instrument().is(Instruments.CALL_GOAT_HORN)) {
			return;
		}

		int radius = PatchworkConfig.callHornRecallRadius();
		for (TamableAnimal pet : serverLevel.getEntitiesOfClass(TamableAnimal.class,
			player.getBoundingBox().inflate(radius), pet -> pet.isAlive() && pet.isTame() && pet.isOrderedToSit()
				&& pet.isOwnedBy(player) && pet.distanceToSqr(player) <= (double) radius * radius)) {
			pet.tryToTeleportToOwner();
			if (pet.distanceToSqr(player) <= 25.0) {
				pet.setOrderedToSit(false);
				pet.setInSittingPose(false);
			}
		}
	}
}
