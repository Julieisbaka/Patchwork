package com.JulieISBaka.patchwork;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class PatchworkConfig {
	private static final int DEFAULT_RECALL_RADIUS = 32;
	private static final int MIN_RECALL_RADIUS = 16;
	private static final int MAX_RECALL_RADIUS = 256;
	private static final String RECALL_RADIUS_KEY = "callHornRecallRadius";
	private static volatile Settings settings = new Settings(true, true, true, true, true, true, true, true, true,
		true, false, true, true, true, true, true, true, true, true, true, true, true, DEFAULT_RECALL_RADIUS);

	public record Settings(
		boolean witherDifficultyHealth,
		boolean witherBirthExplosion,
		boolean chainmailRecipes,
		boolean wolfBanners,
		boolean ownerSweepProtection,
		boolean shulkerDyeing,
		boolean throwableSlimeballs,
		boolean callHornRecall,
		boolean cauldronCleaning,
		boolean beesDefendFlowers,
		boolean creeperChainReactions,
		boolean endermanDefense,
		boolean spiderWebs,
		boolean throwableFireCharges,
		boolean slimeSplitClouds,
		boolean breezeShockwave,
		boolean breezeTorchExtinguishing,
		boolean hoglinCharge,
		boolean skeletonCover,
		boolean potionCauldrons,
		boolean pumpkinLanterns,
		boolean experienceClumping,
		int callHornRecallRadius
	) {
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
		Path path = FabricLoader.getInstance().getConfigDir().resolve("patchwork.properties");
		Properties properties = new Properties();
		try {
			Files.createDirectories(path.getParent());
			if (Files.notExists(path)) {
				properties.setProperty(RECALL_RADIUS_KEY, Integer.toString(DEFAULT_RECALL_RADIUS));
			} else {
				try (Reader reader = Files.newBufferedReader(path)) {
					properties.load(reader);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read Patchwork config: " + path, e);
		}

		String value = properties.getProperty(RECALL_RADIUS_KEY);
		if (value == null) {
			throw new IllegalArgumentException("Missing " + RECALL_RADIUS_KEY + " in " + path);
		}
		int radius;
		try {
			radius = Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(RECALL_RADIUS_KEY + " must be an integer from 16 to 256 in " + path, e);
		}
		if (radius < MIN_RECALL_RADIUS || radius > MAX_RECALL_RADIUS) {
			throw new IllegalArgumentException(RECALL_RADIUS_KEY + " must be from 16 to 256 in " + path + ": " + radius);
		}
		int originalSize = properties.size();
		boolean migratedTorches = properties.containsKey("unlitTorches");
		if (migratedTorches) {
			properties.putIfAbsent("breezeTorchExtinguishing", properties.getProperty("unlitTorches"));
			properties.remove("unlitTorches");
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
		boolean endermanDefense = enabled(properties, "endermanDefense", true, path);
		boolean spiderWebs = enabled(properties, "spiderWebs", true, path);
		boolean throwableFireCharges = enabled(properties, "throwableFireCharges", path);
		boolean slimeSplitClouds = enabled(properties, "slimeSplitClouds", path);
		boolean breezeShockwave = enabled(properties, "breezeShockwave", path);
		boolean breezeTorchExtinguishing = enabled(properties, "breezeTorchExtinguishing", path);
		boolean hoglinCharge = enabled(properties, "hoglinCharge", path);
		boolean skeletonCover = enabled(properties, "skeletonCover", path);
		boolean potionCauldrons = enabled(properties, "potionCauldrons", path);
		boolean pumpkinLanterns = enabled(properties, "pumpkinLanterns", path);
		boolean experienceClumping = enabled(properties, "experienceClumping", path);

		if (migratedTorches || properties.size() != originalSize || Files.notExists(path)) {
			try (Writer writer = Files.newBufferedWriter(path)) {
				properties.store(writer, "Patchwork features: true/false; callHornRecallRadius: 16-256 blocks. Restart to apply.");
			} catch (IOException e) {
				throw new IllegalStateException("Unable to write Patchwork config: " + path, e);
			}
		}
		Settings loaded = new Settings(witherDifficultyHealth, witherBirthExplosion, chainmailRecipes, wolfBanners,
			ownerSweepProtection, shulkerDyeing, throwableSlimeballs, callHornRecall, cauldronCleaning,
			beesDefendFlowers, creeperChainReactions, endermanDefense, spiderWebs, throwableFireCharges,
			slimeSplitClouds, breezeShockwave, breezeTorchExtinguishing, hoglinCharge, skeletonCover,
			potionCauldrons, pumpkinLanterns, experienceClumping, radius);
		Patchwork.LOGGER.info("Patchwork config loaded from {}", path);
		return loaded;
	}

	public static void save(Settings updated) {
		int radius = updated.callHornRecallRadius();
		if (radius < MIN_RECALL_RADIUS || radius > MAX_RECALL_RADIUS) {
			throw new IllegalArgumentException(RECALL_RADIUS_KEY + " must be from 16 to 256: " + radius);
		}
		Path path = FabricLoader.getInstance().getConfigDir().resolve("patchwork.properties");
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
			properties.remove("unlitTorches");
			properties.setProperty("breezeTorchExtinguishing", Boolean.toString(updated.breezeTorchExtinguishing()));
			properties.setProperty("hoglinCharge", Boolean.toString(updated.hoglinCharge()));
			properties.setProperty("skeletonCover", Boolean.toString(updated.skeletonCover()));
			properties.setProperty("potionCauldrons", Boolean.toString(updated.potionCauldrons()));
			properties.setProperty("pumpkinLanterns", Boolean.toString(updated.pumpkinLanterns()));
			properties.setProperty("experienceClumping", Boolean.toString(updated.experienceClumping()));
			properties.setProperty(RECALL_RADIUS_KEY, Integer.toString(radius));
			try (Writer writer = Files.newBufferedWriter(path)) {
				properties.store(writer, "Patchwork features: true/false; callHornRecallRadius: 16-256 blocks. Restart to apply.");
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
