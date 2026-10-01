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
	private static volatile int callHornRecallRadius = DEFAULT_RECALL_RADIUS;

	private PatchworkConfig() {
	}

	public static int callHornRecallRadius() {
		return callHornRecallRadius;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("patchwork.properties");
		Properties properties = new Properties();
		try {
			Files.createDirectories(path.getParent());
			if (Files.notExists(path)) {
				properties.setProperty(RECALL_RADIUS_KEY, Integer.toString(DEFAULT_RECALL_RADIUS));
				try (Writer writer = Files.newBufferedWriter(path)) {
					properties.store(writer, "Call goat horn recall radius in blocks (16-256)");
				}
			}
			try (Reader reader = Files.newBufferedReader(path)) {
				properties.load(reader);
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
		callHornRecallRadius = radius;
		Patchwork.LOGGER.info("Call goat horn recall radius: {} blocks", radius);
	}
}
