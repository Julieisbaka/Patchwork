package com.JulieISBaka.patchwork;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class PatchworkConfig {
	private static final int DEFAULT_RECALL_RADIUS = 32;
	private static final int MIN_RECALL_RADIUS = 16;
	private static final int MAX_RECALL_RADIUS = 256;
	private static final String RECALL_RADIUS_KEY = "callHornRecallRadius";
	private static volatile Settings settings = new Settings(true, true, true, true, true, true, true, true, true, true,
			false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
			true, true, DEFAULT_RECALL_RADIUS, true, true, true, true, true, true, true, true, true,
			NumericSetting.defaults());

	public record Settings(boolean witherDifficultyHealth, boolean witherBirthExplosion, boolean chainmailRecipes,
			boolean wolfBanners, boolean ownerSweepProtection, boolean shulkerDyeing, boolean throwableSlimeballs,
			boolean callHornRecall, boolean cauldronCleaning, boolean beesDefendFlowers, boolean creeperChainReactions,
			boolean endermanDefense, boolean spiderWebs, boolean throwableFireCharges, boolean slimeSplitClouds,
			boolean breezeShockwave, boolean breezeTorchExtinguishing, boolean hoglinCharge, boolean skeletonCover,
			boolean potionCauldrons, boolean pumpkinLanterns, boolean experienceClumping, boolean sweetBerryTrades,
			boolean elytraDyeing, boolean stoneToolMaterials, boolean playerHeadRecipe, boolean spiderCeilingClimbing,
			boolean caveSpiderNausea, boolean loyalTridentVoidReturn, boolean patchworkAdvancements,
			int callHornRecallRadius, boolean animalDroppedFood, boolean rabbitCarrotTaming, boolean soulGolems,
			boolean soulFireCharges, boolean soulFireChargeDispenserProjectiles, boolean soulGolemProjectiles,
			boolean unlitLights, boolean soulCopperLightBlocks, boolean restoredPaintings, boolean illusionerRaidSpawns,
			Map<NumericSetting, Double> numericValues) {
		public Settings {
			numericValues = Map.copyOf(numericValues);
			for (NumericSetting setting : NumericSetting.values()) {
				Double value = numericValues.get(setting);
				if (value == null) {
					throw new IllegalArgumentException("Missing numeric setting: " + setting.key());
				}
				setting.validate(value);
			}
			if (numericValues.get(NumericSetting.SPIDER_WEB_MIN_DISTANCE)
					> numericValues.get(NumericSetting.SPIDER_WEB_MAX_DISTANCE)) {
				throw new IllegalArgumentException("spiderWebMinDistance must not exceed spiderWebMaxDistance");
			}
		}
	}

	private PatchworkConfig() {
	}

	public static int callHornRecallRadius() {
		return settings.callHornRecallRadius();
	}

	public static Settings settings() {
		return settings;
	}

	public static void load() {
		settings = read();
	}

	public static Settings read() {
		return read(FabricLoader.getInstance().getConfigDir().resolve("patchwork.properties"));
	}

	static Settings read(Path path) {
		Properties properties = new Properties();
		boolean newConfig = Files.notExists(path);
		try {
			Files.createDirectories(path.getParent());
			if (!newConfig) {
				try (Reader reader = Files.newBufferedReader(path)) {
					properties.load(reader);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read Patchwork config: " + path, e);
		}

		int originalPropertyCount = properties.size();
		String value = properties.getProperty(RECALL_RADIUS_KEY);
		if (value == null) {
			value = Integer.toString(DEFAULT_RECALL_RADIUS);
			properties.setProperty(RECALL_RADIUS_KEY, value);
			Patchwork.LOGGER.info("Adding missing Patchwork setting {}={} to {}", RECALL_RADIUS_KEY, value, path);
		}
		int radius;
		try {
			radius = Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(RECALL_RADIUS_KEY + " must be an integer from 16 to 256 in " + path, e);
		}
		if (radius < MIN_RECALL_RADIUS || radius > MAX_RECALL_RADIUS) {
			throw new IllegalArgumentException(
					RECALL_RADIUS_KEY + " must be from 16 to 256 in " + path + ": " + radius);
		}
		boolean witherDifficultyHealth = enabled(properties, "witherDifficultyHealth", path);
		boolean witherBirthExplosion = enabled(properties, "witherBirthExplosion", path);
		boolean chainmailRecipes = enabled(properties, "chainmailRecipes", path);
		boolean wolfBanners = enabled(properties, "wolfBanners", path);
		boolean ownerSweepProtection = enabled(properties, "ownerSweepProtection", path);
		boolean shulkerDyeing = enabled(properties, "shulkerDyeing", path);
		boolean throwableSlimeballs = enabled(properties, "throwableSlimeballs", path);
		boolean callHornRecall = enabled(properties, "callHornRecall", path);
		boolean cauldronCleaning = enabled(properties, "cauldronCleaning", path);
		boolean beesDefendFlowers = enabled(properties, "beesDefendFlowers", true, path);
		boolean creeperChainReactions = enabled(properties, "creeperChainReactions", false, path);
		boolean endermanDefense = enabled(properties, "endermanDefense", path);
		boolean spiderWebs = enabled(properties, "spiderWebs", path);
		boolean throwableFireCharges = enabled(properties, "throwableFireCharges", path);
		boolean slimeSplitClouds = enabled(properties, "slimeSplitClouds", path);
		boolean breezeShockwave = enabled(properties, "breezeShockwave", path);
		boolean breezeTorchExtinguishing = enabled(properties, "breezeTorchExtinguishing", path);
		boolean hoglinCharge = enabled(properties, "hoglinCharge", path);
		boolean skeletonCover = enabled(properties, "skeletonCover", path);
		boolean potionCauldrons = enabled(properties, "potionCauldrons", path);
		boolean pumpkinLanterns = enabled(properties, "pumpkinLanterns", path);
		boolean experienceClumping = enabled(properties, "experienceClumping", path);
		boolean sweetBerryTrades = enabled(properties, "sweetBerryTrades", path);
		boolean elytraDyeing = enabled(properties, "elytraDyeing", path);
		boolean stoneToolMaterials = enabled(properties, "stoneToolMaterials", path);
		boolean playerHeadRecipe = enabled(properties, "playerHeadRecipe", path);
		boolean spiderCeilingClimbing = enabled(properties, "spiderCeilingClimbing", path);
		boolean caveSpiderNausea = enabled(properties, "caveSpiderNausea", path);
		boolean loyalTridentVoidReturn = enabled(properties, "loyalTridentVoidReturn", path);
		boolean patchworkAdvancements = enabled(properties, "patchworkAdvancements", path);
		boolean animalDroppedFood = enabled(properties, "animalDroppedFood", path);
		boolean rabbitCarrotTaming = enabled(properties, "rabbitCarrotTaming", path);
		boolean soulGolems = enabled(properties, "soulGolems", path);
		boolean soulFireCharges = enabled(properties, "soulFireCharges", path);
		boolean soulFireChargeDispenserProjectiles = enabled(properties, "soulFireChargeDispenserProjectiles", path);
		boolean soulGolemProjectiles = enabled(properties, "soulGolemProjectiles", path);
		boolean unlitLights = enabled(properties, "unlitLights", path);
		boolean soulCopperLightBlocks = enabled(properties, "soulCopperLightBlocks", path);
		boolean restoredPaintings = enabled(properties, "restoredPaintings", path);
		boolean illusionerRaidSpawns = enabled(properties, "illusionerRaidSpawns", path);
		Map<NumericSetting, Double> numericValues = new EnumMap<>(NumericSetting.class);
		for (NumericSetting setting : NumericSetting.values()) {
			String text = properties.getProperty(setting.key());
			if (text == null) {
				text = setting.format(setting.defaultValue());
				properties.setProperty(setting.key(), text);
				Patchwork.LOGGER.info("Adding missing Patchwork setting {}={} to {}", setting.key(), text, path);
			}
			try {
				numericValues.put(setting, setting.parse(text));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Invalid Patchwork config in " + path + ": " + e.getMessage(), e);
			}
		}

		Settings loaded = new Settings(witherDifficultyHealth, witherBirthExplosion, chainmailRecipes, wolfBanners,
				ownerSweepProtection, shulkerDyeing, throwableSlimeballs, callHornRecall, cauldronCleaning,
				beesDefendFlowers, creeperChainReactions, endermanDefense, spiderWebs, throwableFireCharges,
				slimeSplitClouds, breezeShockwave, breezeTorchExtinguishing, hoglinCharge, skeletonCover,
				potionCauldrons,
				pumpkinLanterns, experienceClumping, sweetBerryTrades, elytraDyeing, stoneToolMaterials,
				playerHeadRecipe, spiderCeilingClimbing, caveSpiderNausea, loyalTridentVoidReturn,
				patchworkAdvancements, radius, animalDroppedFood, rabbitCarrotTaming, soulGolems, soulFireCharges,
				soulFireChargeDispenserProjectiles, soulGolemProjectiles, unlitLights, soulCopperLightBlocks,
				restoredPaintings, illusionerRaidSpawns, numericValues);
		if (properties.size() != originalPropertyCount) {
			try (Writer writer = Files.newBufferedWriter(path)) {
				properties.store(writer,
						"Patchwork settings. See docs/configuration.md for ranges and units. Restart to apply.");
			} catch (IOException e) {
				throw new IllegalStateException("Unable to write Patchwork config: " + path, e);
			}
		}
		Patchwork.LOGGER.info("Patchwork config loaded from {}", path);
		return loaded;
	}

	public static void save(Settings updated) {
		save(FabricLoader.getInstance().getConfigDir().resolve("patchwork.properties"), updated);
	}

	static void save(Path path, Settings updated) {
		int radius = updated.callHornRecallRadius();
		if (radius < MIN_RECALL_RADIUS || radius > MAX_RECALL_RADIUS) {
			throw new IllegalArgumentException(RECALL_RADIUS_KEY + " must be from 16 to 256: " + radius);
		}
		Properties properties = new Properties();
		try {
			try (Reader reader = Files.newBufferedReader(path)) {
				properties.load(reader);
			}
			properties.setProperty("witherDifficultyHealth", Boolean.toString(updated.witherDifficultyHealth()));
			properties.setProperty("witherBirthExplosion", Boolean.toString(updated.witherBirthExplosion()));
			properties.setProperty("chainmailRecipes", Boolean.toString(updated.chainmailRecipes()));
			properties.setProperty("wolfBanners", Boolean.toString(updated.wolfBanners()));
			properties.setProperty("ownerSweepProtection", Boolean.toString(updated.ownerSweepProtection()));
			properties.setProperty("shulkerDyeing", Boolean.toString(updated.shulkerDyeing()));
			properties.setProperty("throwableSlimeballs", Boolean.toString(updated.throwableSlimeballs()));
			properties.setProperty("callHornRecall", Boolean.toString(updated.callHornRecall()));
			properties.setProperty("cauldronCleaning", Boolean.toString(updated.cauldronCleaning()));
			properties.setProperty("beesDefendFlowers", Boolean.toString(updated.beesDefendFlowers()));
			properties.setProperty("creeperChainReactions", Boolean.toString(updated.creeperChainReactions()));
			properties.setProperty("endermanDefense", Boolean.toString(updated.endermanDefense()));
			properties.setProperty("spiderWebs", Boolean.toString(updated.spiderWebs()));
			properties.setProperty("throwableFireCharges", Boolean.toString(updated.throwableFireCharges()));
			properties.setProperty("slimeSplitClouds", Boolean.toString(updated.slimeSplitClouds()));
			properties.setProperty("breezeShockwave", Boolean.toString(updated.breezeShockwave()));
			properties.setProperty("breezeTorchExtinguishing", Boolean.toString(updated.breezeTorchExtinguishing()));
			properties.setProperty("hoglinCharge", Boolean.toString(updated.hoglinCharge()));
			properties.setProperty("skeletonCover", Boolean.toString(updated.skeletonCover()));
			properties.setProperty("potionCauldrons", Boolean.toString(updated.potionCauldrons()));
			properties.setProperty("pumpkinLanterns", Boolean.toString(updated.pumpkinLanterns()));
			properties.setProperty("experienceClumping", Boolean.toString(updated.experienceClumping()));
			properties.setProperty("sweetBerryTrades", Boolean.toString(updated.sweetBerryTrades()));
			properties.setProperty("elytraDyeing", Boolean.toString(updated.elytraDyeing()));
			properties.setProperty("stoneToolMaterials", Boolean.toString(updated.stoneToolMaterials()));
			properties.setProperty("playerHeadRecipe", Boolean.toString(updated.playerHeadRecipe()));
			properties.setProperty("spiderCeilingClimbing", Boolean.toString(updated.spiderCeilingClimbing()));
			properties.setProperty("caveSpiderNausea", Boolean.toString(updated.caveSpiderNausea()));
			properties.setProperty("loyalTridentVoidReturn", Boolean.toString(updated.loyalTridentVoidReturn()));
			properties.setProperty("patchworkAdvancements", Boolean.toString(updated.patchworkAdvancements()));
			properties.setProperty(RECALL_RADIUS_KEY, Integer.toString(radius));
			properties.setProperty("animalDroppedFood", Boolean.toString(updated.animalDroppedFood()));
			properties.setProperty("rabbitCarrotTaming", Boolean.toString(updated.rabbitCarrotTaming()));
			properties.setProperty("soulGolems", Boolean.toString(updated.soulGolems()));
			properties.setProperty("soulFireCharges", Boolean.toString(updated.soulFireCharges()));
			properties.setProperty("soulFireChargeDispenserProjectiles",
					Boolean.toString(updated.soulFireChargeDispenserProjectiles()));
			properties.setProperty("soulGolemProjectiles", Boolean.toString(updated.soulGolemProjectiles()));
			properties.setProperty("unlitLights", Boolean.toString(updated.unlitLights()));
			properties.setProperty("soulCopperLightBlocks", Boolean.toString(updated.soulCopperLightBlocks()));
			properties.setProperty("restoredPaintings", Boolean.toString(updated.restoredPaintings()));
			properties.setProperty("illusionerRaidSpawns", Boolean.toString(updated.illusionerRaidSpawns()));
			for (NumericSetting setting : NumericSetting.values()) {
				properties.setProperty(setting.key(), setting.format(updated.numericValues().get(setting)));
			}
			try (Writer writer = Files.newBufferedWriter(path)) {
				properties.store(writer,
						"Patchwork settings. See docs/configuration.md for ranges and units. Restart to apply.");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to save Patchwork config: " + path, e);
		}
	}

	private static boolean enabled(Properties properties, String key, Path path) {
		return enabled(properties, key, true, path);
	}

	private static boolean enabled(Properties properties, String key, boolean defaultValue, Path path) {
		String value = properties.getProperty(key);
		if (value == null) {
			properties.setProperty(key, Boolean.toString(defaultValue));
			Patchwork.LOGGER.info("Adding missing Patchwork setting {}={} to {}", key, defaultValue, path);
			return defaultValue;
		}
		if (value.trim().equalsIgnoreCase("true")) {
			return true;
		}
		if (value.trim().equalsIgnoreCase("false")) {
			return false;
		}
		throw new IllegalArgumentException(key + " must be true or false in " + path + ": " + value);
	}
}
