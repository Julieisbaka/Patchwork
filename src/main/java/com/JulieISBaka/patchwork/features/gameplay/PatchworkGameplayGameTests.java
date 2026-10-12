package com.JulieISBaka.patchwork.features.gameplay;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public final class PatchworkGameplayGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void patchworkAdvancementsAreLoaded(GameTestHelper test) {
		var advancements = test.getLevel().getServer().getAdvancements();
		for (String path : new String[] { "adventure/iron_man", "adventure/living_like_a_king",
				"adventure/update_aquatic", "adventure/wrecked", "adventure/restored_to_color",
				"adventure/creature_comforts", "adventure/a_soulful_companion", "adventure/let_there_be_light",
				"story/getting_wood", "story/benchmarking", "story/overpowered" }) {
			test.assertTrue(advancements.get(Patchwork.id(path)) != null,
					"Patchwork advancement was not loaded: " + path);
		}
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingPreservesMixedValuesCountsAndRadius(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		Vec3 center = Vec3.atCenterOf(test.absolutePos(CENTER));
		var first = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 7);
		((ExperienceOrbAccessor) first).patchwork$setCount(3);
		var second = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x + 1.5, center.y, center.z,
				11);
		var far = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z + 2.1, 5);
		for (var orb : java.util.List.of(first, second, far)) {
			orb.setNoGravity(true);
			orb.setDeltaMovement(Vec3.ZERO);
			test.getLevel().addFreshEntity(orb);
		}
		test.runAfterDelay(3, () -> {
			var orbs = test.getEntities(EntityTypes.EXPERIENCE_ORB);
			test.assertTrue(orbs.size() == 2, "Nearby orbs did not merge or distant orb merged");
			test.assertTrue(orbs.stream().anyMatch(orb -> orb.getValue() == 32),
					"Merge lost XP or ignored vanilla orb counts");
			test.assertTrue(!far.isRemoved() && far.getValue() == 5, "Orb outside two-block radius was merged");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingCollectsFullValueAndRepairsMending(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		tool.enchant(
				test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
						.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING),
				1);
		tool.setDamageValue(20);
		player.setItemInHand(InteractionHand.MAIN_HAND, tool);
		var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), 0, 0, 0, 15);
		((ExperienceOrbAccessor) orb).patchwork$setCount(2);
		test.getLevel().addFreshEntity(orb);
		int before = player.totalExperience;
		player.takeXpDelay = 0;
		orb.playerTouch(player);
		test.assertTrue(orb.isRemoved(), "Merged orb was not collected in a single pickup");
		test.assertTrue(tool.getDamageValue() == 0, "Merged orb did not apply Mending");
		test.assertTrue(player.totalExperience == before + 20, "Mending and pickup lost or duplicated merged XP");
		player.discard();
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingPickupSaturatesAtMaximumTotalExperience(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		player.totalExperience = Integer.MAX_VALUE - 5;
		player.takeXpDelay = 0;
		var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), 0, 0, 0, 100);
		test.getLevel().addFreshEntity(orb);
		orb.playerTouch(player);
		test.assertTrue(orb.isRemoved(), "XP orb was not collected");
		test.assertTrue(player.totalExperience == Integer.MAX_VALUE,
				"XP pickup overflowed the player's total experience");
		player.discard();
		test.succeed();
	}

	@GameTest
	public void experienceClumpingPreservesLargeValuesAcrossSaveReload(GameTestHelper test) {
		var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), 0, 0, 0, 100000);
		var problems = new net.minecraft.util.ProblemReporter.Collector();
		var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems,
				test.getLevel().registryAccess());
		test.assertTrue(orb.save(output), "XP orb could not be saved");
		var reloaded = new net.minecraft.world.entity.ExperienceOrb(EntityTypes.EXPERIENCE_ORB, test.getLevel());
		reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(problems, test.getLevel().registryAccess(),
				output.buildResult()));
		test.assertTrue(reloaded.getValue() == 100000, "Saved XP was truncated to a short");
		test.assertTrue(problems.isEmpty(), "XP save/reload reported serialization problems");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingAwardSpawnsOneOrbAndAvoidsOverflow(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		Vec3 center = Vec3.atCenterOf(test.absolutePos(CENTER));
		net.minecraft.world.entity.ExperienceOrb.award(test.getLevel(), center, Integer.MAX_VALUE);
		var orbs = test.getEntities(EntityTypes.EXPERIENCE_ORB);
		test.assertTrue(orbs.size() == 1 && orbs.getFirst().getValue() == Integer.MAX_VALUE,
				"Award split XP into multiple entities or changed its value");
		var other = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 10);
		test.getLevel().addFreshEntity(other);
		for (var orb : test.getEntities(EntityTypes.EXPERIENCE_ORB)) {
			orb.setNoGravity(true);
			orb.setDeltaMovement(Vec3.ZERO);
		}
		test.runAfterDelay(3, () -> {
			long total = test.getEntities(EntityTypes.EXPERIENCE_ORB).stream()
					.mapToLong(net.minecraft.world.entity.ExperienceOrb::getValue).sum();
			test.assertTrue(total == (long) Integer.MAX_VALUE + 10, "Merge overflow lost XP");
			test.succeed();
		});
	}

	@GameTest
	public void experienceClumpingSettingControlsVanillaBehavior(GameTestHelper test) {
		boolean clumping = PatchworkConfig.settings().experienceClumping();
		Vec3 center = Vec3.atCenterOf(test.absolutePos(CENTER));
		net.minecraft.world.entity.ExperienceOrb.award(test.getLevel(), center, 30);
		var awarded = test.getEntities(EntityTypes.EXPERIENCE_ORB);
		test.assertTrue(clumping ? awarded.size() == 1 : awarded.size() > 1,
				"XP award did not respect the clumping setting");
		long total = awarded.stream().mapToLong(orb -> (long) orb.getValue()
				* ((ExperienceOrbAccessor) orb).patchwork$count()).sum();
		test.assertTrue(total == 30, "Vanilla XP award lost value");
		awarded.forEach(net.minecraft.world.entity.ExperienceOrb::discard);

		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		var stacked = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 15);
		var data = (ExperienceOrbAccessor) stacked;
		data.patchwork$setCount(2);
		test.getLevel().addFreshEntity(stacked);
		int before = player.totalExperience;
		player.takeXpDelay = 0;
		stacked.playerTouch(player);
		test.assertTrue(clumping ? stacked.isRemoved() : !stacked.isRemoved() && data.patchwork$count() == 1,
				"Counted pickup did not respect the clumping setting");
		test.assertTrue(player.totalExperience == before + (clumping ? 30 : 15), "Counted pickup granted the wrong XP");
		stacked.discard();
		player.discard();

		for (int value : new int[] { 7, 11 }) {
			var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z,
					value);
			orb.setNoGravity(true);
			orb.setDeltaMovement(Vec3.ZERO);
			test.getLevel().addFreshEntity(orb);
		}
		test.runAfterDelay(3, () -> {
			var orbs = test.getEntities(EntityTypes.EXPERIENCE_ORB);
			test.assertTrue(
					orbs.size() == (clumping ? 1 : 2)
							&& orbs.stream().mapToInt(net.minecraft.world.entity.ExperienceOrb::getValue).sum() == 18,
					"Different-value merging did not respect the clumping setting");
			test.succeed();
		});
	}

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}
}
