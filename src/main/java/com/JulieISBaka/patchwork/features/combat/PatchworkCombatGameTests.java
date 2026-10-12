package com.JulieISBaka.patchwork.features.combat;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import com.JulieISBaka.patchwork.features.items.SoulFireCharges;
import com.JulieISBaka.patchwork.features.items.SoulFireSupport;
import com.JulieISBaka.patchwork.features.items.SoulFireball;
import com.JulieISBaka.patchwork.features.mobs.SoulGolem;
import com.JulieISBaka.patchwork.features.mobs.SoulGolems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PatchworkCombatGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void glassAndBeaconsUsePickaxeMiningTag(GameTestHelper test) {
		test.assertTrue(Blocks.GLASS.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
				"Glass is not tagged as mineable with a pickaxe");
		test.assertTrue(Blocks.GLASS_PANE.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
				"Glass panes are not tagged as mineable with a pickaxe");
		test.assertTrue(Blocks.TINTED_GLASS.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
				"Tinted glass is not tagged as mineable with a pickaxe");
		test.assertTrue(Blocks.BEACON.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
				"Beacons are not tagged as mineable with a pickaxe");
		test.succeed();
	}

	@GameTest
	public void renamingAnItemDoesNotIncreaseItsAnvilRepairCost(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var menu = new net.minecraft.world.inventory.AnvilMenu(1, player.getInventory());
		ItemStack input = new ItemStack(Items.DIAMOND_PICKAXE);
		input.set(DataComponents.REPAIR_COST, 12);
		menu.getSlot(net.minecraft.world.inventory.AnvilMenu.INPUT_SLOT).set(input);
		test.assertTrue(menu.setItemName("Renamed pickaxe"), "Anvil did not accept the new custom name");
		ItemStack output = menu.getSlot(net.minecraft.world.inventory.AnvilMenu.RESULT_SLOT).getItem();
		test.assertTrue(!output.isEmpty()
				&& output.getOrDefault(DataComponents.REPAIR_COST, 0) == 12,
				"Renaming increased the item's anvil repair cost");
		test.succeed();
	}

	@GameTest
	public void blueWitherSkullsHomeTowardPlayers(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		BlockPos center = test.absolutePos(CENTER);
		Spider shooter = new Spider(EntityTypes.SPIDER, level);
		shooter.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		test.assertTrue(level.addFreshEntity(player), "Could not add player for wither-skull homing test");
		player.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 6.5);
		Spider nearerMob = new Spider(EntityTypes.SPIDER, level);
		nearerMob.setPos(center.getX() + 1.5, center.getY(), center.getZ() + 0.5);
		test.assertTrue(level.addFreshEntity(shooter) && level.addFreshEntity(nearerMob),
				"Could not add entities for wither-skull homing test");
		WitherSkull skull = new WitherSkull(level, shooter, new Vec3(1.0, 0.0, 0.0));
		skull.setPos(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
		skull.setDangerous(true);
		skull.setDeltaMovement(new Vec3(1.0, 0.0, 0.0));
		test.assertTrue(WitherSkullHoming.findPlayerTarget(skull, level).orElse(null) == player,
				"Blue wither skull did not prioritize a player over a closer mob");
		WitherSkullHoming.steerTowardPlayer(skull, level);
		test.assertTrue(skull.getDeltaMovement().z > 0.0,
				"Blue wither skull did not steer toward the player");
		Vec3 steeredVelocity = skull.getDeltaMovement();
		skull.setDangerous(false);
		WitherSkullHoming.steerTowardPlayer(skull, level);
		test.assertTrue(skull.getDeltaMovement().equals(steeredVelocity),
				"Normal wither skull unexpectedly homed");
		test.succeed();
	}

	@GameTest
	public void soulFireChargeRecipeAcceptsMixedSoulMaterials(GameTestHelper test) {
		for (var material : new net.minecraft.world.item.Item[] { Items.SOUL_SAND, Items.SOUL_SOIL }) {
			var items = new java.util.ArrayList<ItemStack>();
			for (int i = 0; i < 9; i++) {
				items.add(new ItemStack(i == 4 ? Items.FIRE_CHARGE : (i % 2 == 0 ? material : Items.SOUL_SOIL)));
			}
			var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
			var recipe = test.getLevel().recipeAccess()
					.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
			test.assertTrue(
					recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(SoulFireCharges.ITEM)
							&& recipe.orElseThrow().value().assemble(input).getCount() == 1,
					"Eight soul blocks around a fire charge did not craft one Soul Fire Charge");
			items.set(0, ItemStack.EMPTY);
			input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
			test.assertTrue(test.getLevel().recipeAccess()
					.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel())
					.isEmpty(),
					"Incomplete soul-charge ring was accepted");
		}
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void soulFireChargeIgnitesVanillaSoulFireAndDoesNotExplode(GameTestHelper test) {
		BlockPos wall = CENTER.offset(1, 1, 0);
		test.setBlock(wall, Blocks.STONE);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var fireball = SoulFireCharges.shoot(test.getLevel(), player,
				Vec3.atCenterOf(test.absolutePos(CENTER)).add(0, 1, 0), new Vec3(1, 0, 0));
		test.runAfterDelay(6, () -> {
			test.assertBlockPresent(Blocks.SOUL_FIRE, wall.above());
			test.assertBlockPresent(Blocks.STONE, wall);
			test.assertTrue(test.getBlockState(wall.above()).getValue(SoulFireSupport.CHARGE_PLACED),
					"Charge did not mark real soul fire for extended support");
			test.setBlock(wall.above().east(), Blocks.GLASS);
			test.assertTrue(fireball.isRemoved(), "Soul projectile was not removed on impact");
		});
		test.runAfterDelay(12, () -> {
			test.assertBlockPresent(Blocks.SOUL_FIRE, wall.above());
			test.setBlock(wall, Blocks.AIR);
			test.assertBlockPresent(Blocks.AIR, wall.above());
			test.succeed();
		});
	}

	@GameTest
	public void soulFireChargeImpactDoesNotPlaceUnsupportedOrOverwriteFire(GameTestHelper test) {
		BlockPos wall = CENTER.offset(1, 1, 0);
		test.setBlock(wall, Blocks.STONE_SLAB);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		SoulFireCharges.shoot(test.getLevel(), player, Vec3.atCenterOf(test.absolutePos(CENTER)).add(0, 1, 0),
				new Vec3(1, 0, 0));
		test.runAfterDelay(6, () -> {
			test.assertBlockPresent(Blocks.STONE_SLAB, wall);
			test.assertBlockPresent(Blocks.AIR, wall.above());
			test.setBlock(wall, Blocks.SOUL_SAND);
			test.setBlock(wall.above(), Blocks.GLASS);
			SoulFireCharges.ignite(test.getLevel(), new BlockHitResult(Vec3.atCenterOf(test.absolutePos(wall)),
					Direction.UP, test.absolutePos(wall), false));
			test.assertBlockPresent(Blocks.GLASS, wall.above());
			test.succeed();
		});
	}

	@GameTest
	public void soulFireChargeFlammableSidesAndLeaves(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		BlockPos support = CENTER.above();
		for (var block : java.util.List.of(Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.OAK_LEAVES,
				Blocks.WOOL.white())) {
			for (Direction face : java.util.List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST,
					Direction.WEST, Direction.UP)) {
				test.setBlock(support, block);
				BlockPos fire = support.relative(face);
				test.setBlock(fire, Blocks.AIR);
				test.setBlock(fire.below(), Blocks.AIR);
				// The top-face case needs the flammable support directly below the fire.
				test.setBlock(support, block);
				BlockPos absoluteSupport = test.absolutePos(support);
				BlockPos absoluteFire = test.absolutePos(fire);
				test.assertTrue(Blocks.FIRE.defaultBlockState().canSurvive(test.getLevel(), absoluteFire),
						"Test setup does not support vanilla fire");
				test.assertTrue(!Blocks.SOUL_FIRE.defaultBlockState().canSurvive(test.getLevel(), absoluteFire),
						"Ordinary soul fire gained flammable support");
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulFireCharges.ITEM, 2));
				var hit = new BlockHitResult(Vec3.atCenterOf(absoluteSupport), face, absoluteSupport, false);
				var result = SoulFireCharges.ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
				test.assertTrue(result == InteractionResult.SUCCESS && player.getMainHandItem().getCount() == 1,
						"Soul charge block use failed on " + block + " face " + face);
				var expectedFire = face.getAxis().isHorizontal()
						? SoulFireSupport.chargeFire(face.getOpposite())
						: SoulFireSupport.chargeFire();
				test.assertTrue(test.getBlockState(fire).equals(expectedFire),
						"Soul fire was not placed on the clicked flammable face");
				test.setBlock(fire.relative(face), Blocks.GLASS);
				test.assertBlockPresent(Blocks.SOUL_FIRE, fire);
				test.setBlock(fire.relative(face), Blocks.AIR);
				test.setBlock(support, Blocks.AIR);
				test.assertBlockPresent(Blocks.AIR, fire);
				test.setBlock(support, block);
				SoulFireCharges.ignite(test.getLevel(), hit);
				test.assertTrue(test.getBlockState(fire).equals(expectedFire),
						"Soul projectile ignition failed on " + block + " face " + face);
				test.assertBlockPresent(block, support);
				test.setBlock(fire, Blocks.AIR);
				test.setBlock(support, Blocks.AIR);
			}
		}
		test.succeed();
	}

	@GameTest(maxTicks = 15)
	public void soulFireChargeProjectileHitsLeafSide(GameTestHelper test) {
		BlockPos leaves = CENTER.east().above();
		test.setBlock(leaves, Blocks.OAK_LEAVES.defaultBlockState()
				.setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
		test.setBlock(leaves.west().below(), Blocks.AIR);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var projectile = SoulFireCharges.shoot(test.getLevel(), player,
				Vec3.atCenterOf(test.absolutePos(CENTER.above())), new Vec3(1, 0, 0));
		test.runAfterDelay(6, () -> {
			test.assertBlockPresent(Blocks.SOUL_FIRE, leaves.west());
			test.assertBlockPresent(Blocks.OAK_LEAVES, leaves);
			test.assertTrue(projectile.isRemoved(), "Leaf impact did not remove the projectile");
			test.setBlock(leaves, Blocks.AIR);
			test.assertBlockPresent(Blocks.AIR, leaves.west());
			test.succeed();
		});
	}

	@GameTest
	public void soulFireChargeSupportIsScopedAndPersists(GameTestHelper test) {
		BlockPos pos = test.absolutePos(CENTER.above());
		test.setBlock(CENTER, Blocks.STONE);
		var ordinary = Blocks.SOUL_FIRE.defaultBlockState();
		test.assertTrue(!ordinary.getValue(SoulFireSupport.CHARGE_PLACED) && !ordinary.canSurvive(test.getLevel(), pos),
				"Ordinary soul fire gained extended support");
		var marked = SoulFireSupport.chargeFire();
		test.assertTrue(marked.is(Blocks.SOUL_FIRE) && marked.canSurvive(test.getLevel(), pos),
				"Charge fire is not real soul fire supported by stone");
		var saved = net.minecraft.world.level.block.state.BlockState.CODEC
				.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, marked).getOrThrow();
		var restored = net.minecraft.world.level.block.state.BlockState.CODEC
				.parse(net.minecraft.nbt.NbtOps.INSTANCE, saved).getOrThrow();
		test.assertTrue(restored.equals(marked), "Saved soul-fire block state lost charge support");
		test.setBlock(CENTER.above(), restored);
		test.setBlock(CENTER.above().north(), Blocks.GLASS);
		test.assertTrue(test.getBlockState(CENTER.above()).equals(marked),
				"Neighbor update reset charge fire to ordinary soul fire");
		test.setBlock(CENTER, Blocks.OAK_PLANKS);
		test.assertBlockPresent(Blocks.OAK_PLANKS, CENTER);
		test.assertTrue(test.getBlockState(CENTER.above()).equals(marked),
				"Changing solid support removed charge fire or its flag");
		test.setBlock(CENTER, Blocks.AIR);
		test.assertBlockPresent(Blocks.AIR, CENTER.above());
		test.setBlock(CENTER, Blocks.SOUL_SAND);
		test.assertTrue(ordinary.canSurvive(test.getLevel(), pos), "Ordinary soul fire no longer supports soul sand");
		test.succeed();
	}

	@GameTest
	public void soulFireChargeCreativePlacementAndInvalidSupport(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.CREATIVE);
		player.getAbilities().instabuild = true;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulFireCharges.ITEM, 2));
		BlockPos pos = test.absolutePos(CENTER);
		for (var support : java.util.List.of(Blocks.GLASS, Blocks.OAK_PLANKS, Blocks.COBBLESTONE)) {
			test.setBlock(CENTER, support);
			test.setBlock(CENTER.above(), Blocks.AIR);
			var result = SoulFireCharges.ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
			test.assertTrue(result == InteractionResult.SUCCESS && player.getMainHandItem().getCount() == 2,
					"Creative placement consumed a charge or failed on a solid surface");
			test.assertBlockPresent(support, CENTER);
			test.assertBlockPresent(Blocks.SOUL_FIRE, CENTER.above());
		}
		test.setBlock(CENTER.above(), Blocks.GLASS);
		var occupied = SoulFireCharges.ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
		test.assertTrue(occupied == InteractionResult.FAIL, "Charge replaced an occupied fire position");
		test.setBlock(CENTER.above(), Blocks.AIR);
		test.setBlock(CENTER, Blocks.STONE_SLAB);
		var unsupported = SoulFireCharges.ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
		test.assertTrue(unsupported == InteractionResult.FAIL && player.getMainHandItem().getCount() == 2,
				"Charge placed floating fire over a bottom slab");
		test.succeed();
	}

	@GameTest
	public void soulFireChargeIsNextToNormalChargeInCombat(GameTestHelper test) {
		net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(test.getLevel().enabledFeatures(), true,
				test.getLevel().registryAccess());
		var tab = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB
				.getValue(net.minecraft.world.item.CreativeModeTabs.COMBAT);
		var items = new java.util.ArrayList<>(tab.getDisplayItems());
		int normalIndex = -1;
		int soulIndex = -1;
		for (int index = 0; index < items.size(); index++) {
			if (items.get(index).is(Items.FIRE_CHARGE)) {
				normalIndex = index;
			} else if (items.get(index).is(SoulFireCharges.ITEM)) {
				soulIndex = index;
			}
		}
		test.assertTrue(normalIndex >= 0 && soulIndex == normalIndex + 1,
				"Soul Fire Charge is not immediately after the normal charge in Combat");
		test.succeed();
	}

	@GameTest
	public void soulFireChargeThrowingConsumesAndRespectsCooldownAndClearance(GameTestHelper test) {
		require(test, PatchworkConfig.settings().throwableFireCharges(), "throwableFireCharges");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.snapTo(Vec3.atBottomCenterOf(test.absolutePos(CENTER.offset(0, 0, -2))));
		player.setYRot(0);
		player.setXRot(0);
		ItemStack charges = new ItemStack(SoulFireCharges.ITEM, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, charges);
		test.setBlock(CENTER.offset(0, 1, 3), Blocks.STONE);
		SoulFireCharges.throwCharge(player, test.getLevel(), InteractionHand.MAIN_HAND);
		test.assertTrue(charges.getCount() == 3 && !player.getCooldowns().isOnCooldown(charges)
				&& test.getEntities(SoulFireCharges.PROJECTILE).isEmpty(), "Blocked throw consumed a charge");
		test.setBlock(CENTER.offset(0, 1, 3), Blocks.AIR);
		SoulFireCharges.throwCharge(player, test.getLevel(), InteractionHand.MAIN_HAND);
		test.assertTrue(
				charges.getCount() == 2 && player.getCooldowns().isOnCooldown(charges)
						&& test.getEntities(SoulFireCharges.PROJECTILE).size() == 1,
				"Throw failed: count=" + charges.getCount() + ", cooldown="
						+ player.getCooldowns().isOnCooldown(charges)
						+ ", projectiles=" + test.getEntities(SoulFireCharges.PROJECTILE).size());
		SoulFireCharges.throwCharge(player, test.getLevel(), InteractionHand.MAIN_HAND);
		test.assertTrue(charges.getCount() == 2 && test.getEntities(SoulFireCharges.PROJECTILE).size() == 1,
				"Cooldown allowed another throw");
		test.succeed();
	}

	@GameTest
	public void soulFireChargeBlockUseAndDispenserKeepSoulFire(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (var support : new net.minecraft.world.level.block.Block[] { Blocks.STONE, Blocks.SOUL_SAND,
				Blocks.SOUL_SOIL }) {
			test.setBlock(CENTER, support);
			test.setBlock(CENTER.above(), Blocks.AIR);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulFireCharges.ITEM, 2));
			BlockPos pos = test.absolutePos(CENTER);
			var result = SoulFireCharges.ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
			test.assertBlockPresent(Blocks.SOUL_FIRE, CENTER.above());
			test.assertBlockPresent(support, CENTER);
			test.assertTrue(result == InteractionResult.SUCCESS && player.getMainHandItem().getCount() == 1,
					"Direct soul-fire placement did not use one charge");
		}
		var projectile = ((net.minecraft.world.item.ProjectileItem) SoulFireCharges.ITEM).asProjectile(test.getLevel(),
				Vec3.atCenterOf(test.absolutePos(CENTER)), new ItemStack(SoulFireCharges.ITEM), Direction.EAST);
		test.assertTrue(projectile instanceof SoulFireball && projectile.getType() == SoulFireCharges.PROJECTILE,
				"Dispenser factory returned an ordinary fireball");
		test.assertTrue(
				net.minecraft.world.level.block.DispenserBlock.DISPENSER_REGISTRY.containsKey(SoulFireCharges.ITEM),
				"Soul Fire Charge dispenser behavior was not registered");
		test.succeed();
	}

	@GameTest(maxTicks = 15)
	public void soulFireChargeDispenserActuallyLaunchesAndIgnites(GameTestHelper test) {
		BlockPos dispenserPos = CENTER.west();
		test.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState()
				.setValue(net.minecraft.world.level.block.DispenserBlock.FACING, Direction.EAST));
		var entity = test.getLevel().getBlockEntity(test.absolutePos(dispenserPos));
		test.assertTrue(entity instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity,
				"Dispenser block entity is missing");
		var dispenser = (net.minecraft.world.level.block.entity.DispenserBlockEntity) entity;
		dispenser.setItem(0, new ItemStack(SoulFireCharges.ITEM, 2));
		BlockPos wall = CENTER.east();
		test.setBlock(wall, Blocks.STONE);
		test.pulseRedstone(dispenserPos.below(), 2);
		test.runAfterDelay(10, () -> {
			test.assertTrue(dispenser.getItem(0).is(SoulFireCharges.ITEM) && dispenser.getItem(0).getCount() == 1,
					"Powered dispenser did not consume exactly one Soul Fire Charge");
			test.assertTrue(test.getBlockState(wall.above()).equals(SoulFireSupport.chargeFire()),
					"Dispenser projectile did not place charge-supported real soul fire");
			test.assertBlockPresent(Blocks.STONE, wall);
			test.succeed();
		});
	}

	@GameTest(maxTicks = 15)
	public void soulFireChargeDoesOneMoreDamageThanRegularCharge(GameTestHelper test) {
		var normalTarget = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 2));
		var soulTarget = test.spawn(EntityTypes.COW, CENTER.offset(2, 0, 2));
		normalTarget.setNoAi(true);
		soulTarget.setNoAi(true);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		LargeFireball normal = new LargeFireball(test.getLevel(), player, new Vec3(0, 0, 1), 0);
		normal.addTag(Patchwork.THROWN_FIRE_CHARGE_TAG);
		normal.setPos(normalTarget.getEyePosition().add(0, 0, -1));
		normal.setDeltaMovement(0, 0, 0.65);
		test.getLevel().addFreshEntity(normal);
		SoulFireCharges.shoot(test.getLevel(), player, soulTarget.getEyePosition().add(0, 0, -1), new Vec3(0, 0, 1));
		test.runAfterDelay(4, () -> {
			test.assertTrue(normalTarget.getHealth() == normalTarget.getMaxHealth() - 2,
					"Regular Fire Charge direct damage changed");
			test.assertTrue(soulTarget.getHealth() == soulTarget.getMaxHealth() - 3,
					"Soul Fire Charge did not deal exactly one additional damage");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void soulFireChargeSkipsOwnersInItsFlightPath(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		golem.setNoAi(true);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.snapTo(Vec3.atBottomCenterOf(test.absolutePos(CENTER.offset(0, 0, 1))));
		test.getLevel().addFreshEntity(owner);
		golem.setOwner(owner);
		var target = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 3));
		target.setNoAi(true);
		golem.performRangedAttack(target, 1);
		test.runAfterDelay(10, () -> {
			test.assertTrue(owner.getHealth() == owner.getMaxHealth() && owner.getRemainingFireTicks() <= 0,
					"Soul Golem shot damaged or ignited its owner");
			test.assertTrue(target.getHealth() < target.getMaxHealth(),
					"Projectile did not pass through its protected owner");
			test.succeed();
		});
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

	@GameTest
	public void shieldBlocksKnockBackMeleeAttacker(GameTestHelper test) {
		Player defender = test.makeMockPlayer(GameType.SURVIVAL);
		defender.snapTo(Vec3.atBottomCenterOf(test.absolutePos(CENTER)));
		var attacker = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 2));
		attacker.setNoAi(true);
		defender.setYRot(0.0F);
		defender.setItemInHand(InteractionHand.OFF_HAND, Items.SHIELD.getDefaultInstance());
		defender.startUsingItem(InteractionHand.OFF_HAND);
		for (int tick = 0; tick < 10; tick++) {
			defender.tick();
		}

		test.assertTrue(defender.isBlocking() && defender.getItemBlockingWith() != null,
				"Shield was not actively blocking before the hit");
		float healthBefore = defender.getHealth();
		defender.hurtServer(test.getLevel(), defender.damageSources().mobAttack(attacker), 4.0F);
		test.assertTrue(defender.getHealth() == healthBefore, "Shield did not block the melee damage");
		test.assertTrue(attacker.getDeltaMovement().horizontalDistanceSqr() > 0.0,
				"Blocking with a shield did not knock back the attacker");
		test.succeed();
	}

	@GameTest
	public void loyalTridentReturnsAfterEnteringVoid(GameTestHelper test) {
		var owner = test.makeMockServerPlayer(GameType.SURVIVAL);
		owner.setPos(Vec3.atBottomCenterOf(test.absolutePos(CENTER)));
		var loyalty = test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOYALTY);
		ItemStack tridentStack = new ItemStack(Items.TRIDENT);
		tridentStack.enchant(loyalty, 1);
		var trident = new net.minecraft.world.entity.projectile.arrow.ThrownTrident(test.getLevel(), owner,
				tridentStack);
		trident.setPos(owner.getX(), test.getLevel().getMinY() - 65.0, owner.getZ());
		test.getLevel().addFreshEntity(trident);
		trident.checkBelowWorld();
		test.assertTrue(!trident.isRemoved(), "Loyalty trident was discarded on entering the void");
		trident.tick();
		test.assertTrue(!trident.isRemoved() && trident.getDeltaMovement().y > 0.0,
				"Loyalty trident did not begin returning toward its owner from the void");
		test.succeed();
	}

	@GameTest
	public void incomingDamageInterruptsFoodUse(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		Player attacker = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
		player.startUsingItem(InteractionHand.MAIN_HAND);
		test.assertTrue(player.isUsingItem(), "Player did not start eating before the attack");

		test.assertTrue(player.hurtServer(test.getLevel(), player.damageSources().playerAttack(attacker), 1.0F),
				"Attack did not damage the eating player");
		test.assertTrue(!player.isUsingItem(), "Incoming damage did not interrupt food use");
		test.succeed();
	}

	@GameTest
	public void eggAndSnowballThrowsHaveBriefCooldown(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EGG, 2));
		Items.EGG.use(test.getLevel(), player, InteractionHand.MAIN_HAND);
		test.assertTrue(player.getCooldowns().isOnCooldown(Items.EGG.getDefaultInstance()),
				"Throwing an egg did not start its brief cooldown");

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SNOWBALL, 2));
		Items.SNOWBALL.use(test.getLevel(), player, InteractionHand.MAIN_HAND);
		test.assertTrue(player.getCooldowns().isOnCooldown(Items.SNOWBALL.getDefaultInstance()),
				"Throwing a snowball did not start its brief cooldown");
		test.succeed();
	}

	@GameTest
	public void hotbarSwapPreservesAttackStrength(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory().setItem(0, Items.IRON_SWORD.getDefaultInstance());
		player.getInventory().setItem(1, Items.DIAMOND_SWORD.getDefaultInstance());
		player.resetAttackStrengthTicker();
		for (int tick = 0; tick < 8; tick++) {
			player.tick();
		}
		float strengthBeforeSwap = player.getAttackStrengthScale(0.0F);
		player.getInventory().setSelectedSlot(1);
		player.tick();
		test.assertTrue(strengthBeforeSwap > 0.0F && player.getAttackStrengthScale(0.0F) > strengthBeforeSwap,
				"Switching hotbar items reset the attack strength cooldown");
		test.succeed();
	}

	@GameTest
	public void attackingDoesNotCancelSprinting(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var target = test.spawn(EntityTypes.COW, CENTER);
		player.setSprinting(true);
		player.attack(target);
		test.assertTrue(player.isSprinting(), "Attacking a target stopped the player's sprint");
		test.succeed();
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

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}
}
