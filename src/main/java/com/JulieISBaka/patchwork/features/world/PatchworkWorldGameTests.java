package com.JulieISBaka.patchwork.features.world;

import com.JulieISBaka.patchwork.features.blocks.GardenBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PatchworkWorldGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void snowAccumulatesUnderAtMostTwoLeafLayers(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		BlockPos target = test.absolutePos(CENTER);
		level.setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
		level.setBlockAndUpdate(target.above(), Blocks.OAK_LEAVES.defaultBlockState());
		var oneLayerPosition = SnowAccumulation.findSnowPositionUnderLeaves(level, target.above(2));
		test.assertTrue(oneLayerPosition.isPresent() && oneLayerPosition.get().equals(target),
				"One leaf layer did not resolve to the covered block");
		level.setBlockAndUpdate(target.above(2), Blocks.OAK_LEAVES.defaultBlockState());
		var snowPosition = SnowAccumulation.findSnowPositionUnderLeaves(level, target.above(3));
		test.assertTrue(snowPosition.orElse(null) != null && snowPosition.get().equals(target),
				"Two leaf layers did not resolve to the covered block");
		test.assertTrue(SnowAccumulation.accumulateAt(level, target, 2), "Snow did not accumulate under leaves");
		test.assertTrue(SnowAccumulation.accumulateAt(level, target, 2), "Snow did not gain a second layer");
		test.assertTrue(!SnowAccumulation.accumulateAt(level, target, 2)
				&& level.getBlockState(target).getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS) == 2,
				"Snow exceeded the configured maximum");

		level.setBlockAndUpdate(target.above(3), Blocks.OAK_LEAVES.defaultBlockState());
		test.assertTrue(SnowAccumulation.findSnowPositionUnderLeaves(level, target.above(4)).isEmpty(),
				"Snow accumulated under more than two leaf layers");
		test.succeed();
	}

	@GameTest
	public void steepSurfaceDetectsBothSlopeDirections(GameTestHelper test) {
		test.assertTrue(SteepMaterialConditionFix.isSteep(10, 6),
				"A four-block descending slope was not classified as steep");
		test.assertTrue(SteepMaterialConditionFix.isSteep(6, 10),
				"A four-block ascending slope was not classified as steep");
		test.assertTrue(!SteepMaterialConditionFix.isSteep(10, 7),
				"A slope under four blocks was classified as steep");
		test.succeed();
	}

	@GameTest
	public void soulValleyWitherSkeletonEquipmentIsBowCapable(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		var skeleton = new net.minecraft.world.entity.monster.skeleton.WitherSkeleton(EntityTypes.WITHER_SKELETON,
				level);
		SoulSandValleySpawns.equipBow(skeleton);
		test.assertTrue(skeleton.getMainHandItem().is(Items.BOW),
				"Soul Sand Valley wither skeleton did not receive a bow");
		test.assertTrue(skeleton.canUseNonMeleeWeapon(skeleton.getMainHandItem()),
				"Wither skeleton cannot use its assigned bow");
		test.succeed();
	}

	@GameTest
	public void sugarCaneGrowsOnMudWithoutWater(GameTestHelper test) {
		BlockPos pos = test.absolutePos(CENTER);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			test.getLevel().setBlockAndUpdate(pos.relative(direction), Blocks.AIR.defaultBlockState());
		}
		test.setBlock(CENTER.below(), Blocks.MUD);
		BlockState cane = Blocks.SUGAR_CANE.defaultBlockState();
		test.assertTrue(cane.canSurvive(test.getLevel(), pos), "Sugar cane could not survive on dry mud");
		test.setBlock(CENTER, Blocks.SUGAR_CANE);
		test.assertBlockPresent(Blocks.SUGAR_CANE, CENTER);
		test.setBlock(CENTER.below(), Blocks.DIRT);
		test.assertTrue(!cane.canSurvive(test.getLevel(), pos),
				"Dry dirt incorrectly gained mud's water-free sugar-cane support");
		test.succeed();
	}

	@GameTest
	public void gardenPaeoniaWorldGenerationIsScoped(GameTestHelper test) {
		var access = test.getLevel().registryAccess();
		var feature = access.lookupOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE)
				.getOrThrow(GardenBlocks.PAEONIA_PATCH).value();
		var biomes = access.lookupOrThrow(net.minecraft.core.registries.Registries.BIOME);
		for (var key : java.util.List.of(net.minecraft.world.level.biome.Biomes.FLOWER_FOREST,
				net.minecraft.world.level.biome.Biomes.MEADOW)) {
			test.assertTrue(biomes.getOrThrow(key).value().getGenerationSettings().hasFeature(feature),
					"Paeonia patch is missing from " + key);
		}
		test.assertTrue(!biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value()
				.getGenerationSettings().hasFeature(feature), "Paeonia patch was added outside its selected biomes");
		test.setBlock(CENTER.below(), Blocks.GRASS_BLOCK);
		test.setBlock(CENTER, Blocks.AIR);
		test.assertTrue(
				feature.feature().value().place(test.getLevel(), test.getLevel().getChunkSource().getGenerator(),
						net.minecraft.util.RandomSource.create(42), test.absolutePos(CENTER)),
				"Paeonia feature did not generate a flower on valid ground");
		test.assertBlockPresent(GardenBlocks.PAEONIA, CENTER);
		test.succeed();
	}
}
