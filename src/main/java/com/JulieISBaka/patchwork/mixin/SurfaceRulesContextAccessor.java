package com.JulieISBaka.patchwork.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public interface SurfaceRulesContextAccessor {
	@Accessor("blockX")
	int patchwork$getBlockX();

	@Accessor("blockZ")
	int patchwork$getBlockZ();

	@Accessor("chunk")
	ChunkAccess patchwork$getChunk();
}
