package com.JulieISBaka.patchwork;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.cauldron.CauldronInteractions;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class PatchworkGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}

	@GameTest
	public void unlitTorchVariants(GameTestHelper test) {
		require(test, PatchworkConfig.settings().unlitTorches(), "unlitTorches");
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
		require(test, PatchworkConfig.settings().unlitTorches(), "unlitTorches");
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
		test.setBlock(CENTER, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		ItemStack potion = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
		player.setItemInHand(InteractionHand.MAIN_HAND, potion);
		CauldronInteractions.WATER.get(potion).interact(test.getBlockState(CENTER),
			test.getLevel(), test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, potion);
		test.assertBlockPresent(PotionCauldrons.BLOCK, CENTER);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
			"Potion did not replace water with one level");
		test.assertTrue(((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER)))
			.potion().equals(potion.get(DataComponents.POTION_CONTENTS)), "Potion contents were not retained");
		test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Pouring did not return a glass bottle");
		player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
			"Matching potion did not refill exactly one level");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 8));
		test.useBlock(CENTER, player);
		test.assertTrue(player.getOffhandItem().is(Items.TIPPED_ARROW)
			&& player.getOffhandItem().getCount() == 8
			&& player.getOffhandItem().get(DataComponents.POTION_CONTENTS).equals(
				((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER))).potion()),
			"Offhand dipping did not return eight matching tipped arrows");
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
			"Offhand dipping did not use one potion level");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void droppedArrowsUseOnePotionLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, PotionCauldrons.BLOCK.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
		((PotionCauldronEntity)test.getLevel().getBlockEntity(test.absolutePos(CENTER)))
			.setPotion(PotionContents.createItemStack(Items.POTION, Potions.HEALING)
				.get(DataComponents.POTION_CONTENTS));
		Vec3 center = Vec3.atBottomCenterOf(test.absolutePos(CENTER)).add(0, 0.5, 0);
		ItemEntity arrows = new ItemEntity(test.getLevel(), center.x, center.y, center.z,
			new ItemStack(Items.ARROW, 9));
		test.getLevel().addFreshEntity(arrows);
		test.runAfterDelay(5, () -> {
			test.assertTrue(arrows.getItem().getCount() == 1, "Dropped stack did not consume exactly eight arrows");
			test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
				"Dropped arrows did not use one potion level");
			test.assertItemEntityPresent(Items.TIPPED_ARROW, CENTER, 2);
			test.succeed();
		});
	}

	@GameTest
	public void dyedShulkerChangesColor(GameTestHelper test) {
		require(test, PatchworkConfig.settings().shulkerDyeing(), "shulkerDyeing");
		Shulker shulker = test.spawn(EntityTypes.SHULKER, CENTER);
		Player player = test.makeMockPlayer(GameType.CREATIVE);
		ItemStack dye = new ItemStack(Items.RED_DYE);
		Items.RED_DYE.interactLivingEntity(dye, player, shulker, InteractionHand.MAIN_HAND);
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
		require(test, PatchworkConfig.settings().unlitTorches(), "unlitTorches");
		test.assertTrue(test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING),
			"Enable mobGriefing before running the Breeze extinguishing test");
		test.setBlock(CENTER.below(), Blocks.STONE);
		BlockPos torch = CENTER.offset(1, 0, 0);
		BlockPos campfire = CENTER.offset(0, 0, 2);
		test.setBlock(torch.below(), Blocks.STONE);
		test.setBlock(campfire.below(), Blocks.STONE);
		test.setBlock(torch, Blocks.TORCH);
		test.setBlock(campfire, Blocks.CAMPFIRE);
		Breeze breeze = test.spawn(EntityTypes.BREEZE, CENTER);
		Player attacker = test.makeMockPlayer(GameType.SURVIVAL);
		breeze.hurtServer(test.getLevel(), breeze.damageSources().playerAttack(attacker), 1.0F);
		test.assertBlockPresent(UnlitTorches.TORCH, torch);
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
