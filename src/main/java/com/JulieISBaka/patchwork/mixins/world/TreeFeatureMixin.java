package com.JulieISBaka.patchwork.mixins.world;

import com.JulieISBaka.patchwork.features.world.TreeFeatureOffsetFix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {
	@Shadow
	@Final
	private FeatureSize minimumSize;

	@ModifyVariable(method = "getMaxFreeTreeHeight",
			at = @At(value = "STORE", ordinal = 0), index = 7)
	private int patchwork$correctXStartOffset(int offset, WorldGenLevel level, int treeHeight, BlockPos pos) {
		return TreeFeatureOffsetFix.correctStartOffset(offset, this.minimumSize.getSizeAtHeight(treeHeight, 0));
	}

	@ModifyVariable(method = "getMaxFreeTreeHeight",
			at = @At(value = "STORE", ordinal = 0), index = 8)
	private int patchwork$correctZStartOffset(int offset, WorldGenLevel level, int treeHeight, BlockPos pos) {
		return TreeFeatureOffsetFix.correctStartOffset(offset, this.minimumSize.getSizeAtHeight(treeHeight, 0));
	}
}
