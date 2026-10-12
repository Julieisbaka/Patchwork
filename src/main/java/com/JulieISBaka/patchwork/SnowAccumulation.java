package com.JulieISBaka.patchwork;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SnowAccumulation {
	private SnowAccumulation() {
	}

	public static Optional<BlockPos> findSnowPositionUnderLeaves(ServerLevel level, BlockPos surfaceTop) {
		BlockPos cursor = surfaceTop;
		int leafLayers = 0;
		while (level.getBlockState(cursor.below()).is(BlockTags.LEAVES)) {
			cursor = cursor.below();
			if (++leafLayers > 2) {
				return Optional.empty();
			}
		}
		return leafLayers > 0 ? Optional.of(cursor.below()) : Optional.empty();
	}

	public static boolean accumulateAt(ServerLevel level, BlockPos pos, int maxHeight) {
		if (maxHeight <= 0) {
			return false;
		}
		BlockState current = level.getBlockState(pos);
		if (current.is(Blocks.SNOW)) {
			int layers = current.getValue(SnowLayerBlock.LAYERS);
			if (layers >= Math.min(maxHeight, SnowLayerBlock.MAX_HEIGHT)) {
				return false;
			}
			BlockState next = current.setValue(SnowLayerBlock.LAYERS, layers + 1);
			Block.pushEntitiesUp(current, next, level, pos);
			return level.setBlockAndUpdate(pos, next);
		}
		BlockState snow = Blocks.SNOW.defaultBlockState();
		return current.isAir() && snow.canSurvive(level, pos) && level.setBlockAndUpdate(pos, snow);
	}

	public static void onPrecipitation(ServerLevel level, BlockPos precipitationPos) {
		if (!level.isRaining()) {
			return;
		}
		int maxHeight = level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MAX_SNOW_ACCUMULATION_HEIGHT);
		if (maxHeight <= 0) {
			return;
		}
		BlockPos surfaceTop = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, precipitationPos);
		Optional<BlockPos> snowPos = findSnowPositionUnderLeaves(level, surfaceTop);
		if (snowPos.isEmpty()) {
			return;
		}
		BlockPos pos = snowPos.get();
		if (level.getBiome(pos).value().shouldSnow(level, pos)) {
			accumulateAt(level, pos, maxHeight);
		}
	}
}
