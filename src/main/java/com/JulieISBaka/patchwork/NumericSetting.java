package com.JulieISBaka.patchwork;

import java.util.EnumMap;
import java.util.Map;

/**
 * Numeric gameplay settings, including their config keys, defaults, and validation bounds.
 */
public enum NumericSetting {
	ANIMAL_FOOD_SEARCH_RADIUS("animalFoodSearchRadius", 8, 1, 64, false),
	ANIMAL_FOOD_EAT_DISTANCE("animalFoodEatDistance", 1.5, 0.1, 8, false),
	ANIMAL_FOOD_SEARCH_INTERVAL("animalFoodSearchIntervalTicks", 10, 1, 1200, true),
	ANIMAL_FOOD_EAT_COOLDOWN("animalFoodEatCooldownTicks", 40, 0, 12000, true),
	ANIMAL_FOOD_REPATH_INTERVAL("animalFoodRepathIntervalTicks", 10, 1, 1200, true),
	ANIMAL_FOOD_MOVEMENT_SPEED("animalFoodMovementSpeed", 1.1, 0.1, 4, false),
	BEE_DEFENSE_RADIUS("beeDefenseRadius", 4, 1, 16, true),
	BREEZE_SHOCKWAVE_RADIUS("breezeShockwaveRadius", 5, 1, 16, true),
	BREEZE_SHOCKWAVE_COOLDOWN("breezeShockwaveCooldownTicks", 200, 0, 12000, true),
	BREEZE_HORIZONTAL_PUSH("breezeShockwaveHorizontalPush", 1.2, 0, 4, false),
	BREEZE_VERTICAL_PUSH("breezeShockwaveVerticalPush", 0.25, 0, 2, false),
	CAVE_SPIDER_NAUSEA_CHANCE("caveSpiderNauseaChance", 0.2, 0, 1, false),
	CAVE_SPIDER_NAUSEA_DURATION("caveSpiderNauseaDurationTicks", 100, 1, 12000, true),
	ENDERMAN_DEFENSE_CHANCE("endermanDefenseChance", 1.0 / 3.0, 0, 1, false),
	SPIDER_WEB_INTERVAL("spiderWebIntervalTicks", 100, 1, 12000, true),
	SPIDER_WEB_CHANCE("spiderWebChance", 0.25, 0, 1, false),
	SPIDER_WEB_MIN_DISTANCE("spiderWebMinDistance", 2, 0, 64, false),
	SPIDER_WEB_MAX_DISTANCE("spiderWebMaxDistance", 8, 0.1, 64, false),
	SLIMEBALL_COOLDOWN("slimeballCooldownTicks", 20, 0, 12000, true),
	SLIMEBALL_DAMAGE("slimeballDamage", 1, 0, 100, false),
	SLIMEBALL_HEALING("slimeballHealing", 1, 0, 100, false),
	SLIMEBALL_EFFECT_DURATION("slimeballEffectDurationTicks", 60, 1, 12000, true),
	SLIMEBALL_EFFECT_AMPLIFIER("slimeballEffectAmplifier", 0, 0, 10, true),
	THROWABLE_COOLDOWN("eggSnowballCooldownTicks", 2, 0, 12000, true),
	FIRE_CHARGE_COOLDOWN("fireChargeCooldownTicks", 30, 0, 12000, true),
	FIRE_CHARGE_SPEED("fireChargeSpeed", 0.65, 0.05, 4, false),
	FIRE_CHARGE_ACCELERATION("fireChargeAcceleration", 0.03, 0, 1, false),
	FIRE_CHARGE_CLEARANCE("fireChargeClearance", 5, 0, 16, false),
	FIRE_CHARGE_DAMAGE("fireChargeDamage", 2, 0, 100, false),
	SOUL_FIRE_CHARGE_DAMAGE("soulFireChargeDamage", 3, 0, 100, false),
	SOUL_FIRE_CHARGE_BURN_SECONDS("soulFireChargeBurnSeconds", 2, 0, 600, false),
	SLIME_CLOUD_LIFETIME("slimeCloudLifetimeTicks", 100, 1, 12000, true),
	SLIME_CLOUD_RADIUS("slimeCloudRadius", 1, 0, 8, true),
	SLIME_CLOUD_EFFECT_AMPLIFIER("slimeCloudEffectAmplifier", 1, 0, 10, true),
	EXPERIENCE_MERGE_RADIUS("experienceMergeRadius", 2, 0.1, 16, false),
	EXPERIENCE_MERGE_INTERVAL("experienceMergeIntervalTicks", 20, 1, 1200, true),
	HOGLIN_CHARGE_MIN_SPEED("hoglinChargeMinSpeed", 0.18, 0, 2, false),
	HOGLIN_HORIZONTAL_LAUNCH("hoglinHorizontalLaunch", 0.2, 0, 4, false),
	HOGLIN_VERTICAL_LAUNCH("hoglinVerticalLaunch", 0.69, 0, 4, false),
	HOGLIN_IMPACT_WINDOW("hoglinImpactWindowTicks", 20, 1, 1200, true),
	HOGLIN_IMPACT_DAMAGE("hoglinImpactDamage", 2, 0, 100, false),
	WITHER_EASY_HEALTH("witherEasyHealth", 300, 1, 1024, false),
	WITHER_NORMAL_HEALTH("witherNormalHealth", 450, 1, 1024, false),
	WITHER_HARD_HEALTH("witherHardHealth", 600, 1, 1024, false),
	WITHER_BIRTH_BONUS_DAMAGE("witherBirthBonusDamage", 3, 0, 100, false),
	SOUL_GOLEM_ATTACK_INTERVAL("soulGolemAttackIntervalTicks", 40, 1, 1200, true),
	SOUL_GOLEM_ATTACK_RANGE("soulGolemAttackRange", 16, 1, 64, false),
	SOUL_GOLEM_HEALING("soulGolemGoldHealing", 25, 0, 100, false),
	SKELETON_COVER_TARGET_DISTANCE("skeletonCoverTargetDistance", 6, 1, 64, false),
	SKELETON_COVER_MOVEMENT_SPEED("skeletonCoverMovementSpeed", 1.15, 0.1, 4, false),
	FLYING_SPEED_EFFECT_MULTIPLIER("flyingSpeedEffectMultiplier", 0.2, 0, 4, false);

