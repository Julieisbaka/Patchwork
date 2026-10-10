package com.JulieISBaka.patchwork;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class HappyGhastSpeed {
	private HappyGhastSpeed() {
	}

	public static void register() {
		MobEffects.SPEED.value().addAttributeModifier(Attributes.FLYING_SPEED,
				Patchwork.id("effect.speed.flying_speed"), NumericSetting.FLYING_SPEED_EFFECT_MULTIPLIER.get(),
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
