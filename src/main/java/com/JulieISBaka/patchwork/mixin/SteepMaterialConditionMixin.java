package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SteepMaterialConditionFix;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context$SteepMaterialCondition")
public abstract class SteepMaterialConditionMixin implements SteepMaterialConditionAccessor {

	@Overwrite
	protected boolean compute() {
		SurfaceRulesContextAccessor context = patchwork$getContext();
		int x = context.patchwork$getBlockX() & 15;
		int z = context.patchwork$getBlockZ() & 15;
		ChunkAccess chunk = context.patchwork$getChunk();
		int north = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x,
				Math.max(z - 1, 0));
		int south = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x,
				Math.min(z + 1, 15));
		if (SteepMaterialConditionFix.isSteep(north, south)) {
			return true;
		}
		int west = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.max(x - 1, 0), z);
		int east = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.min(x + 1, 15), z);
		return SteepMaterialConditionFix.isSteep(west, east);
	}
}
