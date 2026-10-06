package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerSweepAccessor {
	@Invoker("doSweepAttack")
	void patchwork$doSweepAttack(Entity target, float baseDamage, DamageSource source, float strength);
}
