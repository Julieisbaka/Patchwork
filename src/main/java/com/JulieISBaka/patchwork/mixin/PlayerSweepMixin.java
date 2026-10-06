package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.RabbitPet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerSweepMixin {
	@Redirect(method = "doSweepAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean patchwork$protectOwnedPetsFromSweep(Player player, Entity nearby) {
		return player.isAlliedTo(nearby) || PatchworkConfig.settings().ownerSweepProtection()
				&& (nearby instanceof Wolf wolf && wolf.isTame() && wolf.isOwnedBy(player)
						|| nearby instanceof Cat cat && cat.isTame() && cat.isOwnedBy(player)
						|| nearby instanceof RabbitPet pet && pet.patchwork$isOwnedBy(player));
	}
}