	private final String key;
	private final double defaultValue;
	private final double minimum;
	private final double maximum;
	private final boolean integer;

	/** Creates a numeric setting with a default value, inclusive range, and integer constraint. */
	NumericSetting(String key, double defaultValue, double minimum, double maximum, boolean integer) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.minimum = minimum;
		this.maximum = maximum;
		this.integer = integer;
	}

	/** Returns the key used to persist this setting. */
	public String key() {
		return this.key;
	}

	/** Returns the default value used when this setting is missing. */
	public double defaultValue() {
		return this.defaultValue;
	}

	/** Returns the inclusive minimum accepted value. */
	public double minimum() {
		return this.minimum;
	}

	/** Returns the inclusive maximum accepted value. */
	public double maximum() {
		return this.maximum;
	}

	/** Returns whether this setting only accepts whole numbers. */
	public boolean integer() {
		return this.integer;
	}

	/** Parses and validates a value written in the format expected by this setting. */
	public double parse(String text) {
		double value;
		try {
			value = this.integer ? Integer.parseInt(text.trim()) : Double.parseDouble(text.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(this.key + " must be "
					+ (this.integer ? "an integer" : "a number") + " from " + this.minimum + " to " + this.maximum, e);
		}
		validate(value);
		return value;
	}

	/** Validates that a value is finite and within this setting's configured range. */
	public void validate(double value) {
		if (!Double.isFinite(value) || value < this.minimum || value > this.maximum
				|| (this.integer && value != Math.rint(value))) {
			throw new IllegalArgumentException(this.key + " must be "
					+ (this.integer ? "an integer" : "a finite number") + " from "
					+ this.minimum + " to " + this.maximum + ": " + value);
		}
	}

	/** Formats a value for persistence or display. */
	public String format(double value) {
		return this.integer ? Integer.toString((int) value) : Double.toString(value);
	}

	/** Reads this setting's current value from the loaded Patchwork configuration. */
	public double get() {
		return PatchworkConfig.settings().numericValues().get(this);
	}

	/** Returns this setting as an integer, rejecting non-integer settings. */
	public int intValue() {
		if (!this.integer) {
			throw new IllegalStateException(this.key + " is not an integer setting");
		}
		return (int) get();
	}

	/** Returns this setting as a float. */
	public float floatValue() {
		return (float) get();
	}

	/** Returns the square of this setting's current value. */
	public double squared() {
		double value = get();
		return value * value;
	}

	/** Creates an immutable map containing the default value for every numeric setting. */
	public static Map<NumericSetting, Double> defaults() {
		Map<NumericSetting, Double> values = new EnumMap<>(NumericSetting.class);
		for (NumericSetting setting : values()) {
			values.put(setting, setting.defaultValue);
		}
		return Map.copyOf(values);
	}
}
