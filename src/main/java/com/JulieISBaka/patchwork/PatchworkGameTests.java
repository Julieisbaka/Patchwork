package com.JulieISBaka.patchwork;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.cauldron.CauldronInteractions;
import com.JulieISBaka.patchwork.mixin.PotionArrowItemAccessor;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

public class PatchworkGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}

	@GameTest
	public void lightingCarvedPumpkins(GameTestHelper test) {
		require(test, PatchworkConfig.settings().pumpkinLanterns(), "pumpkinLanterns");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (var torch : new net.minecraft.world.item.Item[] {Items.TORCH, Items.SOUL_TORCH}) {
			test.setBlock(CENTER, Blocks.CARVED_PUMPKIN.defaultBlockState()
				.setValue(CarvedPumpkinBlock.FACING, Direction.WEST));
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(torch, 2));
			BlockPos pos = test.absolutePos(CENTER);
			UseBlockCallback.EVENT.invoker().interact(player, test.getLevel(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
			test.assertBlockPresent(torch == Items.TORCH ? Blocks.JACK_O_LANTERN : PumpkinLanterns.SOUL_BLOCK, CENTER);
			test.assertTrue(test.getBlockState(CENTER).getValue(CarvedPumpkinBlock.FACING) == Direction.WEST,
				"Lighting changed the carved pumpkin facing");
			test.assertTrue(player.getMainHandItem().getCount() == 1, "Lighting did not consume exactly one torch");
		}
		test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
		test.assertItemEntityPresent(PumpkinLanterns.SOUL_ITEM, CENTER, 2);
		test.succeed();
	}

	@GameTest
	public void soulJackOLanternCannotCreateSnowGolem(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, Blocks.SNOW_BLOCK);
		test.setBlock(CENTER.above(), Blocks.SNOW_BLOCK);
		test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
		test.assertTrue(test.getEntities(EntityTypes.SNOW_GOLEM).isEmpty(), "Soul lantern created a Snow Golem");
		test.assertBlockPresent(Blocks.SNOW_BLOCK, CENTER);
		test.assertBlockPresent(Blocks.SNOW_BLOCK, CENTER.above());
		test.assertBlockPresent(PumpkinLanterns.SOUL_BLOCK, CENTER.above(2));
		for (var pumpkin : new net.minecraft.world.level.block.Block[] {Blocks.CARVED_PUMPKIN, Blocks.JACK_O_LANTERN}) {
			test.setBlock(CENTER.above(2), Blocks.AIR);
			test.setBlock(CENTER, Blocks.SNOW_BLOCK);
			test.setBlock(CENTER.above(), Blocks.SNOW_BLOCK);
			test.setBlock(CENTER.above(2), pumpkin);
			test.assertTrue(test.getEntities(EntityTypes.SNOW_GOLEM).size() == 1,
				"Vanilla pumpkin no longer creates a Snow Golem");
			test.getEntities(EntityTypes.SNOW_GOLEM).getFirst().discard();
		}
		test.succeed();
	}

	@GameTest
	public void soulGolemRepairsWithGoldOnly(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		golem.setNoAi(true);
		golem.setHealth(10.0F);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));
		golem.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 10.0F && player.getMainHandItem().getCount() == 2,
			"Iron repaired the Soul Golem or was consumed");
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GOLD_INGOT, 3));
		golem.interact(player, InteractionHand.OFF_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 35.0F && player.getOffhandItem().getCount() == 2,
			"Gold did not heal 25 HP and consume exactly one ingot");
		golem.interact(player, InteractionHand.OFF_HAND, Vec3.ZERO);
		golem.interact(player, InteractionHand.OFF_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 50.0F && player.getOffhandItem().getCount() == 1,
			"Repairing full health consumed an ingot or exceeded max health");
		Player creative = test.makeMockPlayer(GameType.CREATIVE);
		creative.getAbilities().instabuild = true;
		creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
		golem.setHealth(25.0F);
		golem.interact(creative, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 50.0F && creative.getMainHandItem().getCount() == 1,
			"Creative gold repair consumed the ingot");
		var iron = test.spawn(EntityTypes.IRON_GOLEM, CENTER.offset(2, 0, 0));
		iron.setHealth(50.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 2));
		iron.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(iron.getHealth() == 75.0F && player.getMainHandItem().getCount() == 1,
			"Vanilla iron golem repairs changed");
		test.assertTrue(golem.getHurtSound(golem.damageSources().generic()) != net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT
			&& golem.getDeathSound() != net.minecraft.sounds.SoundEvents.IRON_GOLEM_DEATH,
			"Soul Golem still uses iron golem hurt/death sounds");
		test.succeed();
	}

	@GameTest
	public void soulGolemSpawnEggCreatesFriendlyDefender(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulGolems.SPAWN_EGG, 2));
		test.assertTrue(SpawnEggItem.getType(player.getMainHandItem()) == SoulGolems.TYPE,
			"Spawn egg does not reference the Soul Golem");
		BlockPos floor = test.absolutePos(CENTER.below());
		SoulGolems.SPAWN_EGG.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
		test.assertTrue(test.getEntities(SoulGolems.TYPE).size() == 1, "Spawn egg did not spawn one Soul Golem");
		var golem = test.getEntities(SoulGolems.TYPE).getFirst();
		test.assertTrue(golem.getMaxHealth() == 50.0F && !golem.canAttack(player)
			&& golem.isPlayerCreated() && golem.isPersistenceRequired(),
			"Egg-created Soul Golem did not keep balanced, friendly behavior");
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Spawn egg did not consume one egg");
		test.succeed();
	}

	@GameTest
	public void soulGolemAcceptsSoulSandSoilAndMixedStacks(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		for (var bottom : new net.minecraft.world.level.block.Block[] {Blocks.SOUL_SAND, Blocks.SOUL_SOIL}) {
			for (var middle : new net.minecraft.world.level.block.Block[] {Blocks.SOUL_SAND, Blocks.SOUL_SOIL}) {
				test.setBlock(CENTER, bottom);
				test.setBlock(CENTER.above(), middle);
				test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
				var golems = test.getEntities(SoulGolems.TYPE);
				test.assertTrue(golems.size() == 1, "Soul stack did not create exactly one Soul Golem");
				SoulGolem golem = golems.getFirst();
				test.assertTrue(golem.isPlayerCreated() && golem.isPersistenceRequired(),
					"Soul Golem is not a persistent player-built defender");
				test.assertTrue(golem.getMaxHealth() == 50.0F,
					"Soul Golem does not have half of an iron golem's health");
				test.assertBlockPresent(Blocks.AIR, CENTER);
				test.assertBlockPresent(Blocks.AIR, CENTER.above());
				test.assertBlockPresent(Blocks.AIR, CENTER.above(2));
				test.assertBlockPresent(Blocks.STONE, CENTER.below());
				test.assertTrue(!SoulGolems.trySpawn(test.getLevel(), test.absolutePos(CENTER.above(2))),
					"Consumed pattern spawned another Soul Golem");
				golem.discard();
			}
		}
		test.succeed();
	}

	@GameTest
	public void soulGolemRejectsIncompleteOrWrongStacks(GameTestHelper test) {
		test.setBlock(CENTER, Blocks.STONE);
		test.setBlock(CENTER.above(), Blocks.SOUL_SAND);
		test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
		test.assertTrue(test.getEntities(SoulGolems.TYPE).isEmpty(), "Wrong base created a Soul Golem");
		test.assertBlockPresent(Blocks.STONE, CENTER);
		test.assertBlockPresent(Blocks.SOUL_SAND, CENTER.above());
		test.assertBlockPresent(PumpkinLanterns.SOUL_BLOCK, CENTER.above(2));
		test.setBlock(CENTER.above(2), Blocks.AIR);
		test.setBlock(CENTER, Blocks.AIR);
		test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
		test.assertTrue(test.getEntities(SoulGolems.TYPE).isEmpty(), "One soul block created a Soul Golem");
		test.setBlock(CENTER.above(2), Blocks.AIR);
		test.setBlock(CENTER, Blocks.SOUL_SOIL);
		test.setBlock(CENTER.above(2), Blocks.JACK_O_LANTERN);
		test.assertTrue(test.getEntities(SoulGolems.TYPE).isEmpty(), "Ordinary lantern created a Soul Golem");
		test.assertBlockPresent(Blocks.SOUL_SOIL, CENTER);
		test.assertBlockPresent(Blocks.SOUL_SAND, CENTER.above());
		test.assertBlockPresent(Blocks.JACK_O_LANTERN, CENTER.above(2));
		test.succeed();
	}

	@GameTest
	public void lightingSoulGolemHeadConsumesPatternAndTorch(GameTestHelper test) {
		require(test, PatchworkConfig.settings().pumpkinLanterns(), "pumpkinLanterns");
		test.setBlock(CENTER, Blocks.SOUL_SAND);
		test.setBlock(CENTER.above(), Blocks.SOUL_SOIL);
		test.setBlock(CENTER.above(2), Blocks.CARVED_PUMPKIN);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SOUL_TORCH, 2));
		BlockPos head = test.absolutePos(CENTER.above(2));
		UseBlockCallback.EVENT.invoker().interact(player, test.getLevel(), InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(head), Direction.NORTH, head, false));
		test.assertTrue(test.getEntities(SoulGolems.TYPE).size() == 1, "Lighting did not create a Soul Golem");
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Lighting did not consume one soul torch");
		test.assertBlockPresent(Blocks.AIR, CENTER);
		test.assertBlockPresent(Blocks.AIR, CENTER.above());
		test.assertBlockPresent(Blocks.AIR, CENTER.above(2));
		test.succeed();
	}

	@GameTest
	public void soulGolemIsFriendlyAndDealsMeleeDamage(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var creeper = test.spawn(EntityTypes.CREEPER, CENTER.offset(2, 0, 0));
		creeper.setNoAi(true);
		test.assertTrue(!golem.canAttack(player), "Player-created Soul Golem can attack a player");
		test.assertTrue(!golem.canAttack(creeper), "Soul Golem can target a Creeper");
		var zombie = test.spawn(EntityTypes.ZOMBIE, CENTER.offset(0, 0, 2));
		zombie.setNoAi(true);
		test.assertTrue(golem.canAttack(zombie), "Soul Golem cannot attack a hostile Zombie");
		float health = zombie.getHealth();
		test.assertTrue(golem.doHurtTarget(test.getLevel(), zombie) && zombie.getHealth() < health,
			"Soul Golem's melee attack did not damage its target");
		test.assertTrue(zombie.getRemainingFireTicks() == 40, "Soul Golem did not ignite its target for two seconds");
		test.succeed();
	}

	@GameTest
	public void soulGolemDamageIsExactlyOneQuarter(GameTestHelper test) {
		var iron = test.spawn(EntityTypes.IRON_GOLEM, CENTER);
		var soul = test.spawn(SoulGolems.TYPE, CENTER);
		for (int seed = 0; seed < 20; seed++) {
			var ironTarget = test.spawn(EntityTypes.COW, CENTER);
			var soulTarget = test.spawn(EntityTypes.COW, CENTER);
			ironTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
			soulTarget.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
			ironTarget.setHealth(1000);
			soulTarget.setHealth(1000);
			ironTarget.setInvulnerableTime(0);
			soulTarget.setInvulnerableTime(0);
			iron.getRandom().setSeed(seed);
			soul.getRandom().setSeed(seed);
			test.assertTrue(iron.doHurtTarget(test.getLevel(), ironTarget), "Iron golem attack failed");
			test.assertTrue(soul.doHurtTarget(test.getLevel(), soulTarget), "Soul golem attack failed");
			float ironDamage = 1000 - ironTarget.getHealth();
			float soulDamage = 1000 - soulTarget.getHealth();
			test.assertTrue(Math.abs(soulDamage - ironDamage * 0.25F) < 0.0001F,
				"Soul Golem damage is not one quarter of the matching iron golem roll");
			ironTarget.discard();
			soulTarget.discard();
		}
		var soulTarget = test.spawn(EntityTypes.COW, CENTER);
		soulTarget.setRemainingFireTicks(0);
		soulTarget.setPermanentlyInvulnerable(true);
		test.assertTrue(!soul.doHurtTarget(test.getLevel(), soulTarget), "Invulnerable target was damaged");
		test.assertTrue(soulTarget.getRemainingFireTicks() == 0, "Failed attack ignited its target");
		test.succeed();
	}

	@GameTest
	public void bannerLoomAllowsNinePatternsButNotTen(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var menu = new net.minecraft.world.inventory.LoomMenu(1, player.getInventory());
		var pattern = test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
			.getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CROSS);
		for (int layers = 6; layers <= 9; layers++) {
			ItemStack banner = new ItemStack(Items.BANNER.white());
			banner.set(DataComponents.BANNER_PATTERNS, new net.minecraft.world.level.block.entity.BannerPatternLayers(
				java.util.Collections.nCopies(layers,
					new net.minecraft.world.level.block.entity.BannerPatternLayers.Layer(pattern, DyeColor.RED))));
			menu.getBannerSlot().set(banner);
			menu.getDyeSlot().set(new ItemStack(Items.DYE.red()));
			menu.clickMenuButton(player, 0);
			ItemStack result = menu.getResultSlot().getItem();
			if (layers < 9) {
				test.assertTrue(!result.isEmpty()
					&& result.get(DataComponents.BANNER_PATTERNS).layers().size() == layers + 1,
					"Loom did not add pattern " + (layers + 1));
			} else {
				test.assertTrue(result.isEmpty(), "Loom allowed a tenth banner pattern");
				var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
					java.util.List.of(banner, new ItemStack(Items.BANNER.white())));
				var recipe = test.getLevel().recipeAccess()
					.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
				test.assertTrue(recipe.isPresent(), "Nine-layer banner could not be copied");
				var copy = recipe.orElseThrow().value().assemble(input);
				test.assertTrue(copy.get(DataComponents.BANNER_PATTERNS).equals(banner.get(DataComponents.BANNER_PATTERNS)),
					"Copied banner lost its nine layers");
			}
		}
		test.succeed();
	}

	@GameTest
	public void unlitLanternVariantsRelightAndDrop(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var variants = new java.util.ArrayList<net.minecraft.world.level.block.Block>();
		variants.add(Blocks.LANTERN);
		variants.add(Blocks.SOUL_LANTERN);
		variants.addAll(Blocks.COPPER_LANTERN.asList());
		test.assertTrue(variants.size() == 10, "Expected ten lantern variants");
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER.above(), Blocks.STONE);
		for (var lit : variants) {
			for (boolean hanging : new boolean[] {false, true}) {
				var state = lit.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, hanging);
				var unlit = UnlitLanterns.extinguish(state);
				test.assertTrue(unlit != null && unlit.getLightEmission() == 0
					&& unlit.getValue(net.minecraft.world.level.block.LanternBlock.HANGING) == hanging,
					"Lantern did not extinguish while preserving hanging state");
				test.setBlock(CENTER, unlit);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE, 2));
				test.useBlock(CENTER, player);
				test.assertTrue(test.getBlockState(CENTER).equals(state), "Relighting did not restore the exact lantern variant");
				test.assertTrue(player.getMainHandItem().getCount() == 1, "Relighting did not consume one fire charge");
			}
			var unlit = UnlitLanterns.extinguish(lit.defaultBlockState());
			test.setBlock(CENTER, unlit);
			test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
			test.assertItemEntityPresent(unlit.getBlock().asItem(), CENTER, 2);
		}
		test.succeed();
	}

	@GameTest
	public void unlitLanternWaterloggingAndCopperMappings(GameTestHelper test) {
		var wet = Blocks.LANTERN.defaultBlockState()
			.setValue(net.minecraft.world.level.block.LanternBlock.WATERLOGGED, true);
		var unlit = UnlitLanterns.extinguish(wet);
		test.assertTrue(unlit.getValue(net.minecraft.world.level.block.LanternBlock.WATERLOGGED),
			"Extinguishing lost waterlogging");
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, unlit);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE, 2));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).equals(unlit) && player.getMainHandItem().getCount() == 2,
			"Waterlogged lantern was relit or consumed a fire charge");
		var copper = UnlitLanterns.COPPER_LANTERN;
		test.assertTrue(net.minecraft.world.level.block.WeatheringCopper.getNext(copper.weathering().unaffected())
			.orElseThrow() == copper.weathering().exposed(), "Unlit copper cannot oxidize");
		test.assertTrue(net.minecraft.world.level.block.WeatheringCopper.getPrevious(copper.weathering().weathered())
			.orElseThrow() == copper.weathering().exposed(), "Unlit copper cannot be scraped");
		copper.zipUnwaxedWaxed((normal, waxed) -> {
			test.assertTrue(net.minecraft.world.item.HoneycombItem.getWaxed(normal.defaultBlockState())
				.orElseThrow().is(waxed), "Unlit copper cannot be waxed");
			test.assertTrue(net.minecraft.world.item.HoneycombItem.WAX_OFF_BY_BLOCK.get().get(waxed) == normal,
				"Unlit copper cannot be unwaxed");
			test.assertTrue(!waxed.defaultBlockState().isRandomlyTicking(), "Waxed unlit lantern can oxidize");
		});
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingPreservesMixedValuesCountsAndRadius(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		Vec3 center = Vec3.atCenterOf(test.absolutePos(CENTER));
		var first = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 7);
		((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor)first).patchwork$setCount(3);
		var second = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x + 1.5, center.y, center.z, 11);
		var far = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z + 2.1, 5);
		for (var orb : java.util.List.of(first, second, far)) {
			orb.setNoGravity(true);
			orb.setDeltaMovement(Vec3.ZERO);
			test.getLevel().addFreshEntity(orb);
		}
		test.runAfterDelay(3, () -> {
			var orbs = test.getEntities(EntityTypes.EXPERIENCE_ORB);
			test.assertTrue(orbs.size() == 2, "Nearby orbs did not merge or distant orb merged");
			test.assertTrue(orbs.stream().anyMatch(orb -> orb.getValue() == 32), "Merge lost XP or ignored vanilla orb counts");
			test.assertTrue(!far.isRemoved() && far.getValue() == 5, "Orb outside two-block radius was merged");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void experienceClumpingCollectsFullValueAndRepairsMending(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		tool.enchant(test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
			.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING), 1);
		tool.setDamageValue(20);
		player.setItemInHand(InteractionHand.MAIN_HAND, tool);
		var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), 0, 0, 0, 15);
		((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor)orb).patchwork$setCount(2);
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

	@GameTest
	public void experienceClumpingPreservesLargeValuesAcrossSaveReload(GameTestHelper test) {
		var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), 0, 0, 0, 100000);
		var problems = new net.minecraft.util.ProblemReporter.Collector();
		var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems, test.getLevel().registryAccess());
		test.assertTrue(orb.save(output), "XP orb could not be saved");
		var reloaded = new net.minecraft.world.entity.ExperienceOrb(EntityTypes.EXPERIENCE_ORB, test.getLevel());
		reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(problems,
			test.getLevel().registryAccess(), output.buildResult()));
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
			test.assertTrue(total == (long)Integer.MAX_VALUE + 10, "Merge overflow lost XP");
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
		long total = awarded.stream().mapToLong(orb -> (long)orb.getValue()
			* ((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor)orb).patchwork$count()).sum();
		test.assertTrue(total == 30, "Vanilla XP award lost value");
		awarded.forEach(net.minecraft.world.entity.ExperienceOrb::discard);

		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		var stacked = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 15);
		var data = (com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor)stacked;
		data.patchwork$setCount(2);
		test.getLevel().addFreshEntity(stacked);
		int before = player.totalExperience;
		player.takeXpDelay = 0;
		stacked.playerTouch(player);
		test.assertTrue(clumping ? stacked.isRemoved() : !stacked.isRemoved() && data.patchwork$count() == 1,
			"Counted pickup did not respect the clumping setting");
		test.assertTrue(player.totalExperience == before + (clumping ? 30 : 15),
			"Counted pickup granted the wrong XP");
		stacked.discard();
		player.discard();

		for (int value : new int[] {7, 11}) {
			var orb = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, value);
			orb.setNoGravity(true);
			orb.setDeltaMovement(Vec3.ZERO);
			test.getLevel().addFreshEntity(orb);
		}
		test.runAfterDelay(3, () -> {
			var orbs = test.getEntities(EntityTypes.EXPERIENCE_ORB);
			test.assertTrue(orbs.size() == (clumping ? 1 : 2)
				&& orbs.stream().mapToInt(net.minecraft.world.entity.ExperienceOrb::getValue).sum() == 18,
				"Different-value merging did not respect the clumping setting");
			test.succeed();
		});
	}

	@GameTest
	public void unlitTorchVariants(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, Blocks.TORCH);
		test.assertBlockState(CENTER, state -> UnlitTorches.extinguish(state).is(UnlitTorches.TORCH),
			state -> net.minecraft.network.chat.Component.literal("Ordinary torch did not extinguish"));
		test.assertTrue(UnlitTorches.extinguish(Blocks.SOUL_TORCH.defaultBlockState()).is(UnlitTorches.SOUL_TORCH),
			"Soul torch did not extinguish");
		test.assertTrue(UnlitTorches.extinguish(Blocks.COPPER_TORCH.defaultBlockState()).is(UnlitTorches.COPPER_TORCH),
			"Copper torch did not extinguish");
		test.assertTrue(UnlitTorches.extinguish(Blocks.REDSTONE_TORCH.defaultBlockState()).is(UnlitTorches.REDSTONE_TORCH),
			"Lit redstone torch did not extinguish");
		test.assertTrue(UnlitTorches.extinguish(Blocks.REDSTONE_TORCH.defaultBlockState()
			.setValue(RedstoneTorchBlock.LIT, false)) == null, "Already-off redstone torch was changed");
		test.assertTrue(UnlitTorches.extinguish(Blocks.WALL_TORCH.defaultBlockState()
			.setValue(WallTorchBlock.FACING, Direction.EAST)).getValue(WallTorchBlock.FACING) == Direction.EAST,
			"Wall facing was lost");
		test.succeed();
	}

	@GameTest
	public void unlitTorchesDropMatchingItems(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, UnlitTorches.COPPER_TORCH);
		test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
		test.assertItemEntityPresent(UnlitTorches.COPPER_TORCH_ITEM, CENTER, 2);
		test.succeed();
	}

	@GameTest
	public void unlitTorchRelightsWithFireCharge(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, UnlitTorches.TORCH);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE));
		test.useBlock(CENTER, player);
		test.assertBlockPresent(Blocks.TORCH, CENTER);
		test.assertTrue(player.getMainHandItem().isEmpty(), "Relighting did not consume one fire charge");
		test.succeed();
	}

	@GameTest
	public void washingUsesOneWaterLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().cauldronCleaning(), "cauldronCleaning");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var dyed = new net.minecraft.world.item.Item[] {
			Items.WOOL.red(), Items.DYED_TERRACOTTA.blue(), Items.STAINED_GLASS.green()
		};
		var clean = new net.minecraft.world.item.Item[] {
			Items.WOOL.white(), Items.TERRACOTTA, Items.GLASS
		};
		for (int i = 0; i < dyed.length; i++) {
			test.setBlock(CENTER, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
			ItemStack stack = new ItemStack(dyed[i]);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			CauldronInteractions.WATER.get(stack).interact(test.getBlockState(CENTER), test.getLevel(),
				test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, stack);
			test.assertTrue(player.getMainHandItem().is(clean[i]), "Dyed " + dyed[i] + " was not washed");
			test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
				"Washing " + dyed[i] + " did not use exactly one water level");
		}
		test.succeed();
	}

	@GameTest
	public void potionPourRefillAndDipOffhand(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, Blocks.CAULDRON);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		ItemStack potion = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
		PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
		player.setItemInHand(InteractionHand.MAIN_HAND, potion);
		CauldronInteractions.EMPTY.get(potion).interact(test.getBlockState(CENTER),
			test.getLevel(), test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, potion);
		test.assertBlockPresent(PotionCauldrons.BLOCK, CENTER);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
			"Potion did not fill the empty cauldron with one level");
		test.assertTrue(((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER)))
			.potion().equals(contents), "Potion contents were not retained");
		test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Pouring did not return a glass bottle");
		player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
			"Matching potion did not refill exactly one level");
		player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.HEALING));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2
			&& ((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER)))
				.potion().equals(contents),
			"Different potion contents were mixed");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW));
		test.getBlockState(CENTER).useItemOn(player.getOffhandItem(), test.getLevel(), player,
			InteractionHand.OFF_HAND, new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)),
				Direction.NORTH, test.absolutePos(CENTER), false));
		test.assertTrue(player.getOffhandItem().is(Items.TIPPED_ARROW)
			&& player.getOffhandItem().getCount() == 1
			&& player.getOffhandItem().get(DataComponents.POTION_CONTENTS).equals(
				((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER))).potion()),
			"Offhand dipping did not return one matching tipped arrow");
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
			"Offhand dipping did not use one potion level");
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ARROW, 8));
		test.useBlock(CENTER, player);
		test.assertTrue(player.getMainHandItem().is(Items.ARROW) && player.getMainHandItem().getCount() == 7,
			"Dipping a held stack did not consume exactly one arrow");
		test.assertTrue(player.getInventory().countItem(Items.TIPPED_ARROW) == 1,
			"Dipping a held stack did not put exactly one tipped arrow in inventory");
		test.assertBlockPresent(Blocks.CAULDRON, CENTER);
		test.succeed();
	}

	@GameTest
	public void potionRejectsWaterCauldronsAndPreservesWaterBottles(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (int fill = 1; fill <= 3; fill++) {
			test.setBlock(CENTER, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, fill));
			ItemStack potion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
			player.setItemInHand(InteractionHand.MAIN_HAND, potion);
			CauldronInteractions.WATER.get(potion).interact(test.getBlockState(CENTER), test.getLevel(),
				test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, potion);
			test.assertBlockPresent(Blocks.WATER_CAULDRON, CENTER);
			test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == fill
				&& player.getMainHandItem().is(Items.POTION), "Potion replaced water or was consumed");
		}
		test.setBlock(CENTER, Blocks.CAULDRON);
		ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		player.setItemInHand(InteractionHand.MAIN_HAND, water);
		CauldronInteractions.EMPTY.get(water).interact(test.getBlockState(CENTER), test.getLevel(),
			test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, water);
		test.assertBlockPresent(Blocks.WATER_CAULDRON, CENTER);
		test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Vanilla water pouring changed");
		test.useBlock(CENTER, player);
		test.assertBlockPresent(Blocks.CAULDRON, CENTER);
		test.assertTrue(player.getMainHandItem().get(DataComponents.POTION_CONTENTS).is(Potions.WATER),
			"Vanilla water retrieval changed");
		test.succeed();
	}

	@GameTest
	public void potionBottlesRetrieveExactContentsAndOneLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, PotionCauldrons.BLOCK.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
		PotionContents contents = PotionContents.createItemStack(Items.POTION, Potions.LONG_SWIFTNESS)
			.get(DataComponents.POTION_CONTENTS);
		((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER))).setPotion(contents);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, 2));
		test.useBlock(CENTER, player);
		test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE) && player.getMainHandItem().getCount() == 1,
			"Extraction did not consume one bottle from the stack");
		test.assertTrue(player.getInventory().countItem(Items.POTION) == 1,
			"Extraction did not put one potion in inventory");
		for (ItemStack item : player.getInventory().getNonEquipmentItems()) {
			if (item.is(Items.POTION)) {
				test.assertTrue(contents.equals(item.get(DataComponents.POTION_CONTENTS)),
					"Bottled potion lost its exact contents");
			}
		}
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
			"Bottling used more than one potion level");
		test.useBlock(CENTER, player);
		test.assertTrue(player.getMainHandItem().is(Items.POTION)
			&& contents.equals(player.getMainHandItem().get(DataComponents.POTION_CONTENTS)),
			"Single bottle was not replaced with the correct potion");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
		test.getBlockState(CENTER).useItemOn(player.getOffhandItem(), test.getLevel(), player,
			InteractionHand.OFF_HAND, new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)),
				Direction.NORTH, test.absolutePos(CENTER), false));
		test.assertBlockPresent(Blocks.CAULDRON, CENTER);
		test.assertTrue(player.getOffhandItem().is(Items.POTION)
			&& contents.equals(player.getOffhandItem().get(DataComponents.POTION_CONTENTS)),
			"Offhand extraction lost the last potion level");
		test.succeed();
	}

	@GameTest(maxTicks = 10)
	public void charcoalBlockBurnsFor16000Ticks(GameTestHelper test) {
		var coalFuel = Items.COAL_BLOCK.getDefaultInstance().get(DataComponents.COOKING_FUEL);
		var charcoalFuel = CharcoalBlocks.ITEM.getDefaultInstance().get(DataComponents.COOKING_FUEL);
		test.assertTrue(coalFuel != null && coalFuel.equals(charcoalFuel),
			"Charcoal block does not have the coal block's furnace fuel value");
		test.setBlock(CENTER, Blocks.FURNACE);
		var furnace = (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity)
			test.getLevel().getBlockEntity(test.absolutePos(CENTER));
		furnace.setItem(0, new ItemStack(Items.COBBLESTONE));
		furnace.setItem(1, new ItemStack(CharcoalBlocks.ITEM));
		test.runAfterDelay(2, () -> {
			int duration = furnace.saveWithoutMetadata(test.getLevel().registryAccess())
				.getInt("lit_total_time").orElse(0);
			test.assertTrue(duration == 16000, "Expected 16000 charcoal fuel ticks, got " + duration);
			test.assertTrue(furnace.getItem(1).isEmpty(), "Furnace did not consume the charcoal block");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void potionDroppedSingleArrowUsesOnePotionLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, PotionCauldrons.BLOCK.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
		((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER)))
			.setPotion(PotionContents.createItemStack(Items.POTION, Potions.HEALING)
				.get(DataComponents.POTION_CONTENTS));
		Vec3 center = Vec3.atBottomCenterOf(test.absolutePos(CENTER)).add(0, 0.5, 0);
		ItemEntity arrows = new ItemEntity(test.getLevel(), center.x, center.y, center.z,
			new ItemStack(Items.ARROW));
		arrows.setDeltaMovement(Vec3.ZERO);
		arrows.setPickUpDelay(40);
		test.getLevel().addFreshEntity(arrows);
		test.runAfterDelay(5, () -> {
			test.assertTrue(!arrows.isRemoved() && arrows.getItem().is(Items.TIPPED_ARROW),
				"Dropped single arrow was respawned instead of converted in place");
			test.assertTrue(arrows.getItem().getCount() == 1 && arrows.hasPickUpDelay(),
				"Dipping changed the arrow count or reset its pickup delay");
			test.assertTrue(test.getEntities(EntityTypes.ITEM).size() == 1,
				"Dipping a single arrow spawned another item entity");
			test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
				"Dropped arrows did not use one potion level");
			test.assertItemEntityPresent(Items.TIPPED_ARROW, CENTER, 2);
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void potionDroppedArrowStackProducesAtMostThreeArrows(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, PotionCauldrons.BLOCK.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
		var contents = PotionContents.createItemStack(Items.POTION, Potions.HEALING)
			.get(DataComponents.POTION_CONTENTS);
		((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER))).setPotion(contents);
		Vec3 center = Vec3.atBottomCenterOf(test.absolutePos(CENTER)).add(0, 0.5, 0);
		ItemEntity arrows = new ItemEntity(test.getLevel(), center.x, center.y, center.z,
			new ItemStack(Items.ARROW, 8));
		arrows.setDeltaMovement(Vec3.ZERO);
		arrows.setPickUpDelay(40);
		arrows.setTarget(playerUuidForArrowTest());
		((PotionArrowItemAccessor)arrows).patchwork$setAge(1000);
		test.getLevel().addFreshEntity(arrows);
		test.runAfterDelay(5, () -> {
			test.assertTrue(!arrows.isRemoved() && arrows.getItem().is(Items.TIPPED_ARROW)
				&& arrows.getItem().getCount() == 3, "Full cauldron did not convert the original entity into three arrows");
			test.assertBlockPresent(Blocks.CAULDRON, CENTER);
			int tippedCount = 0;
			int plainCount = 0;
			for (ItemEntity item : test.getEntities(EntityTypes.ITEM)) {
				test.assertTrue(item.getAge() >= 1000 && item.hasPickUpDelay(),
					"Splitting arrows lost their age or pickup delay");
				test.assertTrue(playerUuidForArrowTest().equals(((PotionArrowItemAccessor)item).patchwork$getTarget()),
					"Splitting arrows lost their pickup owner");
				if (item.getItem().is(Items.TIPPED_ARROW)) {
					test.assertTrue(contents.equals(item.getItem().get(DataComponents.POTION_CONTENTS)),
						"Dropped tipped arrow lost its potion contents");
					tippedCount += item.getItem().getCount();
				} else if (item.getItem().is(Items.ARROW)) {
					plainCount += item.getItem().getCount();
				}
			}
			test.assertTrue(tippedCount == 3 && plainCount == 5,
				"Full cauldron did not conserve eight arrows with exactly three tipped");
			test.succeed();
		});
	}

	private static java.util.UUID playerUuidForArrowTest() {
		return java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
	}

	@GameTest
	public void dyedShulkerChangesColor(GameTestHelper test) {
		require(test, PatchworkConfig.settings().shulkerDyeing(), "shulkerDyeing");
		Shulker shulker = test.spawn(EntityTypes.SHULKER, CENTER);
		Player player = test.makeMockPlayer(GameType.CREATIVE);
		ItemStack dye = new ItemStack(Items.DYE.red());
		Items.DYE.red().interactLivingEntity(dye, player, shulker, InteractionHand.MAIN_HAND);
		test.assertTrue(shulker.getColor() == DyeColor.RED, "Dye did not recolor the shulker");
		test.succeed();
	}

	@GameTest
	public void witherHealthMatchesDifficulty(GameTestHelper test) {
		require(test, PatchworkConfig.settings().witherDifficultyHealth(), "witherDifficultyHealth");
		WitherBoss wither = test.spawn(EntityTypes.WITHER, CENTER);
		double expected = switch (test.getLevel().getDifficulty()) {
			case EASY, PEACEFUL -> 300.0;
			case NORMAL -> 450.0;
			case HARD -> 600.0;
		};
		test.assertTrue(wither.getMaxHealth() == expected, "Wither maximum health does not match world difficulty");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void breezeExtinguishesLights(GameTestHelper test) {
		require(test, PatchworkConfig.settings().breezeShockwave(), "breezeShockwave");
		require(test, PatchworkConfig.settings().breezeTorchExtinguishing(), "breezeTorchExtinguishing");
		test.assertTrue(test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING),
			"Enable mobGriefing before running the Breeze extinguishing test");
		test.setBlock(CENTER.below(), Blocks.STONE);
		BlockPos torch = CENTER.offset(1, 0, 0);
		BlockPos campfire = CENTER.offset(0, 0, 2);
		BlockPos lantern = CENTER.offset(-1, 0, 0);
		test.setBlock(torch.below(), Blocks.STONE);
		test.setBlock(campfire.below(), Blocks.STONE);
		test.setBlock(lantern.below(), Blocks.STONE);
		test.setBlock(torch, Blocks.TORCH);
		test.setBlock(campfire, Blocks.CAMPFIRE);
		test.setBlock(lantern, Blocks.COPPER_LANTERN.waxed().weathered());
		Breeze breeze = test.spawn(EntityTypes.BREEZE, CENTER);
		Player attacker = test.makeMockPlayer(GameType.SURVIVAL);
		breeze.hurtServer(test.getLevel(), breeze.damageSources().playerAttack(attacker), 1.0F);
		test.assertBlockPresent(UnlitTorches.TORCH, torch);
		test.assertBlockPresent(UnlitLanterns.COPPER_LANTERN.waxed().weathered(), lantern);
		test.assertTrue(!test.getBlockState(campfire).getValue(CampfireBlock.LIT), "Campfire stayed lit");
		test.succeed();
	}

	@GameTest
	public void chargingHoglinLaunchesVictim(GameTestHelper test) {
		require(test, PatchworkConfig.settings().hoglinCharge(), "hoglinCharge");
		Hoglin hoglin = test.spawn(EntityTypes.HOGLIN, CENTER);
		var target = test.spawn(EntityTypes.COW, CENTER.offset(2, 0, 0));
		hoglin.setSprinting(true);
		test.assertTrue(hoglin.doHurtTarget(test.getLevel(), target), "Hoglin did not land a hit");
		test.assertTrue(target.getDeltaMovement().y >= 0.68, "Charging hit did not launch the target upward");
		test.assertTrue(target.getDeltaMovement().x > 0.0, "Charging hit did not push away from Hoglin");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void slimeballHealsSlimes(GameTestHelper test) {
		require(test, PatchworkConfig.settings().throwableSlimeballs(), "throwableSlimeballs");
		test.setBlock(CENTER.offset(0, -1, 2), Blocks.STONE);
		Slime slime = test.spawn(EntityTypes.SLIME, CENTER.offset(0, 0, 2));
		slime.setNoAi(true);
		slime.setSize(2, true);
		slime.setHealth(slime.getMaxHealth() - 2.0F);
		float before = slime.getHealth();
		Vec3 launch = Vec3.atCenterOf(test.absolutePos(CENTER)).add(0.0, 0.0, -1.4);
		Snowball ball = new Snowball(test.getLevel(), launch.x, launch.y, launch.z, new ItemStack(Items.SLIME_BALL));
		ball.setDeltaMovement(0.0, 0.0, 0.8);
		test.getLevel().addFreshEntity(ball);
		test.runAfterDelay(6, () -> {
			test.assertTrue(slime.getHealth() > before, "Slimeball did not heal Slime");
			test.assertTrue(slime.hasEffect(MobEffects.SPEED), "Slimeball did not give Slime Speed");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void slimeballSlowsOtherTargets(GameTestHelper test) {
		require(test, PatchworkConfig.settings().throwableSlimeballs(), "throwableSlimeballs");
		test.setBlock(CENTER.offset(0, -1, 2), Blocks.STONE);
		var cow = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 2));
		cow.setNoAi(true);
		float before = cow.getHealth();
		Vec3 launch = Vec3.atCenterOf(test.absolutePos(CENTER)).add(0.0, 0.0, -1.4);
		Snowball ball = new Snowball(test.getLevel(), launch.x, launch.y, launch.z, new ItemStack(Items.SLIME_BALL));
		ball.setDeltaMovement(0.0, 0.0, 0.8);
		test.getLevel().addFreshEntity(ball);
		test.runAfterDelay(6, () -> {
			test.assertTrue(cow.getHealth() == before - 1.0F, "Slimeball did not deal 1 HP to a non-Slime");
			test.assertTrue(cow.hasEffect(MobEffects.SLOWNESS), "Slimeball did not slow its target");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 30)
	public void thrownFireChargeIgnitesSolidSideHit(GameTestHelper test) {
		require(test, PatchworkConfig.settings().throwableFireCharges(), "throwableFireCharges");
		BlockPos wall = CENTER.offset(1, 1, 0);
		test.setBlock(wall, Blocks.STONE);
		LargeFireball fireball = new LargeFireball(test.getLevel(), test.makeMockPlayer(GameType.SURVIVAL),
			new Vec3(1.0, 0.0, 0.0), 0);
		fireball.addTag(Patchwork.THROWN_FIRE_CHARGE_TAG);
		fireball.setPos(Vec3.atCenterOf(test.absolutePos(CENTER)).add(0.0, 1.0, 0.0));
		fireball.setDeltaMovement(0.8, 0.0, 0.0);
		test.getLevel().addFreshEntity(fireball);
		test.runAfterDelay(6, () -> {
			BlockPos top = wall.above();
			test.assertTrue(test.getBlockState(top).getBlock() instanceof BaseFireBlock,
				"Side impact on nonflammable block did not light its top surface");
			test.assertBlockPresent(Blocks.STONE, wall);
			test.succeed();
		});
	}
}
