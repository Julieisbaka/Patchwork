package com.JulieISBaka.patchwork.features.blocks;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import com.JulieISBaka.patchwork.features.items.SoulFireCharges;
import com.JulieISBaka.patchwork.features.mobs.SoulGolems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PatchworkBlocksGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void woodcutterRegistersWoodcuttingRecipes(GameTestHelper test) {
		var recipes = test.getLevel().recipeAccess().stonecutterRecipes();
		var plankRecipes = recipes.selectByInput(new ItemStack(Items.OAK_PLANKS));
		var logRecipes = recipes.selectByInput(new ItemStack(Items.OAK_LOG));
		test.assertTrue(Woodcutter.BLOCK.asItem() == Woodcutter.ITEM, "Woodcutter block item was not registered");
		test.assertTrue(Patchwork.id("woodcutter")
				.equals(net.minecraft.core.registries.BuiltInRegistries.MENU.getKey(Woodcutter.MENU_TYPE)),
				"Woodcutter menu type was not registered");
		test.assertTrue(plankRecipes.entries().stream().anyMatch(entry -> entry.recipe().recipe()
				.map(holder -> holder.id().identifier().equals(Patchwork.id("woodcutter/oak_stairs"))).orElse(false)),
				"Oak plank to stair recipe was not loaded");
		test.assertTrue(logRecipes.entries().stream().anyMatch(entry -> entry.recipe().recipe()
				.map(holder -> holder.id().identifier().equals(Patchwork.id("woodcutter/oak_planks_from_logs")))
				.orElse(false)), "Oak log to planks recipe was not loaded");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		WoodcutterMenu menu = new WoodcutterMenu(0, player.getInventory(),
				ContainerLevelAccess.create(test.getLevel(), test.absolutePos(CENTER)));
		menu.container.setItem(0, new ItemStack(Items.OAK_PLANKS));
		menu.slotsChanged(menu.container);
		int stairsRecipe = -1;
		for (int i = 0; i < menu.getVisibleRecipes().entries().size(); i++) {
			if (menu.getVisibleRecipes().entries().get(i).recipe().recipe()
					.map(holder -> holder.id().identifier().equals(Patchwork.id("woodcutter/oak_stairs")))
					.orElse(false)) {
				stairsRecipe = i;
				break;
			}
		}
		test.assertTrue(stairsRecipe >= 0, "Saw menu did not display the oak stairs recipe");
		test.assertTrue(menu.clickMenuButton(player, stairsRecipe), "Saw menu did not select the oak stairs recipe");
		test.assertTrue(menu.getSlot(1).getItem().is(Items.OAK_STAIRS),
				"Saw menu selection did not produce oak stairs");
		test.succeed();
	}

	@GameTest
	public void copiedBlockPropertiesPreserveLootAndNames(GameTestHelper test) {
		var blocks = new java.util.ArrayList<net.minecraft.world.level.block.Block>(java.util.List.of(
				CharcoalBlocks.BLOCK, GardenBlocks.WAX_BLOCK, GardenBlocks.PAEONIA, GardenBlocks.POTTED_PAEONIA,
				PumpkinLanterns.SOUL_BLOCK, UnlitTorches.TORCH, UnlitTorches.WALL_TORCH,
				UnlitTorches.SOUL_TORCH, UnlitTorches.SOUL_WALL_TORCH,
				UnlitTorches.REDSTONE_TORCH, UnlitTorches.REDSTONE_WALL_TORCH,
				UnlitLanterns.LANTERN, UnlitLanterns.SOUL_LANTERN));
		blocks.addAll(CopperTorches.LIT.asList());
		blocks.addAll(CopperTorches.LIT_WALL.asList());
		blocks.addAll(CopperTorches.UNLIT.asList());
		blocks.addAll(CopperTorches.UNLIT_WALL.asList());
		blocks.addAll(UnlitLanterns.COPPER_LANTERN.asList());
		for (var block : blocks) {
			var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
			if (!id.getNamespace().equals(Patchwork.MOD_ID)) {
				continue;
			}
			String drop = id.getPath().replace("_wall_torch", "_torch");
			var expected = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
					Patchwork.id("blocks/" + drop));
			test.assertTrue(block.getLootTable().equals(java.util.Optional.of(expected)),
					"Copied block inherited vanilla drops: " + id);
			test.assertTrue(block.getDescriptionId().equals(id.toLanguageKey("block")),
					"Copied block inherited a vanilla description: " + id);
		}
		test.assertTrue(PotionCauldrons.BLOCK.getLootTable().equals(Blocks.CAULDRON.getLootTable())
				&& PotionCauldrons.BLOCK.getDescriptionId().equals(Blocks.CAULDRON.getDescriptionId()),
				"Potion cauldron no longer preserves the vanilla cauldron's drops and name");
		test.succeed();
	}

	@GameTest
	public void restoredPaintingsArePlaceableAndInCreative(GameTestHelper test) {
		var paintings = test.getLevel().registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.PAINTING_VARIANT);
		var variants = java.util.List.of(
				net.minecraft.world.entity.decoration.painting.PaintingVariants.EARTH,
				net.minecraft.world.entity.decoration.painting.PaintingVariants.WIND,
				net.minecraft.world.entity.decoration.painting.PaintingVariants.WATER,
				net.minecraft.world.entity.decoration.painting.PaintingVariants.FIRE);
		net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(test.getLevel().enabledFeatures(), true,
				test.getLevel().registryAccess());
		var functional = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB
				.getValue(CreativeModeTabs.FUNCTIONAL_BLOCKS);
		for (var key : variants) {
			var variant = paintings.getOrThrow(key);
			test.assertTrue(variant.is(net.minecraft.tags.PaintingVariantTags.PLACEABLE),
					key + " is missing from the normal painting placement pool");
			test.assertTrue(functional.getDisplayItems().stream().anyMatch(stack -> stack.is(Items.PAINTING)
					&& variant.equals(stack.get(DataComponents.PAINTING_VARIANT))),
					key + " is missing from Creative Functional Blocks");
		}
		test.assertTrue(paintings.getOrThrow(net.minecraft.world.entity.decoration.painting.PaintingVariants.KEBAB)
				.is(net.minecraft.tags.PaintingVariantTags.PLACEABLE), "Vanilla paintings were removed from the pool");
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				test.setBlock(CENTER.offset(x, y, -1), Blocks.STONE);
			}
		}
		var painting = net.minecraft.world.entity.decoration.painting.Painting
				.create(test.getLevel(), test.absolutePos(CENTER), Direction.SOUTH);
		test.assertTrue(painting.isPresent(), "Normal painting placement failed on a 2x2 wall");
		test.assertTrue(painting.orElseThrow().getVariant().value().width() == 2
				&& painting.orElseThrow().getVariant().value().height() == 2,
				"Normal painting placement no longer chooses a fitting 2x2 painting");
		test.succeed();
	}

	@GameTest
	public void gardenWaxBlockRequiresNineHoneycomb(GameTestHelper test) {
		var items = new java.util.ArrayList<ItemStack>();
		for (int i = 0; i < 9; i++) {
			items.add(new ItemStack(Items.HONEYCOMB));
		}
		var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
		var recipe = test.getLevel().getServer().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(
				recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(GardenBlocks.WAX_ITEM)
						&& recipe.orElseThrow().value().assemble(input).getCount() == 1,
				"Nine honeycomb did not craft exactly one wax block");
		items.set(4, ItemStack.EMPTY);
		input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
		test.assertTrue(
				test.getLevel().getServer().getRecipeManager()
						.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel())
						.isEmpty(),
				"Incomplete honeycomb grid crafted a wax block");
		items.set(4, new ItemStack(Items.HONEY_BOTTLE));
		input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
		test.assertTrue(
				test.getLevel().getServer().getRecipeManager()
						.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel())
						.isEmpty(),
				"Honey bottle substituted for honeycomb");
		test.setBlock(CENTER, GardenBlocks.WAX_BLOCK);
		test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
		test.assertItemEntityPresent(GardenBlocks.WAX_ITEM, CENTER, 2);
		test.succeed();
	}

	@GameTest
	public void gardenWaxBlockCraftsBackIntoNineHoneycomb(GameTestHelper test) {
		var input = net.minecraft.world.item.crafting.CraftingInput.of(
				1, 1, java.util.List.of(new ItemStack(GardenBlocks.WAX_ITEM)));
		var recipe = test.getLevel().getServer().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(
				recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(Items.HONEYCOMB)
						&& recipe.orElseThrow().value().assemble(input).getCount() == 9,
				"One wax block did not craft exactly nine honeycomb");
		test.succeed();
	}

	@GameTest
	public void gardenPaeoniaBehavesLikeASmallFlower(GameTestHelper test) {
		var state = GardenBlocks.PAEONIA.defaultBlockState();
		test.setBlock(CENTER.below(), Blocks.GRASS_BLOCK);
		test.assertTrue(state.canSurvive(test.getLevel(), test.absolutePos(CENTER)), "Paeonia cannot grow on grass");
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.assertTrue(!state.canSurvive(test.getLevel(), test.absolutePos(CENTER)), "Paeonia can grow on stone");
		test.setBlock(CENTER.below(), Blocks.DIRT);
		test.setBlock(CENTER, GardenBlocks.PAEONIA);
		test.assertTrue(
				state.is(net.minecraft.tags.BlockTags.FLOWERS) && state.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)
						&& new ItemStack(GardenBlocks.PAEONIA_ITEM)
								.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
										net.minecraft.resources.Identifier.withDefaultNamespace("flowers")))
						&& GardenBlocks.PAEONIA_ITEM.components().has(DataComponents.COMPOSTABLE),
				"Paeonia is missing flower tags or composting");
		test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
		test.assertItemEntityPresent(GardenBlocks.PAEONIA_ITEM, CENTER, 2);
		var input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1,
				java.util.List.of(new ItemStack(GardenBlocks.PAEONIA_ITEM)));
		var recipe = test.getLevel().getServer().getRecipeManager()
				.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(
				recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(Items.DYE.magenta())
						&& recipe.orElseThrow().value().assemble(input).getCount() == 1,
				"Paeonia did not craft one magenta dye");
		test.setBlock(CENTER, Blocks.FLOWER_POT);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(GardenBlocks.PAEONIA_ITEM, 2));
		BlockPos pos = test.absolutePos(CENTER);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		test.getBlockState(CENTER).useItemOn(player.getMainHandItem(), test.getLevel(), player,
				InteractionHand.MAIN_HAND, hit);
		test.assertBlockPresent(GardenBlocks.POTTED_PAEONIA, CENTER);
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Potting did not consume exactly one flower");
		BlockPos compostPos = test.absolutePos(CENTER.offset(2, 0, 0));
		test.getLevel().setBlockAndUpdate(compostPos, Blocks.COMPOSTER.defaultBlockState());
		var compostStack = new ItemStack(GardenBlocks.PAEONIA_ITEM, 2);
		net.minecraft.world.level.block.ComposterBlock.insertItem(player, test.getLevel().getBlockState(compostPos),
				test.getLevel(), compostStack, compostPos);
		test.assertTrue(
				test.getLevel().getBlockState(compostPos)
						.getValue(net.minecraft.world.level.block.ComposterBlock.LEVEL) == 1
						&& compostStack.getCount() == 1,
				"Composting Paeonia did not consume one flower and start a compost layer");
		test.getLevel().destroyBlock(pos, true, null, 512);
		test.assertItemEntityPresent(Items.FLOWER_POT, CENTER, 2);
		test.assertItemEntityPresent(GardenBlocks.PAEONIA_ITEM, CENTER, 2);
		test.succeed();
	}

	@GameTest
	public void cactusFlowerAndTallFlowersCanBePotted(GameTestHelper test) {
		Block[] flowers = { Blocks.CACTUS_FLOWER, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LILAC, Blocks.SUNFLOWER,
				Blocks.PITCHER_PLANT };
		Block[] potted = { GardenBlocks.POTTED_CACTUS_FLOWER, GardenBlocks.POTTED_ROSE_BUSH,
				GardenBlocks.POTTED_PEONY, GardenBlocks.POTTED_LILAC, GardenBlocks.POTTED_SUNFLOWER,
				GardenBlocks.POTTED_PITCHER_PLANT };
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		BlockPos pos = test.absolutePos(CENTER);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		for (int i = 0; i < flowers.length; i++) {
			test.setBlock(CENTER, Blocks.FLOWER_POT);
			ItemStack stack = new ItemStack(flowers[i].asItem(), 2);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			test.getBlockState(CENTER).useItemOn(stack, test.getLevel(), player, InteractionHand.MAIN_HAND, hit);
			test.assertBlockPresent(potted[i], CENTER);
			test.assertTrue(player.getMainHandItem().getCount() == 1,
					"Potting " + flowers[i] + " did not consume exactly one flower");
		}
		test.succeed();
	}

	@GameTest
	public void gardenBlocksAppearInCreative(GameTestHelper test) {
		net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(test.getLevel().enabledFeatures(), true,
				test.getLevel().registryAccess());
		var building = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB
				.getValue(net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS);
		var natural = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB
				.getValue(net.minecraft.world.item.CreativeModeTabs.NATURAL_BLOCKS);
		test.assertTrue(building.getDisplayItems().stream().anyMatch(stack -> stack.is(GardenBlocks.WAX_ITEM)),
				"Wax block is missing from Building Blocks");
		test.assertTrue(natural.getDisplayItems().stream().anyMatch(stack -> stack.is(GardenBlocks.PAEONIA_ITEM)),
				"Paeonia is missing from Natural Blocks");
		test.succeed();
	}

	@GameTest
	public void lightingCarvedPumpkins(GameTestHelper test) {
		require(test, PatchworkConfig.settings().pumpkinLanterns(), "pumpkinLanterns");
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (var torch : new net.minecraft.world.item.Item[] { Items.TORCH, Items.SOUL_TORCH }) {
			test.setBlock(CENTER,
					Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.WEST));
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
		test.assertTrue(test.getEntities(SoulGolems.TYPE).getFirst().getOwnerReference().matches(player),
				"Lighting did not assign the builder as owner");
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Lighting did not consume one soul torch");
		test.assertBlockPresent(Blocks.AIR, CENTER);
		test.assertBlockPresent(Blocks.AIR, CENTER.above());
		test.assertBlockPresent(Blocks.AIR, CENTER.above(2));
		test.succeed();
	}

	@GameTest
	public void unlitTorchesRelightWithFlintAndSteel(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var lit = new net.minecraft.world.level.block.Block[] { Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.COPPER_TORCH,
				Blocks.REDSTONE_TORCH, Blocks.WALL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.COPPER_WALL_TORCH,
				Blocks.REDSTONE_WALL_TORCH };
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FLINT_AND_STEEL));
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER.west(), Blocks.STONE);
		for (int i = 0; i < lit.length; i++) {
			var state = lit[i].defaultBlockState();
			if (state.hasProperty(WallTorchBlock.FACING)) {
				state = state.setValue(WallTorchBlock.FACING, Direction.EAST);
			}
			test.setBlock(CENTER, UnlitTorches.extinguish(state));
			test.getBlockState(CENTER).useItemOn(player.getOffhandItem(), test.getLevel(), player,
					InteractionHand.OFF_HAND,
					new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)), Direction.NORTH,
							test.absolutePos(CENTER), false));
			test.assertTrue(test.getBlockState(CENTER).equals(state), "Flint relighting lost variant or wall facing");
			test.assertTrue(
					player.getOffhandItem().is(Items.FLINT_AND_STEEL)
							&& player.getOffhandItem().getDamageValue() == i + 1,
					"Relighting did not use one durability");
		}
		test.succeed();
	}

	@GameTest
	public void lightVariantsCampfireCookingRecipesAndDrops(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		ItemStack silk = new ItemStack(Items.DIAMOND_AXE);
		silk.enchant(test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
		for (var block : LightVariants.COPPER_CAMPFIRES.asList()) {
			test.setBlock(CENTER, Blocks.AIR);
			test.setBlock(CENTER, block);
			var entity = (net.minecraft.world.level.block.entity.CampfireBlockEntity)
					test.getLevel().getBlockEntity(test.absolutePos(CENTER));
			test.assertTrue(entity != null && entity.placeFood(test.getLevel(), player, new ItemStack(Items.BEEF)),
					"Copper campfire cannot cook food");
			var recipeCache = net.minecraft.world.item.crafting.RecipeManager
					.createCheck(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING);
			for (int tick = 0; tick < 600; tick++) {
				net.minecraft.world.level.block.entity.CampfireBlockEntity.cookTick(test.getLevel(),
						test.absolutePos(CENTER), test.getBlockState(CENTER), entity, recipeCache);
			}
			test.assertTrue(entity.getItems().stream().allMatch(ItemStack::isEmpty), "Campfire food did not finish cooking");
			test.assertItemEntityPresent(Items.COOKED_BEEF, CENTER, 2);
			for (boolean lit : java.util.List.of(false, true)) {
				var state = block.defaultBlockState().setValue(BlockStateProperties.LIT, lit);
				var drops = Block.getDrops(state, test.getLevel(), test.absolutePos(CENTER), entity, player, silk);
				var expected = lit ? block.asItem() : LightVariants.unlitItems().get(block);
				test.assertTrue(drops.size() == 1 && drops.getFirst().is(expected),
						"Silk Touch did not preserve copper campfire variant and lit state");
				var ordinary = Block.getDrops(state, test.getLevel(), test.absolutePos(CENTER), entity);
				test.assertTrue(ordinary.size() == 1 && ordinary.getFirst().is(Items.CHARCOAL)
						&& ordinary.getFirst().getCount() == 2, "Copper campfire changed ordinary charcoal drops");
			}
		}
		for (var soulBase : java.util.List.of(Items.SOUL_SAND, Items.SOUL_SOIL)) {
			assertLightRecipe(test, 1, 3,
					java.util.List.of(new ItemStack(Items.STRING), new ItemStack(Items.HONEYCOMB), new ItemStack(soulBase)),
					LightVariants.SOUL_CANDLE.asItem());
		}
		assertLightRecipe(test, 1, 3,
				java.util.List.of(new ItemStack(Items.STRING), new ItemStack(Items.HONEYCOMB),
						new ItemStack(Items.COPPER_NUGGET)), LightVariants.COPPER_CANDLES.weathering().unaffected().asItem());
		assertLightRecipe(test, 3, 3,
				java.util.List.of(ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY,
						new ItemStack(Items.STICK), new ItemStack(Items.COPPER_NUGGET), new ItemStack(Items.STICK),
						new ItemStack(Items.OAK_LOG), new ItemStack(Items.OAK_LOG), new ItemStack(Items.OAK_LOG)),
				LightVariants.COPPER_CAMPFIRES.weathering().unaffected().asItem());
		test.succeed();
	}

	@GameTest
	public void lightVariantsCopperWeatheringWaxingAndBrightness(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		test.setBlock(CENTER.below(), Blocks.HAY_BLOCK);
		for (var family : java.util.List.of(LightVariants.COPPER_CANDLES, LightVariants.COPPER_CANDLE_CAKES,
				LightVariants.COPPER_CAMPFIRES, LightVariants.COPPER_PUMPKINS)) {
			var ages = net.minecraft.world.level.block.WeatheringCopper.WeatherState.values();
			for (int stage = 0; stage < ages.length; stage++) {
				var normal = family.weathering().pick(ages[stage]);
				var waxed = family.waxed().pick(ages[stage]);
				for (var block : java.util.List.of(normal, waxed)) {
					for (var state : block.getStateDefinition().getPossibleStates()) {
						boolean lit = !state.hasProperty(BlockStateProperties.LIT)
								|| state.getValue(BlockStateProperties.LIT);
						test.assertTrue(state.getLightEmission() == (lit ? 14 - stage * 2 : 0),
								"Incorrect copper light for " + state);
					}
				}
				var state = normal.defaultBlockState().trySetValue(BlockStateProperties.LIT, true)
						.trySetValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 4)
						.trySetValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
						.trySetValue(BlockStateProperties.SIGNAL_FIRE, true);
				test.setBlock(CENTER, Blocks.AIR);
				test.setBlock(CENTER, state);
				var entity = test.getLevel().getBlockEntity(test.absolutePos(CENTER));
				if (entity instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity campfire) {
					test.assertTrue(campfire.placeFood(test.getLevel(), player, new ItemStack(Items.BEEF)),
							"Copper campfire rejected cookable food");
				}
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
				useTorchItem(test, player);
				test.assertTrue(test.getBlockState(CENTER).equals(waxed.withPropertiesOf(state)),
						"Waxing lost light variant properties");
				test.assertTrue(!test.getBlockState(CENTER).isRandomlyTicking(), "Waxed light still oxidizes");
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
				useTorchItem(test, player);
				test.assertTrue(test.getBlockState(CENTER).equals(state),
						"Unwaxing changed " + state + " to " + test.getBlockState(CENTER));
				if (stage > 0) {
					useTorchItem(test, player);
					test.assertTrue(test.getBlockState(CENTER).equals(family.weathering().pick(ages[stage - 1])
							.withPropertiesOf(state)), "Scraping did not restore one brighter stage");
					test.setBlock(CENTER, state);
				}
				if (stage < 3) {
					var random = net.minecraft.util.RandomSource.create(42);
					for (int tick = 0; tick < 10000 && test.getBlockState(CENTER).equals(state); tick++) {
						state.randomTick(test.getLevel(), test.absolutePos(CENTER), random);
					}
					test.assertTrue(test.getBlockState(CENTER).equals(family.weathering().pick(ages[stage + 1])
							.withPropertiesOf(state)), "Oxidation did not preserve properties and dim one stage");
				} else {
					test.assertTrue(!state.isRandomlyTicking(), "Fully oxidized light still ticks randomly");
				}
				if (entity instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity campfire) {
					test.assertTrue(test.getLevel().getBlockEntity(test.absolutePos(CENTER)) == entity
							&& campfire.getItems().getFirst().is(Items.BEEF),
							"Copper campfire lost its cooking inventory during oxidation/waxing/scraping");
				}
			}
		}
		test.assertTrue(LightVariants.SOUL_CANDLE.defaultBlockState().setValue(BlockStateProperties.LIT, true)
				.getLightEmission() == 10, "Soul candle has wrong brightness");
		test.succeed();
	}

	@GameTest
	public void lightVariantsUnlitItemsPlacementStackingAndIgnition(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.assertTrue(LightVariants.unlitItems().size() == 10, "Missing unlit campfire variants");
		for (var entry : LightVariants.unlitItems().entrySet()) {
			var block = entry.getKey();
			var item = entry.getValue();
			test.setBlock(CENTER, Blocks.AIR);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 4));
			var placement = new UseOnContext(player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER.below())).add(0, 0.5, 0),
							Direction.UP, test.absolutePos(CENTER.below()), false));
			test.assertTrue(item.useOn(placement).consumesAction(), "Unlit item failed placement: " + item);
			test.assertTrue(test.getBlockState(CENTER).is(block)
					&& !test.getBlockState(CENTER).getValue(BlockStateProperties.LIT),
					"Unlit item placed wrong block or a lit block: " + item);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
			useTorchItem(test, player);
			test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.LIT), "Light variant did not ignite");
			var unlit = test.getBlockState(CENTER).setValue(BlockStateProperties.LIT, false);
			test.setBlock(CENTER, unlit);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulFireCharges.ITEM));
			useTorchItem(test, player);
			test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.LIT), "Soul Fire Charge did not ignite variant");
			if (block instanceof net.minecraft.world.level.block.CampfireBlock) {
				test.setBlock(CENTER, block.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true)
						.setValue(BlockStateProperties.LIT, false));
				test.assertTrue(!net.minecraft.world.level.block.CampfireBlock.canLight(test.getBlockState(CENTER)),
						"Waterlogged copper campfire can ignite");
			}
			var input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(new ItemStack(block)));
			var recipe = test.getLevel().recipeAccess().getRecipeFor(
					net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
			test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(item),
					"Missing normal-to-unlit conversion recipe");
			var reverseInput = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(new ItemStack(item)));
			var reverse = test.getLevel().recipeAccess().getRecipeFor(
					net.minecraft.world.item.crafting.RecipeType.CRAFTING, reverseInput, test.getLevel());
			test.assertTrue(reverse.isPresent() && reverse.orElseThrow().value().assemble(reverseInput).is(block.asItem()),
					"Missing unlit-to-normal conversion recipe");
		}
		test.succeed();
	}

	@GameTest
	public void lightVariantsCopperPumpkinLightingAndRecipes(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (int index = 0; index < CopperTorches.LIT.asList().size(); index++) {
			var torch = CopperTorches.LIT.asList().get(index).asItem();
			var pumpkin = LightVariants.COPPER_PUMPKINS.asList().get(index);
			test.setBlock(CENTER, Blocks.CARVED_PUMPKIN.defaultBlockState()
					.setValue(net.minecraft.world.level.block.CarvedPumpkinBlock.FACING, Direction.EAST));
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(torch, 2));
			var hit = new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)), Direction.UP,
					test.absolutePos(CENTER), false);
			net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker()
					.interact(player, test.getLevel(), InteractionHand.MAIN_HAND, hit);
			test.assertTrue(test.getBlockState(CENTER).is(pumpkin)
					&& test.getBlockState(CENTER).getValue(net.minecraft.world.level.block.CarvedPumpkinBlock.FACING)
							== Direction.EAST && player.getMainHandItem().getCount() == 1,
					"Copper torch pumpkin lighting lost oxidation, wax, facing, or consumption");
			var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
					java.util.List.of(new ItemStack(Items.CARVED_PUMPKIN), new ItemStack(torch)));
			var recipe = test.getLevel().recipeAccess().getRecipeFor(
					net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
			test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(pumpkin.asItem()),
					"Copper pumpkin crafting lost oxidation or wax stage");
		}
		for (var family : java.util.List.of(LightVariants.COPPER_CANDLES, LightVariants.COPPER_CAMPFIRES,
				LightVariants.COPPER_PUMPKINS)) {
			family.zipUnwaxedWaxed((normal, waxed) -> {
				var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
						java.util.List.of(new ItemStack(normal), new ItemStack(Items.HONEYCOMB)));
				var recipe = test.getLevel().recipeAccess().getRecipeFor(
						net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
				test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(waxed.asItem()),
						"Missing light variant waxing recipe");
			});
		}
		test.succeed();
	}

	@GameTest
	public void unlitTorchCopperBrightnessDecreasesWithOxidation(GameTestHelper test) {
		for (var collection : java.util.List.of(CopperTorches.LIT, CopperTorches.LIT_WALL)) {
			var ages = net.minecraft.world.level.block.WeatheringCopper.WeatherState.values();
			for (int stage = 0; stage < ages.length; stage++) {
				int expected = 14 - stage * 2;
				for (var block : java.util.List.of(collection.weathering().pick(ages[stage]),
						collection.waxed().pick(ages[stage]))) {
					for (var state : block.getStateDefinition().getPossibleStates()) {
						test.assertTrue(state.getLightEmission() == expected,
								"Wrong copper torch brightness for stage " + stage + ": " + state);
						test.assertTrue(CopperTorches.extinguish(state).getLightEmission() == 0,
								"Extinguished copper torch still emits light");
					}
				}
			}
		}
		test.succeed();
	}

	@GameTest
	public void unlitTorchCopperWeatheringWaxingAndScraping(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		for (var block : java.util.List.of(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH,
				Blocks.SOUL_WALL_TORCH)) {
			test.assertTrue(!(block instanceof net.minecraft.world.level.block.WeatheringCopper),
					"Non-copper torch was made weatherable");
		}
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER.west(), Blocks.STONE);
		for (var collection : java.util.List.of(CopperTorches.LIT, CopperTorches.LIT_WALL, CopperTorches.UNLIT,
				CopperTorches.UNLIT_WALL)) {
			var normal = collection.weathering()
					.map(block -> block.defaultBlockState().hasProperty(WallTorchBlock.FACING)
							? block.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST)
							: block.defaultBlockState());
			var waxed = collection.waxed().map(block -> block.withPropertiesOf(normal.unaffected()));
			var normalStates = new net.minecraft.world.level.block.WeatheringCopperCollection<>(normal, waxed);
			normalStates.zipUnwaxedWaxed((state, waxedState) -> {
				test.setBlock(CENTER, state);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
				useTorchItem(test, player);
				test.assertTrue(
						test.getBlockState(CENTER).equals(waxedState) && player.getMainHandItem().getCount() == 1,
						"Waxing changed torch orientation/type or consumption");
				test.assertTrue(!waxedState.isRandomlyTicking(), "Waxed torch can weather");
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
				useTorchItem(test, player);
				test.assertTrue(
						test.getBlockState(CENTER).equals(state) && player.getMainHandItem().getDamageValue() == 1,
						"Axe did not strip wax while preserving state");
				var previous = net.minecraft.world.level.block.WeatheringCopper.getPrevious(state);
				if (previous.isPresent()) {
					useTorchItem(test, player);
					test.assertTrue(
							test.getBlockState(CENTER).equals(previous.orElseThrow())
									&& player.getMainHandItem().getDamageValue() == 2,
							"Axe did not remove exactly one oxidation stage");
				}
				test.setBlock(CENTER, state);
				var next = net.minecraft.world.level.block.WeatheringCopper.getNext(state.getBlock())
						.map(block -> block.withPropertiesOf(state));
				test.assertTrue(state.isRandomlyTicking() == next.isPresent(),
						"Torch random tick eligibility is incorrect: " + state + ", block type="
								+ state.getBlock().getClass());
				if (next.isPresent()) {
					var random = net.minecraft.util.RandomSource.create(42);
					for (int tick = 0; tick < 10000 && test.getBlockState(CENTER).equals(state); tick++) {
						state.randomTick(test.getLevel(), test.absolutePos(CENTER), random);
					}
					test.assertTrue(test.getBlockState(CENTER).equals(next.orElseThrow()),
							"Random weathering lost orientation/type or did not advance one stage");
				}
			});
		}
		test.succeed();
	}

	@GameTest
	public void unlitTorchCopperStagesRelightDropAndCraftWaxed(GameTestHelper test) {
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER.west(), Blocks.STONE);
		for (var collection : java.util.List.of(CopperTorches.LIT, CopperTorches.LIT_WALL)) {
			for (var lit : collection.asList()) {
				var state = lit.defaultBlockState();
				if (state.hasProperty(WallTorchBlock.FACING)) {
					state = state.setValue(WallTorchBlock.FACING, Direction.EAST);
				}
				var unlit = UnlitTorches.extinguish(state);
				test.assertTrue(unlit != null && unlit.getLightEmission() == 0, "Copper stage did not extinguish");
				test.setBlock(CENTER, unlit);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
				test.getBlockState(CENTER).useItemOn(player.getMainHandItem(), test.getLevel(), player,
						InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)),
								Direction.UP, test.absolutePos(CENTER), false));
				test.assertTrue(test.getBlockState(CENTER).equals(state),
						"Relighting lost copper stage, wax, or facing");
				test.setBlock(CENTER, unlit);
				test.getLevel().destroyBlock(test.absolutePos(CENTER), true, null, 512);
				test.assertItemEntityPresent(unlit.getBlock().asItem(), CENTER, 2);
			}
		}
		for (var collection : java.util.List.of(CopperTorches.LIT, CopperTorches.UNLIT)) {
			collection.zipUnwaxedWaxed((normal, waxed) -> {
				var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1,
						java.util.List.of(new ItemStack(normal), new ItemStack(Items.HONEYCOMB)));
				var recipe = test.getLevel().recipeAccess()
						.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
				test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(waxed.asItem()),
						"Waxing recipe lost copper torch stage or lit state");
			});
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
			for (boolean hanging : new boolean[] { false, true }) {
				var state = lit.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING,
						hanging);
				var unlit = UnlitLanterns.extinguish(state);
				test.assertTrue(
						unlit != null && unlit.getLightEmission() == 0
								&& unlit.getValue(net.minecraft.world.level.block.LanternBlock.HANGING) == hanging,
						"Lantern did not extinguish while preserving hanging state");
				test.setBlock(CENTER, unlit);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE, 2));
				test.useBlock(CENTER, player);
				test.assertTrue(test.getBlockState(CENTER).equals(state),
						"Relighting did not restore the exact lantern variant");
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
		var wet = Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.WATERLOGGED,
				true);
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
		copper.weathering().forEach(block -> {
			var state = block.defaultBlockState();
			var next = net.minecraft.world.level.block.WeatheringCopper.getNext(block);
			test.assertTrue(state.isRandomlyTicking() == next.isPresent(),
					"Unlit copper lantern tick eligibility is incorrect");
			if (next.isPresent()) {
				test.setBlock(CENTER, state);
				var random = net.minecraft.util.RandomSource.create(42);
				for (int tick = 0; tick < 10000 && test.getBlockState(CENTER).equals(state); tick++) {
					state.randomTick(test.getLevel(), test.absolutePos(CENTER), random);
				}
				test.assertTrue(test.getBlockState(CENTER).is(next.orElseThrow()),
						"Unlit copper lantern did not weather");
			}
		});
		copper.zipUnwaxedWaxed((normal, waxed) -> {
			test.assertTrue(
					net.minecraft.world.item.HoneycombItem.getWaxed(normal.defaultBlockState()).orElseThrow().is(waxed),
					"Unlit copper cannot be waxed");
			test.assertTrue(net.minecraft.world.item.HoneycombItem.WAX_OFF_BY_BLOCK.get().get(waxed) == normal,
					"Unlit copper cannot be unwaxed");
			test.assertTrue(!waxed.defaultBlockState().isRandomlyTicking(), "Waxed unlit lantern can oxidize");
		});
		test.succeed();
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
		test.assertTrue(
				UnlitTorches.extinguish(Blocks.REDSTONE_TORCH.defaultBlockState()).is(UnlitTorches.REDSTONE_TORCH),
				"Lit redstone torch did not extinguish");
		test.assertTrue(
				UnlitTorches
						.extinguish(Blocks.REDSTONE_TORCH.defaultBlockState().setValue(RedstoneTorchBlock.LIT,
								false)) == null,
				"Already-off redstone torch was changed");
		test.assertTrue(UnlitTorches
				.extinguish(Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST))
				.getValue(WallTorchBlock.FACING) == Direction.EAST, "Wall facing was lost");
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
		net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) test
				.makeMockServerPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE));
		test.useBlock(CENTER, player);
		test.assertBlockPresent(Blocks.TORCH, CENTER);
		test.assertTrue(player.getMainHandItem().isEmpty(), "Relighting did not consume one fire charge");
		var advancement = test.getLevel().getServer().getAdvancements()
				.get(Patchwork.id("adventure/let_there_be_light"));
		test.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
				"Relighting with a fire charge did not award Let There Be Light");
		test.succeed();
	}

	@GameTest
	public void unlitLanternRelightsWithFireCharge(GameTestHelper test) {
		test.setBlock(CENTER, UnlitLanterns.LANTERN.defaultBlockState());
		net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) test
				.makeMockServerPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE));
		test.useBlock(CENTER, player);
		test.assertBlockPresent(Blocks.LANTERN, CENTER);
		var advancement = test.getLevel().getServer().getAdvancements()
				.get(Patchwork.id("adventure/let_there_be_light"));
		test.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
				"Relighting a lantern with a fire charge did not award Let There Be Light");
		test.succeed();
	}

	@GameTest(maxTicks = 10)
	public void charcoalBlockBurnsFor16000Ticks(GameTestHelper test) {
		var coalFuel = Items.COAL_BLOCK.getDefaultInstance().get(DataComponents.COOKING_FUEL);
		var charcoalFuel = CharcoalBlocks.ITEM.getDefaultInstance().get(DataComponents.COOKING_FUEL);
		test.assertTrue(coalFuel != null && coalFuel.equals(charcoalFuel),
				"Charcoal block does not have the coal block's furnace fuel value");
		test.setBlock(CENTER, Blocks.FURNACE);
		var furnace = (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity) test.getLevel()
				.getBlockEntity(test.absolutePos(CENTER));
		furnace.setItem(0, new ItemStack(Items.COBBLESTONE));
		furnace.setItem(1, new ItemStack(CharcoalBlocks.ITEM));
		test.runAfterDelay(2, () -> {
			int duration = furnace.saveWithoutMetadata(test.getLevel().registryAccess()).getInt("lit_total_time")
					.orElse(0);
			test.assertTrue(duration == 16000, "Expected 16000 charcoal fuel ticks, got " + duration);
			test.assertTrue(furnace.getItem(1).isEmpty(), "Furnace did not consume the charcoal block");
			test.succeed();
		});
	}

	@GameTest
	public void goldDoorLocksWhilePowered(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, GoldDoors.DOOR.defaultBlockState());
		test.setBlock(CENTER.above(), GoldDoors.DOOR.defaultBlockState()
				.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
						net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		Player creative = test.makeMockPlayer(GameType.CREATIVE);
		creative.getAbilities().instabuild = true;
		BlockPos pos = test.absolutePos(CENTER);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN)
				&& test.getBlockState(CENTER.above()).getValue(BlockStateProperties.OPEN),
				"Gold door did not open by hand while unpowered");
		test.getLevel().setBlockAndUpdate(test.absolutePos(CENTER.offset(1, 0, 0)),
				Blocks.REDSTONE_BLOCK.defaultBlockState());
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.POWERED)
				&& test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"Redstone power did not lock the gold door without changing its open state");
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"A survival player closed a powered gold door");
		((net.minecraft.world.level.block.DoorBlock) GoldDoors.DOOR).setOpen(player, test.getLevel(),
				test.getBlockState(CENTER), pos, false);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"A powered gold door was forced closed through setOpen");
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), creative, hit);
		test.assertTrue(!test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"A creative player could not bypass a powered gold door");
		test.getLevel().setBlockAndUpdate(test.absolutePos(CENTER.offset(1, 0, 0)), Blocks.AIR.defaultBlockState());
		test.assertTrue(!test.getBlockState(CENTER).getValue(BlockStateProperties.POWERED),
				"Gold door stayed powered after its redstone source was removed");
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"Gold door stayed locked after losing power");
		test.succeed();
	}

	@GameTest
	public void goldTrapdoorLocksWhilePowered(GameTestHelper test) {
		test.setBlock(CENTER, GoldDoors.TRAPDOOR.defaultBlockState());
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		Player creative = test.makeMockPlayer(GameType.CREATIVE);
		creative.getAbilities().instabuild = true;
		BlockPos pos = test.absolutePos(CENTER);
		var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"Gold trapdoor did not open by hand while unpowered");
		test.getLevel().setBlockAndUpdate(test.absolutePos(CENTER.offset(1, 0, 0)),
				Blocks.REDSTONE_BLOCK.defaultBlockState());
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.POWERED)
				&& test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"Redstone power did not lock the gold trapdoor without changing its open state");
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"A survival player closed a powered gold trapdoor");
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), creative, hit);
		test.assertTrue(!test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"A creative player could not bypass a powered gold trapdoor");
		test.getLevel().setBlockAndUpdate(test.absolutePos(CENTER.offset(1, 0, 0)), Blocks.AIR.defaultBlockState());
		test.getBlockState(CENTER).useWithoutItem(test.getLevel(), player, hit);
		test.assertTrue(test.getBlockState(CENTER).getValue(BlockStateProperties.OPEN),
				"Gold trapdoor stayed locked after losing power");
		test.succeed();
	}

	@GameTest
	public void goldDoorsDropAndNameThemselves(GameTestHelper test) {
		for (var block : java.util.List.of(GoldDoors.DOOR, GoldDoors.TRAPDOOR)) {
			var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
			var expected = net.minecraft.resources.ResourceKey.create(
					net.minecraft.core.registries.Registries.LOOT_TABLE, Patchwork.id("blocks/" + id.getPath()));
			test.assertTrue(block.getLootTable().equals(java.util.Optional.of(expected)),
					"Gold door block does not drop itself: " + id);
			test.assertTrue(block.getDescriptionId().equals(id.toLanguageKey("block")),
					"Gold door block uses an unexpected description: " + id);
		}
		test.succeed();
	}

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}

	private static void assertLightRecipe(GameTestHelper test, int width, int height, java.util.List<ItemStack> stacks,
			net.minecraft.world.item.Item expected) {
		var input = net.minecraft.world.item.crafting.CraftingInput.of(width, height, stacks);
		var recipe = test.getLevel().recipeAccess().getRecipeFor(
				net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(expected),
				"Missing survival recipe for " + expected);
	}

	private static void useTorchItem(GameTestHelper test, Player player) {
		BlockPos pos = test.absolutePos(CENTER);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
	}
}
