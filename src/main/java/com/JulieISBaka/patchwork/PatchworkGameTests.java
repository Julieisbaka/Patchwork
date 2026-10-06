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
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

public class PatchworkGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}

	@GameTest
	public void configurationAutomaticallyAddsMissingDefaults(GameTestHelper test) throws Exception {
		var directory = java.nio.file.Files.createTempDirectory("patchwork-config-test-");
		var path = directory.resolve("patchwork.properties");
		try {
			var defaults = PatchworkConfig.read(path);
			var baseline = new java.util.Properties();
			try (var reader = java.nio.file.Files.newBufferedReader(path)) {
				baseline.load(reader);
			}
			for (var component : PatchworkConfig.Settings.class.getRecordComponents()) {
				Object expected = component.getType() == boolean.class
						? !component.getName().equals("creeperChainReactions") : 32;
				test.assertTrue(component.getAccessor().invoke(defaults).equals(expected),
						"Wrong default for " + component.getName());
				test.assertTrue(baseline.getProperty(component.getName()).equals(expected.toString()),
						"Default was not saved for " + component.getName());
			}
			baseline.setProperty("witherDifficultyHealth", "false");
			baseline.setProperty("creeperChainReactions", "true");
			baseline.setProperty("callHornRecallRadius", "64");
			baseline.setProperty("customOption", "keep-me");
			for (var component : PatchworkConfig.Settings.class.getRecordComponents()) {
				var incomplete = new java.util.Properties();
				incomplete.putAll(baseline);
				incomplete.remove(component.getName());
				try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
					incomplete.store(writer, "Configuration migration regression");
				}
				var loaded = PatchworkConfig.read(path);
				var persisted = new java.util.Properties();
				try (var reader = java.nio.file.Files.newBufferedReader(path)) {
					persisted.load(reader);
				}
				Object expected = component.getAccessor().invoke(defaults);
				test.assertTrue(component.getAccessor().invoke(loaded).equals(expected)
						&& persisted.getProperty(component.getName()).equals(expected.toString()),
						"Missing setting was not defaulted and saved: " + component.getName());
				for (String key : incomplete.stringPropertyNames()) {
					test.assertTrue(incomplete.getProperty(key).equals(persisted.getProperty(key)),
							"Migration changed an existing property: " + key);
				}
				var before = java.nio.file.Files.readAllBytes(path);
				var modified = java.nio.file.Files.getLastModifiedTime(path);
				PatchworkConfig.read(path);
				test.assertTrue(java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(path))
						&& modified.equals(java.nio.file.Files.getLastModifiedTime(path)),
						"Reading a complete config rewrote it");
			}
			java.nio.file.Files.writeString(path, "");
			test.assertTrue(PatchworkConfig.read(path).equals(defaults), "Empty existing config did not gain defaults");
			for (var component : PatchworkConfig.Settings.class.getRecordComponents()) {
				var invalidValues = component.getType() == boolean.class
						? java.util.List.of("yes", "") : java.util.List.of("15", "257", "32.5", "not-a-number", "");
				for (String invalid : invalidValues) {
					var properties = new java.util.Properties();
					properties.putAll(baseline);
					properties.remove("wolfBanners");
					properties.setProperty(component.getName(), invalid);
					try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
						properties.store(writer, "Invalid config must not be rewritten");
					}
					var before = java.nio.file.Files.readAllBytes(path);
					boolean rejected = false;
					try {
						PatchworkConfig.read(path);
					} catch (IllegalArgumentException exception) {
						rejected = exception.getMessage().contains(component.getName());
					}
					test.assertTrue(rejected && java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(path)),
							"Invalid setting was accepted or changed on disk: " + component.getName());
				}
			}
			test.succeed();
		} finally {
			java.nio.file.Files.deleteIfExists(path);
			java.nio.file.Files.delete(directory);
		}
	}

	@GameTest
	public void rabbitCarrotTamingAndOwnerCommands(GameTestHelper test) {
		var rabbit = test.spawn(EntityTypes.RABBIT, CENTER);
		var pet = (RabbitPet) rabbit;
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_CARROT, 2));
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(pet.getOwnerReference() == null, "Golden carrots unexpectedly tame rabbits");
		rabbit.setAge(0);
		rabbit.resetLove();
		owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.CARROT, 3));
		test.assertTrue(rabbit.interact(owner, InteractionHand.OFF_HAND, Vec3.ZERO).consumesAction(),
				"Offhand carrot taming did not succeed");
		test.assertTrue(pet.patchwork$isOwnedBy(owner) && owner.getOffhandItem().getCount() == 2
				&& !rabbit.isInLove() && rabbit.isPersistenceRequired() && !pet.patchwork$isOrderedToStay(),
				"One carrot did not tame a persistent, following rabbit without starting breeding");
		Player stranger = test.makeMockServerPlayer(GameType.SURVIVAL);
		stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 2));
		rabbit.interact(stranger, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(pet.patchwork$isOwnedBy(owner) && rabbit.isInLove(),
				"Feeding a tamed rabbit stole ownership or broke breeding");
		stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		rabbit.interact(stranger, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(!pet.patchwork$isOrderedToStay(), "A stranger ordered the rabbit to stay");
		owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(pet.patchwork$isOrderedToStay(), "Owner's empty hand did not enable staying");
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(!pet.patchwork$isOrderedToStay(), "Owner's empty hand did not resume following");
		var baby = test.spawn(EntityTypes.RABBIT, CENTER.offset(1, 0, 0));
		baby.setBaby(true);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 2));
		baby.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(((RabbitPet) baby).patchwork$isOwnedBy(owner) && baby.isBaby()
				&& owner.getMainHandItem().getCount() == 1, "Baby carrot taming changed age or consumption");
		var creativeRabbit = test.spawn(EntityTypes.RABBIT, CENTER.offset(2, 0, 0));
		Player creative = test.makeMockPlayer(GameType.CREATIVE);
		GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
		creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT, 2));
		creativeRabbit.interact(creative, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(((RabbitPet) creativeRabbit).patchwork$isOwnedBy(creative)
				&& creative.getMainHandItem().getCount() == 2, "Creative taming consumed a carrot");
		test.succeed();
	}

	@GameTest
	public void rabbitOwnershipAndStayPersist(GameTestHelper test) {
		var rabbit = test.spawn(EntityTypes.RABBIT, CENTER);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		var pet = (RabbitPet) rabbit;
		pet.patchwork$setOrderedToStay(true);
		var problems = new net.minecraft.util.ProblemReporter.Collector();
		var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems,
				test.getLevel().registryAccess());
		test.assertTrue(rabbit.save(output), "Tamed rabbit could not be saved");
		var reloaded = new net.minecraft.world.entity.animal.rabbit.Rabbit(EntityTypes.RABBIT, test.getLevel());
		reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(problems, test.getLevel().registryAccess(),
				output.buildResult()));
		test.assertTrue(((RabbitPet) reloaded).patchwork$isOwnedBy(owner)
				&& ((RabbitPet) reloaded).patchwork$isOrderedToStay() && reloaded.isPersistenceRequired()
				&& reloaded.getVariant() == rabbit.getVariant() && problems.isEmpty(),
				"Rabbit owner, stay command, persistence, or variant failed save/reload");
		var wild = test.spawn(EntityTypes.RABBIT, CENTER.offset(1, 0, 0));
		var wildOutput = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems,
				test.getLevel().registryAccess());
		test.assertTrue(wild.save(wildOutput), "Wild rabbit could not be saved");
		var legacy = wildOutput.buildResult();
		legacy.remove("PatchworkOwner");
		legacy.remove("PatchworkStay");
		reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(problems, test.getLevel().registryAccess(),
				legacy));
		test.assertTrue(((RabbitPet) reloaded).getOwnerReference() == null
				&& !((RabbitPet) reloaded).patchwork$isOrderedToStay(), "Old wild rabbit data gained pet state");
		test.succeed();
	}

	@GameTest(maxTicks = 200)
	public void rabbitStaysThenFollowsOwner(GameTestHelper test) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 4; z++) {
				test.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		var rabbit = test.spawn(EntityTypes.RABBIT, new BlockPos(1, 1, 1));
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.snapTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(6, 1, 1))));
		test.getLevel().addFreshEntity(owner);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		var pet = (RabbitPet) rabbit;
		pet.patchwork$setOrderedToStay(true);
		rabbit.setOnGround(true);
		rabbit.startJumping();
		test.assertTrue(!rabbit.isJumping(), "Staying rabbit started a jump");
		Vec3 origin = rabbit.position();
		test.runAfterDelay(30, () -> {
			test.assertTrue(rabbit.position().distanceToSqr(origin) < 0.25,
					"Staying rabbit wandered away");
			pet.patchwork$setOrderedToStay(false);
			var follow = new RabbitPets.FollowGoal(rabbit, pet);
			test.assertTrue(follow.canUse(), "Rabbit cannot follow its nearby owner");
			test.succeedWhen(() -> test.assertTrue(rabbit.distanceToSqr(owner) < 16,
					"Rabbit did not move toward its owner"));
		});
	}

	@GameTest
	public void rabbitAvoidanceAndSafeTeleport(GameTestHelper test) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				test.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		var rabbit = test.spawn(EntityTypes.RABBIT, new BlockPos(1, 1, 1));
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.snapTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(4, 1, 4))));
		test.getLevel().addFreshEntity(owner);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		var avoid = new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(rabbit, Player.class, 8, 2.2, 2.2);
		test.assertTrue(!avoid.canUse(), "Tamed rabbit tries to flee from its owner");
		rabbit.getRandom().setSeed(0);
		test.assertTrue(RabbitPets.teleportToOwner(rabbit, owner) && rabbit.distanceToSqr(owner) < 25
				&& test.getLevel().noCollision(rabbit), "Rabbit did not teleport to a safe owner-adjacent block");
		Vec3 origin = rabbit.position();
		rabbit.setLeashedTo(owner, false);
		test.assertTrue(!RabbitPets.teleportToOwner(rabbit, owner) && rabbit.position().equals(origin),
				"Leashed rabbit teleported");
		rabbit.removeLeash();
		for (int x = 1; x <= 7; x++) {
			for (int z = 1; z <= 7; z++) {
				for (int y = 18; y <= 22; y++) {
					test.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
		owner.snapTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(4, 20, 4))));
		test.assertTrue(!RabbitPets.teleportToOwner(rabbit, owner) && rabbit.position().equals(origin),
				"Rabbit teleported into unsupported air");
		test.succeed();
	}

	@GameTest
	public void rabbitPetRecallAndSweepProtection(GameTestHelper test) {
		require(test, PatchworkConfig.settings().callHornRecall(), "callHornRecall");
		require(test, PatchworkConfig.settings().ownerSweepProtection(), "ownerSweepProtection");
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				test.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
		var rabbit = test.spawn(EntityTypes.RABBIT, new BlockPos(1, 1, 1));
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.snapTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(4, 1, 4))));
		test.getLevel().addFreshEntity(owner);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
		rabbit.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		var pet = (RabbitPet) rabbit;
		pet.patchwork$setOrderedToStay(true);
		var horn = new ItemStack(Items.GOAT_HORN);
		horn.set(DataComponents.INSTRUMENT, new net.minecraft.world.item.component.InstrumentComponent(
				test.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.INSTRUMENT)
						.getOrThrow(net.minecraft.world.item.Instruments.CALL_GOAT_HORN)));
		owner.setItemInHand(InteractionHand.MAIN_HAND, horn);
		rabbit.getRandom().setSeed(0);
		test.assertTrue(horn.use(test.getLevel(), owner, InteractionHand.MAIN_HAND).consumesAction()
				&& !pet.patchwork$isOrderedToStay() && rabbit.distanceToSqr(owner) < 25,
				"Call horn did not recall and release a staying rabbit");
		var target = test.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 1, 5));
		rabbit.snapTo(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(5, 1, 5))));
		var wild = test.spawn(EntityTypes.RABBIT, new BlockPos(3, 1, 5));
		float rabbitHealth = rabbit.getHealth();
		((com.JulieISBaka.patchwork.mixin.PlayerSweepAccessor) owner).patchwork$doSweepAttack(
				target, 4, test.getLevel().damageSources().playerAttack(owner), 1);
		test.assertTrue(rabbit.getHealth() == rabbitHealth, "Owner's sweep attack hurt their rabbit");
		test.assertTrue(wild.getHealth() < wild.getMaxHealth(), "Sweep protection affected wild rabbits");
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
	public void restoredIllusionersJoinLateRaidWaves(GameTestHelper test) {
		for (var difficulty : java.util.List.of(net.minecraft.world.Difficulty.EASY,
				net.minecraft.world.Difficulty.NORMAL, net.minecraft.world.Difficulty.HARD)) {
			var raid = new net.minecraft.world.entity.raid.Raid(test.absolutePos(CENTER), difficulty);
			raid.setRaidOmenLevel(2);
			for (int wave = 1; wave <= raid.getNumGroups(difficulty) + 1; wave++) {
				((com.JulieISBaka.patchwork.mixin.RaidAccessor) (Object) raid)
						.patchwork$spawnGroup(test.getLevel(), test.absolutePos(CENTER));
				var raiders = raid.getAllRaiders();
				var illusioners = raiders.stream().filter(raider -> raider.getType() == EntityTypes.ILLUSIONER)
						.toList();
				test.assertTrue(illusioners.size() == (wave >= 5 ? 1 : 0),
						"Wrong illusioner count for " + difficulty + " raid wave " + wave);
				test.assertTrue(raiders.stream().anyMatch(raider -> raider.getType() == EntityTypes.PILLAGER),
						"Illusioner spawning replaced vanilla pillagers");
				test.assertTrue(raid.getTotalHealth() == raid.getHealthOfLivingRaiders(),
						"Raid health does not include all spawned raiders");
				for (var illusioner : illusioners) {
					test.assertTrue(illusioner.getCurrentRaid() == raid && illusioner.getWave() == wave
							&& illusioner.canJoinRaid() && illusioner.isAlive()
							&& test.getLevel().getEntity(illusioner.getUUID()) == illusioner,
							"Illusioner was not spawned and registered as a live wave member");
					test.assertTrue(illusioner.getMainHandItem().is(Items.BOW),
							"Illusioner raid spawn did not receive its bow");
				}
				for (var raider : raiders) {
					raid.removeFromRaid(test.getLevel(), raider, true);
					raider.discard();
				}
				test.assertTrue(raid.getTotalRaidersAlive() == 0, "Removed raiders still block wave completion");
			}
		}
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
	public void soulJackOLanternCannotCreateSnowGolem(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		test.setBlock(CENTER, Blocks.SNOW_BLOCK);
		test.setBlock(CENTER.above(), Blocks.SNOW_BLOCK);
		test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
		test.assertTrue(test.getEntities(EntityTypes.SNOW_GOLEM).isEmpty(), "Soul lantern created a Snow Golem");
		test.assertBlockPresent(Blocks.SNOW_BLOCK, CENTER);
		test.assertBlockPresent(Blocks.SNOW_BLOCK, CENTER.above());
		test.assertBlockPresent(PumpkinLanterns.SOUL_BLOCK, CENTER.above(2));
		for (var pumpkin : new net.minecraft.world.level.block.Block[] { Blocks.CARVED_PUMPKIN,
				Blocks.JACK_O_LANTERN }) {
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
		test.assertTrue(
				golem.getHurtSound(golem.damageSources().generic()) != net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT
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
		test.assertTrue(golem.getMaxHealth() == 50.0F && !golem.canAttack(player) && golem.isPlayerCreated()
				&& golem.isPersistenceRequired(), "Egg-created Soul Golem did not keep balanced, friendly behavior");
		test.assertTrue(golem.getOwnerReference() != null && golem.getOwnerReference().matches(player),
				"Spawn egg did not assign its player as owner");
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Spawn egg did not consume one egg");
		test.succeed();
	}

	@GameTest
	public void soulGolemAcceptsSoulSandSoilAndMixedStacks(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.STONE);
		for (var bottom : new net.minecraft.world.level.block.Block[] { Blocks.SOUL_SAND, Blocks.SOUL_SOIL }) {
			for (var middle : new net.minecraft.world.level.block.Block[] { Blocks.SOUL_SAND, Blocks.SOUL_SOIL }) {
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
		test.assertTrue(test.getEntities(SoulGolems.TYPE).getFirst().getOwnerReference().matches(player),
				"Lighting did not assign the builder as owner");
		test.assertTrue(player.getMainHandItem().getCount() == 1, "Lighting did not consume one soul torch");
		test.assertBlockPresent(Blocks.AIR, CENTER);
		test.assertBlockPresent(Blocks.AIR, CENTER.above());
		test.assertBlockPresent(Blocks.AIR, CENTER.above(2));
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void soulGolemIsFriendlyAndShootsSoulCharges(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		golem.setNoAi(true);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		var creeper = test.spawn(EntityTypes.CREEPER, CENTER.offset(2, 0, 0));
		creeper.setNoAi(true);
		test.assertTrue(!golem.canAttack(player), "Player-created Soul Golem can attack a player");
		test.assertTrue(!golem.canAttack(creeper), "Soul Golem can target a Creeper");
		var target = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 3));
		target.setNoAi(true);
		float health = target.getHealth();
		test.assertTrue(!golem.doHurtTarget(test.getLevel(), target) && target.getHealth() == health,
				"Soul Golem still deals melee damage");
		golem.performRangedAttack(target, 1.0F);
		var projectiles = test.getEntities(SoulFireCharges.PROJECTILE);
		test.assertTrue(projectiles.size() == 1 && projectiles.getFirst().getOwner() == golem,
				"Soul Golem did not launch an attributed Soul Fire Charge");
		test.runAfterDelay(10, () -> {
			test.assertTrue(target.getHealth() == health - 3.0F,
					"Soul Golem projectile did not deal exactly 3 HP on impact");
			test.assertTrue(target.getRemainingFireTicks() > 0 && target.getRemainingFireTicks() <= 40,
					"Soul Golem projectile did not apply two-second fire");
			test.succeed();
		});
	}

	@GameTest
	public void soulGolemOwnershipAndShearingPersist(GameTestHelper test) {
		var soul = test.spawn(SoulGolems.TYPE, CENTER);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT, 2));
		soul.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(soul.getOwnerReference().matches(owner) && owner.getMainHandItem().getCount() == 1,
				"One gold ingot did not claim the full-health ownerless golem");
		Player stranger = test.makeMockPlayer(GameType.SURVIVAL);
		stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
		soul.setHealth(25);
		soul.interact(stranger, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(soul.getOwnerReference().matches(owner), "Repair stole ownership");
		owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHEARS));
		soul.interact(owner, InteractionHand.OFF_HAND, Vec3.ZERO);
		test.assertTrue(!soul.hasLantern() && owner.getOffhandItem().getDamageValue() == 1,
				"Shearing did not remove the lantern and use one shears durability");
		test.assertItemEntityPresent(PumpkinLanterns.SOUL_ITEM, CENTER, 2);
		soul.interact(owner, InteractionHand.OFF_HAND, Vec3.ZERO);
		test.assertTrue(owner.getOffhandItem().getDamageValue() == 1, "Shearing twice duplicated the lantern");
		var problems = new net.minecraft.util.ProblemReporter.Collector();
		var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(problems,
				test.getLevel().registryAccess());
		test.assertTrue(soul.save(output), "Soul Golem could not be saved");
		var reloaded = new SoulGolem(SoulGolems.TYPE, test.getLevel());
		reloaded.load(net.minecraft.world.level.storage.TagValueInput.create(problems, test.getLevel().registryAccess(),
				output.buildResult()));
		test.assertTrue(reloaded.getOwnerReference().matches(owner) && !reloaded.hasLantern()
				&& reloaded.getHealth() == 50 && problems.isEmpty(), "Owner, shearing, or health failed save/reload");
		test.assertTrue(!reloaded.canAttack(owner), "Reloaded golem can attack its owner");
		test.succeed();
	}

	@GameTest
	public void soulGolemPlacedHeadAssignsBuilder(GameTestHelper test) {
		test.setBlock(CENTER, Blocks.SOUL_SAND);
		test.setBlock(CENTER.above(), Blocks.SOUL_SOIL);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PumpkinLanterns.SOUL_ITEM));
		BlockPos support = test.absolutePos(CENTER.above());
		PumpkinLanterns.SOUL_ITEM.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false)));
		test.assertTrue(
				test.getEntities(SoulGolems.TYPE).size() == 1
						&& test.getEntities(SoulGolems.TYPE).getFirst().getOwnerReference().matches(player),
				"Placing the Soul Lantern did not assign the builder");
		test.succeed();
	}

	@GameTest(maxTicks = 30)
	public void soulGolemDefendsAndAssistsOwner(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		golem.setOwner(owner);
		var victim = test.spawn(EntityTypes.COW, CENTER.offset(1, 0, 0));
		victim.setNoAi(true);
		Player attacker = test.makeMockPlayer(GameType.SURVIVAL);
		attacker.snapTo(Vec3.atBottomCenterOf(test.absolutePos(CENTER.offset(0, 0, 2))));
		test.getLevel().addFreshEntity(attacker);
		owner.setLastHurtByMob(attacker);
		test.assertTrue(golem.canAttack(attacker) && !golem.canAttack(owner),
				"Golem cannot defend against a player or can target its owner");
		test.runAfterDelay(5, () -> {
			test.assertTrue(golem.getTarget() == attacker, "Golem did not target its owner's attacker");
			attacker.discard();
			owner.setLastHurtByMob(null);
			owner.setLastHurtMob(victim);
			golem.setTarget(null);
		});
		test.runAfterDelay(15, () -> {
			test.assertTrue(golem.getTarget() == victim, "Golem did not assist against its owner's victim");
			var pet = test.spawn(EntityTypes.WOLF, CENTER);
			pet.tame(owner);
			test.assertTrue(!golem.canAttack(pet), "Golem can attack its owner's pet");
			test.succeed();
		});
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

	@GameTest(maxTicks = 80)
	public void soulGolemRangedGoalFiresWithoutMelee(GameTestHelper test) {
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		golem.setOwner(owner);
		var victim = test.spawn(EntityTypes.COW, CENTER.offset(0, 0, 3));
		victim.setNoAi(true);
		owner.setLastHurtMob(victim);
		test.succeedWhen(() -> test.assertTrue(victim.getHealth() < victim.getMaxHealth(),
				"Soul Golem ranged goal did not shoot its owner's target"));
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

	@GameTest(maxTicks = 20)
	public void soulGolemFireRespectsMobGriefing(GameTestHelper test) {
		boolean original = test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
		test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, test.getLevel().getServer());
		BlockPos wall = CENTER.offset(1, 1, 0);
		test.setBlock(wall, Blocks.SOUL_SOIL);
		SoulGolem golem = test.spawn(SoulGolems.TYPE, CENTER);
		golem.setNoAi(true);
		SoulFireCharges.shoot(test.getLevel(), golem, Vec3.atCenterOf(test.absolutePos(CENTER)).add(0, 1, 0),
				new Vec3(1, 0, 0));
		test.runAfterDelay(6, () -> {
			test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, test.getLevel().getServer());
			test.assertBlockPresent(Blocks.AIR, wall.above());
			test.assertBlockPresent(Blocks.SOUL_SOIL, wall);
			test.succeed();
		});
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

	private static void assertLightRecipe(GameTestHelper test, int width, int height, java.util.List<ItemStack> stacks,
			net.minecraft.world.item.Item expected) {
		var input = net.minecraft.world.item.crafting.CraftingInput.of(width, height, stacks);
		var recipe = test.getLevel().recipeAccess().getRecipeFor(
				net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, test.getLevel());
		test.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(expected),
				"Missing survival recipe for " + expected);
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
		test.assertTrue(LightVariants.unlitItems().size() == 36, "Missing unlit candle or campfire variants");
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
			if (block instanceof net.minecraft.world.level.block.CandleBlock) {
				var hit = new BlockHitResult(Vec3.atCenterOf(test.absolutePos(CENTER)), Direction.UP,
						test.absolutePos(CENTER), false);
				for (int count = 2; count <= 4; count++) {
					test.assertTrue(item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
							"Unlit candle failed stacking");
					test.assertTrue(test.getBlockState(CENTER).getValue(net.minecraft.world.level.block.CandleBlock.CANDLES)
							== count, "Wrong stacked candle count");
				}
				var drops = Block.getDrops(test.getBlockState(CENTER), test.getLevel(), test.absolutePos(CENTER), null);
				test.assertTrue(drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() == 4,
						"Unlit candle drops for " + test.getBlockState(CENTER) + " (canonical " + block.asItem()
								+ ") were " + drops + ", expected 4 " + item);
				test.setBlock(CENTER, Blocks.CAKE);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
				test.getBlockState(CENTER).useItemOn(player.getMainHandItem(), test.getLevel(), player,
						InteractionHand.MAIN_HAND, hit);
				test.assertTrue(test.getBlockState(CENTER).equals(
						net.minecraft.world.level.block.CandleCakeBlock.byCandle((net.minecraft.world.level.block.CandleBlock) block)),
						"Unlit candle did not add its matching candle to cake");
			}
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

	private static void useTorchItem(GameTestHelper test, Player player) {
		BlockPos pos = test.absolutePos(CENTER);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
	}

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

	@GameTest(maxTicks = 30)
	public void experienceClumpingPreservesMixedValuesCountsAndRadius(GameTestHelper test) {
		require(test, PatchworkConfig.settings().experienceClumping(), "experienceClumping");
		Vec3 center = Vec3.atCenterOf(test.absolutePos(CENTER));
		var first = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 7);
		((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor) first).patchwork$setCount(3);
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
		((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor) orb).patchwork$setCount(2);
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
				* ((com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor) orb).patchwork$count()).sum();
		test.assertTrue(total == 30, "Vanilla XP award lost value");
		awarded.forEach(net.minecraft.world.entity.ExperienceOrb::discard);

		var player = test.makeMockServerPlayer(GameType.SURVIVAL);
		var stacked = new net.minecraft.world.entity.ExperienceOrb(test.getLevel(), center.x, center.y, center.z, 15);
		var data = (com.JulieISBaka.patchwork.mixin.ExperienceOrbAccessor) stacked;
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
