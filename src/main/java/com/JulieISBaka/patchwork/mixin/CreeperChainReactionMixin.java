package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class CreeperChainReactionMixin {
	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void patchwork$primeCreeper(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		if (PatchworkConfig.settings().creeperChainReactions()
			&& cir.getReturnValue()
			&& (Object)this instanceof Creeper creeper
			&& creeper.isAlive()
			&& source.is(DamageTypeTags.IS_EXPLOSION)
			&& source.getDirectEntity() instanceof Creeper other
			&& other != creeper) {
			creeper.ignite();
		}
	}
}
