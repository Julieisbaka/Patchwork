package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SoulGolem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class SoulGolemOwnerMixin {
	@Inject(method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"))
	private void patchwork$assignEggOwner(ServerLevel level, ItemStack stack, LivingEntity user, BlockPos pos,
		EntitySpawnReason reason, boolean align, boolean invert, CallbackInfoReturnable<Entity> cir) {
		if (cir.getReturnValue() instanceof SoulGolem golem && user instanceof Player player) {
			golem.setOwner(player);
		}
	}
}
