package com.JulieISBaka.patchwork.config;

import com.JulieISBaka.patchwork.features.mobs.AnimalFoodGoal;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;

public final class PatchworkConfigGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void translationsCoverEveryKeyAndPlaceholder(GameTestHelper test) throws Exception {
		var loader = PatchworkConfigGameTests.class.getClassLoader();
		com.google.gson.JsonObject english;
		try (var stream = java.util.Objects.requireNonNull(
				loader.getResourceAsStream("assets/patchwork/lang/en_us.json"));
				var reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
			english = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
		}
		var placeholder = java.util.regex.Pattern.compile("%(?:(\\d+)\\$)?[sd]");
		for (String language : java.util.List.of("de_de", "fr_fr", "es_es", "pt_br", "zh_cn", "ja_jp", "ko_kr",
				"ru_ru")) {
			try (var stream = java.util.Objects.requireNonNull(
					loader.getResourceAsStream("assets/patchwork/lang/" + language + ".json"));
					var reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
				var translated = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
				test.assertTrue(translated.keySet().equals(english.keySet()),
						"Translation keys differ from English: " + language);
				for (String key : english.keySet()) {
					var value = translated.get(key);
					test.assertTrue(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
							&& !value.getAsString().isBlank(), "Empty/non-string translation: " + language + ":" + key);
					var expected = placeholder.matcher(english.get(key).getAsString()).results()
							.map(java.util.regex.MatchResult::group).sorted().toList();
					var actual = placeholder.matcher(value.getAsString()).results()
							.map(java.util.regex.MatchResult::group).sorted().toList();
					test.assertTrue(expected.equals(actual), "Broken translation placeholders: " + language + ":" + key);
				}
			}
		}
		test.succeed();
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
				if (component.getType() == java.util.Map.class) {
					continue;
				}
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
				if (component.getType() == java.util.Map.class) {
					continue;
				}
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
				if (component.getType() == java.util.Map.class) {
					continue;
				}
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
	public void numericConfigurationMigrationValidationAndSave(GameTestHelper test) throws Exception {
		var directory = java.nio.file.Files.createTempDirectory("patchwork-numeric-config-test-");
		var path = directory.resolve("patchwork.properties");
		try {
			var defaults = PatchworkConfig.read(path);
			var baseline = new java.util.Properties();
			try (var reader = java.nio.file.Files.newBufferedReader(path)) {
				baseline.load(reader);
			}
			baseline.setProperty("customOption", "keep-me");
			for (NumericSetting setting : NumericSetting.values()) {
				test.assertTrue(defaults.numericValues().get(setting) == setting.defaultValue()
						&& baseline.getProperty(setting.key()).equals(setting.format(setting.defaultValue())),
						"Wrong numeric default: " + setting.key());
				var incomplete = new java.util.Properties();
				incomplete.putAll(baseline);
				incomplete.remove(setting.key());
				try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
					incomplete.store(writer, "Numeric migration regression");
				}
				var migrated = PatchworkConfig.read(path);
				test.assertTrue(migrated.numericValues().get(setting) == setting.defaultValue(),
						"Missing numeric setting was not defaulted: " + setting.key());
				var persisted = new java.util.Properties();
				try (var reader = java.nio.file.Files.newBufferedReader(path)) {
					persisted.load(reader);
				}
				test.assertTrue(persisted.getProperty(setting.key()).equals(setting.format(setting.defaultValue())),
						"Numeric migration was not saved: " + setting.key());
				for (String key : incomplete.stringPropertyNames()) {
					test.assertTrue(incomplete.getProperty(key).equals(persisted.getProperty(key)),
							"Numeric migration changed an existing property: " + key);
				}
				var invalidValues = new java.util.ArrayList<>(java.util.List.of("", "no", "NaN", "Infinity",
						"-Infinity", Double.toString(setting.minimum() - 1), Double.toString(setting.maximum() + 1)));
				if (setting.integer()) {
					invalidValues.add("1.5");
					invalidValues.add("10.0");
				}
				for (String invalid : invalidValues) {
					var properties = new java.util.Properties();
					properties.putAll(baseline);
					properties.remove("animalDroppedFood");
					properties.setProperty(setting.key(), invalid);
					try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
						properties.store(writer, "Invalid numeric config must not be rewritten");
					}
					var before = java.nio.file.Files.readAllBytes(path);
					boolean rejected = false;
					try {
						PatchworkConfig.read(path);
					} catch (IllegalArgumentException e) {
						rejected = e.getMessage().contains(setting.key());
					}
					test.assertTrue(rejected && java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(path)),
							"Invalid numeric setting was accepted or rewritten: " + setting.key() + "=" + invalid);
				}
				for (double boundary : new double[] { setting.minimum(), setting.maximum() }) {
					var properties = new java.util.Properties();
					properties.putAll(baseline);
					properties.setProperty(setting.key(), setting.format(boundary));
					// Keep the paired distance valid when testing either boundary independently.
					properties.setProperty("spiderWebMinDistance",
							setting == NumericSetting.SPIDER_WEB_MIN_DISTANCE ? setting.format(boundary) : "0.0");
					properties.setProperty("spiderWebMaxDistance",
							setting == NumericSetting.SPIDER_WEB_MAX_DISTANCE ? setting.format(boundary) : "64.0");
					try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
						properties.store(writer, "Numeric boundary and save regression");
					}
					var loaded = PatchworkConfig.read(path);
					test.assertTrue(loaded.numericValues().get(setting) == boundary,
							"Valid boundary rejected: " + setting.key());
					PatchworkConfig.save(path, loaded);
					test.assertTrue(PatchworkConfig.read(path).equals(loaded), "Numeric settings did not round-trip");
					try (var reader = java.nio.file.Files.newBufferedReader(path)) {
						persisted.clear();
						persisted.load(reader);
					}
					test.assertTrue("keep-me".equals(persisted.getProperty("customOption")),
							"Saving numeric settings removed an unknown property");
				}
			}
			baseline.setProperty("spiderWebMinDistance", "9.0");
			baseline.setProperty("spiderWebMaxDistance", "8.0");
			baseline.remove("animalDroppedFood");
			try (var writer = java.nio.file.Files.newBufferedWriter(path)) {
				baseline.store(writer, "Invalid paired distances");
			}
			var before = java.nio.file.Files.readAllBytes(path);
			boolean rejected = false;
			try {
				PatchworkConfig.read(path);
			} catch (IllegalArgumentException e) {
				rejected = e.getMessage().contains("spiderWebMinDistance");
			}
			test.assertTrue(rejected && java.util.Arrays.equals(before, java.nio.file.Files.readAllBytes(path)),
					"Invalid paired distances were accepted or rewritten");
			test.succeed();
		} finally {
			java.nio.file.Files.deleteIfExists(path);
			java.nio.file.Files.delete(directory);
		}
	}

	@GameTest
	public void animalFoodUsesConfiguredRadiusAndToggle(GameTestHelper test) {
		boolean original = test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
		test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, test.getLevel().getServer());
		try {
			var animal = test.spawn(EntityTypes.COW, CENTER);
			animal.setNoAi(true);
			double radius = NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS.get();
			ItemEntity food = new ItemEntity(test.getLevel(), animal.getX() + radius + 2, animal.getY(), animal.getZ(),
					new ItemStack(Items.WHEAT, 2));
			food.setNoPickUpDelay();
			test.assertTrue(test.getLevel().addFreshEntity(food), "Could not spawn food for configured radius test");
			AnimalFoodGoal outside = new AnimalFoodGoal(animal);
			test.assertTrue(!outside.canUse(), "Animal searched beyond its configured food radius");
			food.setPos(animal.getX() + radius * 0.75, animal.getY(), animal.getZ());
			AnimalFoodGoal inside = new AnimalFoodGoal(animal);
			boolean enabled = PatchworkConfig.settings().animalDroppedFood();
			test.assertTrue(inside.canUse() == enabled, "Animal food radius or feature toggle was ignored");
			if (enabled) {
				test.assertTrue(inside.canContinueToUse(), "Food inside configured radius was not pursued");
				food.setPos(animal.getX() + radius + 1, animal.getY(), animal.getZ());
				test.assertTrue(!inside.canContinueToUse(), "Animal pursued food outside configured radius");
			}
			food.discard();
			test.succeed();
		} finally {
			test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, test.getLevel().getServer());
		}
	}
}
