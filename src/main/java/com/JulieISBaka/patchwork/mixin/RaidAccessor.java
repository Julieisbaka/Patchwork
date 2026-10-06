package com.JulieISBaka.patchwork.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Raid.class)
public interface RaidAccessor {
	@Invoker("spawnGroup")
	void patchwork$spawnGroup(ServerLevel level, BlockPos pos);
}
