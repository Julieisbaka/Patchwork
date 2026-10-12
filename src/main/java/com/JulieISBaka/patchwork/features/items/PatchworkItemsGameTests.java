package com.JulieISBaka.patchwork.features.items;

import com.JulieISBaka.patchwork.mixins.items.PotionArrowItemAccessor;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import com.JulieISBaka.patchwork.features.blocks.PotionCauldronEntity;
import com.JulieISBaka.patchwork.features.blocks.PotionCauldrons;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PatchworkItemsGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void bannerLoomAllowsNinePatternsButNotTen(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var menu = new net.minecraft.world.inventory.LoomMenu(1, player.getInventory());
		var pattern = test.getLevel().registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
				.getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CROSS);
		for (int layers = 6; layers <= 9; layers++) {
			ItemStack banner = new ItemStack(Items.BANNER.white());
			banner.set(DataComponents.BANNER_PATTERNS,
					new net.minecraft.world.level.block.entity.BannerPatternLayers(java.util.Collections.nCopies(layers,
							new net.minecraft.world.level.block.entity.BannerPatternLayers.Layer(pattern,
									DyeColor.RED))));
			menu.getBannerSlot().set(banner);
			menu.getDyeSlot().set(new ItemStack(Items.DYE.red()));
			menu.clickMenuButton(player, 0);
			ItemStack result = menu.getResultSlot().getItem();
			if (layers < 9) {
				test.assertTrue(
						!result.isEmpty() && result.get(DataComponents.BANNER_PATTERNS).layers().size() == layers + 1,
						"Loom did not add pattern " + (layers + 1));
			} else {
				test.assertTrue(result.isEmpty(), "Loom allowed a tenth banner pattern");
				var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
						java.util.List.of(banner, new ItemStack(Items.BANNER.white())));
				var recipe = test.getLevel().recipeAccess()
						.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
				test.assertTrue(recipe.isPresent(), "Nine-layer banner could not be copied");
				var copy = recipe.orElseThrow().value().assemble(input);
				test.assertTrue(
						copy.get(DataComponents.BANNER_PATTERNS).equals(banner.get(DataComponents.BANNER_PATTERNS)),
						"Copied banner lost its nine layers");
			}
		}
		test.succeed();
	}

	@GameTest
	public void washingUsesOneWaterLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().cauldronCleaning(), "cauldronCleaning");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var dyed = new net.minecraft.world.item.Item[] { Items.WOOL.red(), Items.DYED_TERRACOTTA.blue(),
				Items.STAINED_GLASS.green() };
		var clean = new net.minecraft.world.item.Item[] { Items.WOOL.white(), Items.TERRACOTTA, Items.GLASS };
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
	public void cauldronUndyesBundles(GameTestHelper test) {
		require(test, PatchworkConfig.settings().cauldronCleaning(), "cauldronCleaning");
		test.setBlock(CENTER, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
		net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) test
				.makeMockServerPlayer(GameType.SURVIVAL);
		ItemStack bundle = new ItemStack(Items.BUNDLE);
		bundle.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(0xD02090));
		player.setItemInHand(InteractionHand.MAIN_HAND, bundle);
		CauldronInteractions.WATER.get(bundle).interact(test.getBlockState(CENTER), test.getLevel(),
				test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, bundle);
		test.assertTrue(player.getMainHandItem().is(Items.BUNDLE)
				&& !player.getMainHandItem().has(DataComponents.DYED_COLOR),
				"Water cauldron did not remove the bundle's dye");
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
				"Undyeing a bundle did not use exactly one water level");
		var advancement = test.getLevel().getServer().getAdvancements()
				.get(Patchwork.id("adventure/restored_to_color"));
		test.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
				"Undyeing a bundle did not award Restored to Color");
		test.succeed();
	}

	@GameTest
	public void leatherAndCarvedPumpkinCraftAPlayerHead(GameTestHelper test) {
		var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, java.util.List.of(
				new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER),
				new ItemStack(Items.LEATHER), new ItemStack(Items.CARVED_PUMPKIN), new ItemStack(Items.LEATHER),
				new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER)));
		var recipe = test.getLevel().getServer().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(Items.PLAYER_HEAD),
				"Leather around a carved pumpkin did not craft a player head");
		test.succeed();
	}

	@GameTest
	public void stoneToolsCanUseStoneAndCobblestoneTogether(GameTestHelper test) {
		record ToolRecipe(net.minecraft.world.item.Item result, java.util.List<ItemStack> input) {
		}
		ItemStack empty = ItemStack.EMPTY;
		var recipes = java.util.List.of(
				new ToolRecipe(Items.STONE_PICKAXE, java.util.List.of(new ItemStack(Items.STONE),
						new ItemStack(Items.COBBLESTONE), new ItemStack(Items.STONE), empty, new ItemStack(Items.STICK),
						empty, empty, new ItemStack(Items.STICK), empty)),
				new ToolRecipe(Items.STONE_AXE, java.util.List.of(new ItemStack(Items.STONE),
						new ItemStack(Items.COBBLESTONE), empty, new ItemStack(Items.STONE), new ItemStack(Items.STICK),
						empty, empty, new ItemStack(Items.STICK), empty)),
				new ToolRecipe(Items.STONE_HOE, java.util.List.of(new ItemStack(Items.STONE),
						new ItemStack(Items.COBBLESTONE), empty, empty, new ItemStack(Items.STICK), empty, empty,
						new ItemStack(Items.STICK), empty)),
				new ToolRecipe(Items.STONE_SHOVEL, java.util.List.of(new ItemStack(Items.STONE), empty, empty,
						new ItemStack(Items.STICK), empty, empty, new ItemStack(Items.STICK), empty, empty)),
				new ToolRecipe(Items.STONE_SWORD, java.util.List.of(new ItemStack(Items.STONE), empty, empty,
						new ItemStack(Items.COBBLESTONE), empty, empty, new ItemStack(Items.STICK), empty, empty)));
		var manager = test.getLevel().getServer().getRecipeManager();
		for (ToolRecipe recipeCase : recipes) {
			var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, recipeCase.input());
			var recipe = manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input,
					test.getLevel());
			test.assertTrue(recipe.isPresent(),
					"No matching stone tool recipe accepted a stone/cobblestone mixture for " + recipeCase.result());
			test.assertTrue(recipe.orElseThrow().value().assemble(input).is(recipeCase.result()),
					"Stone tool recipe produced the wrong item for " + recipeCase.result());
		}
		test.succeed();
	}

	@GameTest
	public void elytraCanBeDyedWithCraftingDyeRecipe(GameTestHelper test) {
		var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
				java.util.List.of(new ItemStack(Items.ELYTRA), new ItemStack(Items.DYE.red())));
		var recipe = test.getLevel().getServer().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input)
				.has(DataComponents.DYED_COLOR), "Dye did not add a color to the Elytra");
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
		CauldronInteractions.EMPTY.get(potion).interact(test.getBlockState(CENTER), test.getLevel(),
				test.absolutePos(CENTER), player, InteractionHand.MAIN_HAND, potion);
		test.assertBlockPresent(PotionCauldrons.BLOCK, CENTER);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 1,
				"Potion did not fill the empty cauldron with one level");
		test.assertTrue(
				((PotionCauldronEntity) test.getLevel().getBlockEntity(test.absolutePos(CENTER))).potion()
						.equals(contents),
				"Potion contents were not retained");
		test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Pouring did not return a glass bottle");
		player.setItemInHand(InteractionHand.MAIN_HAND,
				PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2,
				"Matching potion did not refill exactly one level");
		player.setItemInHand(InteractionHand.MAIN_HAND, PotionContents.createItemStack(Items.POTION, Potions.HEALING));
		test.useBlock(CENTER, player);
		test.assertTrue(test.getBlockState(CENTER).getValue(LayeredCauldronBlock.LEVEL) == 2
				&& ((PotionCauldronEntity) test.getLevel().getBlockEntity(test.absolutePos(CENTER))).potion()
						.equals(contents),
				"Different potion contents were mixed");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW));
		test.getBlockState(CENTER).useItemOn(player.getOffhandItem(), test.getLevel(), player, InteractionHand.OFF_HAND,
				new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)), Direction.NORTH, test.absolutePos(CENTER),
						false));
		test.assertTrue(
				player.getOffhandItem().is(Items.TIPPED_ARROW) && player.getOffhandItem().getCount() == 1
						&& player.getOffhandItem().get(DataComponents.POTION_CONTENTS)
								.equals(((PotionCauldronEntity) test.getLevel()
										.getBlockEntity(test.absolutePos(CENTER))).potion()),
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
		((PotionCauldronEntity) test.getLevel().getBlockEntity(test.absolutePos(CENTER))).setPotion(contents);
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
		test.assertTrue(
				player.getMainHandItem().is(Items.POTION)
						&& contents.equals(player.getMainHandItem().get(DataComponents.POTION_CONTENTS)),
				"Single bottle was not replaced with the correct potion");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
		test.getBlockState(CENTER).useItemOn(player.getOffhandItem(), test.getLevel(), player, InteractionHand.OFF_HAND,
				new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)), Direction.NORTH, test.absolutePos(CENTER),
						false));
		test.assertBlockPresent(Blocks.CAULDRON, CENTER);
		test.assertTrue(
				player.getOffhandItem().is(Items.POTION)
						&& contents.equals(player.getOffhandItem().get(DataComponents.POTION_CONTENTS)),
				"Offhand extraction lost the last potion level");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void potionDroppedSingleArrowUsesOnePotionLevel(GameTestHelper test) {
		require(test, PatchworkConfig.settings().potionCauldrons(), "potionCauldrons");
		test.setBlock(CENTER, PotionCauldrons.BLOCK.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
		((PotionCauldronEntity) test.getLevel().getBlockEntity(test.absolutePos(CENTER))).setPotion(
				PotionContents.createItemStack(Items.POTION, Potions.HEALING).get(DataComponents.POTION_CONTENTS));
		Vec3 center = Vec3.atBottomCenterOf(test.absolutePos(CENTER)).add(0, 0.5, 0);
		ItemEntity arrows = new ItemEntity(test.getLevel(), center.x, center.y, center.z, new ItemStack(Items.ARROW));
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
		((PotionCauldronEntity) test.getLevel().getBlockEntity(test.absolutePos(CENTER))).setPotion(contents);
		Vec3 center = Vec3.atBottomCenterOf(test.absolutePos(CENTER)).add(0, 0.5, 0);
		ItemEntity arrows = new ItemEntity(test.getLevel(), center.x, center.y, center.z,
				new ItemStack(Items.ARROW, 8));
		arrows.setDeltaMovement(Vec3.ZERO);
		arrows.setPickUpDelay(40);
		arrows.setTarget(playerUuidForArrowTest());
		((PotionArrowItemAccessor) arrows).patchwork$setAge(1000);
		test.getLevel().addFreshEntity(arrows);
		test.runAfterDelay(5, () -> {
			test.assertTrue(
					!arrows.isRemoved() && arrows.getItem().is(Items.TIPPED_ARROW) && arrows.getItem().getCount() == 3,
					"Full cauldron did not convert the original entity into three arrows");
			test.assertBlockPresent(Blocks.CAULDRON, CENTER);
			int tippedCount = 0;
			int plainCount = 0;
			for (ItemEntity item : test.getEntities(EntityTypes.ITEM)) {
				test.assertTrue(item.getAge() >= 1000 && item.hasPickUpDelay(),
						"Splitting arrows lost their age or pickup delay");
				test.assertTrue(playerUuidForArrowTest().equals(((PotionArrowItemAccessor) item).patchwork$getTarget()),
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

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}

	private static java.util.UUID playerUuidForArrowTest() {
		return java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
	}
}
