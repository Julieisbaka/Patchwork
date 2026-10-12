package com.JulieISBaka.patchwork.features.mobs;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import com.JulieISBaka.patchwork.features.blocks.PumpkinLanterns;
import com.JulieISBaka.patchwork.features.blocks.UnlitLanterns;
import com.JulieISBaka.patchwork.features.blocks.UnlitTorches;
import com.JulieISBaka.patchwork.features.combat.PlayerSweepAccessor;
import com.JulieISBaka.patchwork.features.items.SoulFireCharges;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PatchworkMobsGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void babyVillagersAcceptIronGolemPoppies(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		Villager baby = new Villager(EntityTypes.VILLAGER, level);
		baby.setBaby(true);
		BlockPos center = test.absolutePos(CENTER);
		baby.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
		IronGolem golem = new IronGolem(EntityTypes.IRON_GOLEM, level);
		golem.setPos(center.getX() + 1.5, center.getY(), center.getZ() + 0.5);
		test.assertTrue(level.addFreshEntity(baby) && level.addFreshEntity(golem),
				"Could not add villager or golem for flower test");
		test.assertTrue(baby.wantsToPickUp(level, Items.POPPY.getDefaultInstance()),
				"Baby villager rejected a poppy");
		test.assertTrue(VillagerRoseOffering.offerToBaby(level, golem, baby),
				"Iron golem did not offer a poppy to the baby villager");
		test.assertTrue(test.getEntities(EntityTypes.ITEM).stream()
				.anyMatch(item -> item.getItem().is(Items.POPPY)), "The offered poppy item was not spawned");
		Villager adult = new Villager(EntityTypes.VILLAGER, level);
		test.assertTrue(!adult.wantsToPickUp(level, Items.POPPY.getDefaultInstance()),
				"Adult villager unexpectedly accepted an iron golem poppy");
		test.succeed();
	}

	@GameTest
	public void blazeRodsDoNotBurnInFireOrLava(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		BlockPos center = test.absolutePos(CENTER);
		ItemEntity rods = new ItemEntity(level, center.getX() + 0.5, center.getY(), center.getZ() + 0.5,
				new ItemStack(Items.BLAZE_ROD));
		test.assertTrue(level.addFreshEntity(rods), "Could not add blaze rods for fire-resistance test");
		test.assertTrue(rods.fireImmune(), "Blaze rods are not fire immune");
		test.assertTrue(!rods.hurtServer(level, level.damageSources().lava(), 5.0F) && !rods.isRemoved(),
				"Blaze rods were destroyed by lava damage");
		test.assertTrue(!rods.hurtServer(level, level.damageSources().inFire(), 5.0F) && !rods.isRemoved(),
				"Blaze rods were destroyed by fire damage");
		test.succeed();
	}

	@GameTest
	public void villagersConsumeFoodToHeal(GameTestHelper test) {
		Villager villager = new Villager(EntityTypes.VILLAGER, test.getLevel());
		villager.setHealth(10.0F);
		villager.getInventory().setItem(0, new ItemStack(Items.BREAD));
		test.assertTrue(VillagerFoodBehavior.eatToHeal(villager), "Villager did not consume food to heal");
		test.assertTrue(villager.getHealth() == 12.0F, "Villager did not heal two health points");
		test.assertTrue(VillagerFoodBehavior.foodItemCount(villager) == 0,
				"Villager did not consume its bread");
		test.assertTrue(!VillagerFoodBehavior.eatToHeal(villager), "Villager healed without any food");
		test.succeed();
	}

	@GameTest
	public void babyZombieChickenJockeyAttacksPlayers(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		BlockPos center = test.absolutePos(CENTER);
		Chicken chicken = new Chicken(EntityTypes.CHICKEN, level);
		chicken.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
		Zombie rider = new Zombie(EntityTypes.ZOMBIE, level);
		rider.setBaby(true);
		rider.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
		Player player = test.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(center.getX() + 1.0, center.getY(), center.getZ() + 0.5);
		test.assertTrue(level.addFreshEntity(chicken) && level.addFreshEntity(rider),
				"Could not add chicken jockey entities");
		test.assertTrue(level.addFreshEntity(player), "Could not add player for chicken jockey test");
		test.assertTrue(rider.startRiding(chicken), "Baby zombie could not ride the chicken");
		test.assertTrue(ChickenJockeyAttackGoal.isBabyZombieJockey(chicken),
				"Baby zombie chicken jockey was not recognized");
		ChickenJockeyAttackGoal goal = new ChickenJockeyAttackGoal(chicken);
		test.assertTrue(goal.canUse(), "Chicken jockey did not target the nearby player");
		float health = player.getHealth();
		goal.tick();
		test.assertTrue(player.getHealth() < health, "Chicken jockey did not damage the nearby player");
		test.succeed();
	}

	@GameTest
	public void blazesHealFromFireSeekItAndResistEffects(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		BlockPos center = test.absolutePos(CENTER);
		level.setBlockAndUpdate(center.below(), Blocks.STONE.defaultBlockState());
		level.setBlockAndUpdate(center, Blocks.FIRE.defaultBlockState());
		Blaze blaze = new Blaze(EntityTypes.BLAZE, level);
		blaze.setPos(center.getX() + 3.5, center.getY(), center.getZ() + 0.5);
		test.assertTrue(level.addFreshEntity(blaze), "Could not add blaze for fire behavior test");
		blaze.setHealth(8.0F);
		test.assertTrue(!blaze.hurtServer(level, level.damageSources().onFire(), 4.0F)
				&& blaze.getHealth() > 8.0F, "Fire damage did not heal the blaze");
		test.assertTrue(!blaze.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON))
				&& !blaze.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WITHER))
				&& !blaze.canBeAffected(new net.minecraft.world.effect.MobEffectInstance(MobEffects.HUNGER)),
				"Blaze accepted poison, wither, or hunger");
		BlazeFireSeekingGoal goal = new BlazeFireSeekingGoal(blaze);
		test.assertTrue(goal.canUse(), "Injured blaze did not find nearby fire");
		Player target = test.makeMockPlayer(GameType.SURVIVAL);
		target.setPos(blaze.getX() + 1.0, blaze.getY(), blaze.getZ());
		test.assertTrue(level.addFreshEntity(target), "Could not add melee target for blaze test");
		test.assertTrue(blaze.doHurtTarget(level, target), "Blaze melee attack did not hit its target");
		test.assertTrue(target.isOnFire(), "Blaze melee attack did not set its target on fire");
		test.succeed();
	}

	@GameTest
	public void spidersAreFastHaveLongSightAndTakeNoFallDamage(GameTestHelper test) {
		var level = (net.minecraft.server.level.ServerLevel) test.getLevel();
		Spider spider = test.spawn(EntityTypes.SPIDER, CENTER);
		CaveSpider caveSpider = test.spawn(EntityTypes.CAVE_SPIDER, CENTER.offset(2, 0, 0));
		for (Spider mob : new Spider[] { spider, caveSpider }) {
			test.assertTrue(mob.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.4,
					"Spider movement speed was not increased");
			test.assertTrue(mob.getAttributeValue(Attributes.FOLLOW_RANGE) == 32.0,
					"Spider follow range was not increased");
			float health = mob.getHealth();
			test.assertTrue(!mob.causeFallDamage(20.0, 1.0F, level.damageSources().fall())
					&& mob.getHealth() == health, "Spider took fall damage");
		}
		test.succeed();
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
		test.assertTrue(golem.getHealth() == 30.0F && player.getOffhandItem().getCount() == 2,
				"Gold did not heal 25 HP and consume exactly one ingot");
		golem.interact(player, InteractionHand.OFF_HAND, Vec3.ZERO);
		golem.interact(player, InteractionHand.OFF_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 30.0F && player.getOffhandItem().getCount() == 2,
				"Repairing full health consumed an ingot or exceeded max health");
		Player creative = test.makeMockPlayer(GameType.CREATIVE);
		creative.getAbilities().instabuild = true;
		creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
		golem.setHealth(25.0F);
		golem.interact(creative, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(golem.getHealth() == 30.0F && creative.getMainHandItem().getCount() == 1,
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
		test.assertTrue(golem.getMaxHealth() == 30.0F && !golem.canAttack(player) && golem.isPlayerCreated()
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
				test.assertTrue(golem.getMaxHealth() == 30.0F,
						"Soul Golem does not have 30 health");
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
	public void buildingSoulGolemAwardsAdvancement(GameTestHelper test) {
		net.minecraft.server.level.ServerPlayer builder = (net.minecraft.server.level.ServerPlayer) test
				.makeMockServerPlayer(GameType.SURVIVAL);
		builder.setPos(Vec3.atBottomCenterOf(test.absolutePos(CENTER)));
		BlockPos head = test.absolutePos(CENTER.above(2));
		test.setBlock(CENTER, Blocks.SOUL_SAND);
		test.setBlock(CENTER.above(), Blocks.SOUL_SOIL);
		SoulGolems.withBuilder(builder, () -> {
			test.setBlock(CENTER.above(2), PumpkinLanterns.SOUL_BLOCK);
			return null;
		});
		test.assertTrue(test.getEntities(SoulGolems.TYPE).stream().anyMatch(golem -> golem.blockPosition().distSqr(head) < 16),
				"Player-built Soul Golem did not spawn");
		var advancement = test.getLevel().getServer().getAdvancements()
				.get(Patchwork.id("adventure/a_soulful_companion"));
		test.assertTrue(advancement != null && builder.getAdvancements().getOrStartProgress(advancement).isDone(),
				"Building a Soul Golem did not award A Soulful Companion");
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

	@GameTest(maxTicks = 10)
	public void spidersClingToAndTraverseCeilings(GameTestHelper test) {
		test.setBlock(CENTER.above(), Blocks.STONE);
		test.setBlock(CENTER.offset(1, 1, 0), Blocks.STONE);
		Spider spider = test.spawn(EntityTypes.SPIDER, CENTER);
		spider.setNoAi(true);
		spider.setPos(test.absolutePos(CENTER).getX() + 0.5, test.absolutePos(CENTER).getY() + 0.1,
				test.absolutePos(CENTER).getZ() + 0.5);
		double startX = spider.getX();
		double startY = spider.getY();
		spider.setDeltaMovement(0.1, 0.0, 0.0);
		test.runAfterDelay(3, () -> {
			test.assertTrue(spider.isNoGravity() && spider.getY() >= startY - 0.15,
					"Spider fell instead of clinging to the overhead ceiling");
			test.assertTrue(spider.getX() > startX + 0.1, "Spider did not move horizontally along the ceiling");
			test.succeed();
		});
	}

	@GameTest
	public void ownerCanShearBannerFromWolf(GameTestHelper test) {
		require(test, PatchworkConfig.settings().wolfBanners(), "wolfBanners");
		var wolf = test.spawn(EntityTypes.WOLF, CENTER);
		Player owner = test.makeMockPlayer(GameType.SURVIVAL);
		wolf.tame(owner);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BANNER.white()));
		wolf.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(wolf.getItemBySlot(EquipmentSlot.HEAD).is(Items.BANNER.white()),
				"Owner could not equip a banner on the wolf");

		Player stranger = test.makeMockPlayer(GameType.SURVIVAL);
		stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
		wolf.interact(stranger, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(wolf.getItemBySlot(EquipmentSlot.HEAD).is(Items.BANNER.white())
				&& stranger.getMainHandItem().getDamageValue() == 0,
				"Non-owner removed the wolf banner or damaged their shears");

		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
		wolf.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
		test.assertTrue(wolf.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
				&& java.util.stream.IntStream.range(0, owner.getInventory().getContainerSize())
						.anyMatch(slot -> owner.getInventory().getItem(slot).is(Items.BANNER.white()))
				&& owner.getMainHandItem().getDamageValue() == 1,
				"Owner could not shear and recover the wolf's banner");
		test.succeed();
	}

	@GameTest
	public void caveSpiderCanInflictNausea(GameTestHelper test) {
		CaveSpider spider = test.spawn(EntityTypes.CAVE_SPIDER, CENTER);
		spider.setNoAi(true);
		boolean nauseaApplied = false;
		for (int attempt = 0; attempt < 100 && !nauseaApplied; attempt++) {
			var victim = test.spawn(EntityTypes.COW, CENTER);
			spider.doHurtTarget(test.getLevel(), victim);
			nauseaApplied = victim.hasEffect(MobEffects.NAUSEA);
			victim.discard();
		}
		test.assertTrue(nauseaApplied, "Repeated cave-spider hits never inflicted Nausea");
		test.succeed();
	}

	@GameTest
	public void wanderingTraderOffersSweetBerries(GameTestHelper test) {
		var trader = test.spawn(EntityTypes.WANDERING_TRADER, CENTER);
		WanderingTraderTrades.addSweetBerries(trader);
		test.assertTrue(trader.getOffers().stream().anyMatch(offer -> offer.getResult().is(Items.SWEET_BERRIES)),
				"Wandering trader did not receive a sweet-berry trade");
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

	@GameTest
	public void creakingHasIncreasedMovementAndAttackAttributes(GameTestHelper test) {
		var creaking = test.spawn(EntityTypes.CREAKING, CENTER);
		test.assertTrue(creaking.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.5,
				"Creaking movement speed was not increased");
		test.assertTrue(creaking.getAttributeValue(Attributes.ATTACK_DAMAGE) == 4.0,
				"Creaking attack damage was not increased");
		test.succeed();
	}

	@GameTest(maxTicks = 40)
	public void animalsEatDroppedFood(GameTestHelper test) {
		test.assertTrue(test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING),
				"Enable mobGriefing before running this test");
		var cow = test.spawn(EntityTypes.COW, CENTER);
		net.minecraft.server.level.ServerPlayer feeder = (net.minecraft.server.level.ServerPlayer) test
				.makeMockServerPlayer(GameType.SURVIVAL);
		var item = new ItemEntity(test.getLevel(), cow.getX(), cow.getY(), cow.getZ(),
				new ItemStack(Items.WHEAT, 2));
		item.setPickUpDelay(0);
		item.setThrower(feeder);
		test.getLevel().addFreshEntity(item);
		test.runAfterDelay(20, () -> {
			test.assertTrue(item.isAlive() && item.getItem().getCount() == 1,
					"Animal did not eat one item from the nearby food stack");
			test.assertTrue(cow.isInLove(), "Eating dropped food did not feed the adult animal");
			var advancement = test.getLevel().getServer().getAdvancements()
					.get(Patchwork.id("adventure/creature_comforts"));
			test.assertTrue(advancement != null
					&& feeder.getAdvancements().getOrStartProgress(advancement).isDone(),
					"Feeding an animal with dropped food did not award Creature Comforts");
			test.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void animalsDoNotEatDroppedFoodWithoutMobGriefing(GameTestHelper test) {
		boolean original = test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
		test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, false, test.getLevel().getServer());
		var cow = test.spawn(EntityTypes.COW, CENTER);
		var item = new ItemEntity(test.getLevel(), cow.getX(), cow.getY(), cow.getZ(), new ItemStack(Items.WHEAT));
		item.setPickUpDelay(0);
		test.getLevel().addFreshEntity(item);
		test.runAfterDelay(20, () -> {
			boolean didNotEat = item.isAlive() && item.getItem().getCount() == 1 && !cow.isInLove();
			test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, test.getLevel().getServer());
			test.assertTrue(didNotEat, "Animal ate dropped food while mobGriefing was disabled");
			test.succeed();
		});
	}

	@GameTest
	public void snowSpeedIsAThreeLevelBootEnchantmentExclusiveWithSoulSpeed(GameTestHelper test) {
		var enchantments = test.getLevel().registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
		var snowSpeed = enchantments.getOrThrow(net.minecraft.resources.ResourceKey.create(
				net.minecraft.core.registries.Registries.ENCHANTMENT, Patchwork.id("snow_speed")));
		var soulSpeed = enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SOUL_SPEED);
		test.assertTrue(snowSpeed.value().definition().maxLevel() == 3
				&& snowSpeed.value().exclusiveSet().contains(soulSpeed),
				"Snow Speed did not load at level III or exclude Soul Speed");
		ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
		boots.enchant(snowSpeed, 3);
		test.assertTrue(boots.getEnchantments().getLevel(snowSpeed) == 3,
				"Snow Speed III could not be applied to boots");
		test.succeed();
	}

	@GameTest(maxTicks = 10)
	public void snowSpeedBoostsMovementOnSnow(GameTestHelper test) {
		test.setBlock(CENTER.below(), Blocks.SNOW_BLOCK);
		var cow = test.spawn(EntityTypes.COW, CENTER);
		cow.setNoAi(true);
		var enchantment = test.getLevel().registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(net.minecraft.resources.ResourceKey.create(
						net.minecraft.core.registries.Registries.ENCHANTMENT, Patchwork.id("snow_speed")));
		ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
		boots.enchant(enchantment, 1);
		cow.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, boots);
		double unenchantedSpeed = cow.getAttributeValue(Attributes.MOVEMENT_SPEED);
		cow.setPos(cow.getX() + 0.1, cow.getY(), cow.getZ());
		test.runAfterDelay(2, () -> {
			test.assertTrue(cow.getAttributeValue(Attributes.MOVEMENT_SPEED) > unenchantedSpeed,
					"Snow Speed did not increase movement speed on snow");
			test.succeed();
		});
	}

	@GameTest
	public void speedPotionIncreasesHappyGhastFlyingSpeed(GameTestHelper test) {
		var happyGhast = test.spawn(EntityTypes.HAPPY_GHAST, CENTER);
		double baseSpeed = happyGhast.getAttributeValue(Attributes.FLYING_SPEED);
		happyGhast.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SPEED, 200, 0));
		test.assertTrue(happyGhast.getAttributeValue(Attributes.FLYING_SPEED) > baseSpeed,
				"Speed did not increase a Happy Ghast's flying speed");
		test.succeed();
	}

	private static void require(GameTestHelper test, boolean enabled, String setting) {
		test.assertTrue(enabled, "Enable " + setting + " in config/patchwork.properties before running this test");
	}
}
