package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.NumericSetting;
import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** Config-screen validation, persistence, reopening, and cancellation. */
final class PatchworkClientConfigGameTests {
	private PatchworkClientConfigGameTests() {
	}

	/** Runs this focused client GameTest group. */
	static void run(ClientGameTestContext context) {
		testConfigurationScreen(context);
	}

	/** Checks config-screen validation, persistence, reopening, and cancellation. */
	private static void testConfigurationScreen(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var original = PatchworkConfig.read();
			try {
				verifyConfigurationScreen(client);
			} finally {
				PatchworkConfig.save(original);
			}
		});
	}

	/** Exercises config inputs and verifies Save and Cancel behavior. */
	private static void verifyConfigurationScreen(net.minecraft.client.Minecraft client) {
		Screen parent = new TitleScreen();
		PatchworkConfigScreen screen = new PatchworkConfigScreen(parent);
		client.gui.setScreen(screen);
		OptionsList options = screen.children().stream().filter(OptionsList.class::isInstance)
				.map(OptionsList.class::cast).findFirst().orElseThrow();
		List<? extends ContainerEventHandler> entries = options.children();
		List<EditBox> inputs = entries.stream().flatMap(entry -> entry.children().stream())
				.filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
		if (inputs.size() != NumericSetting.values().length + 1) {
			throw new AssertionError("Configuration screen is missing numeric inputs");
		}
		Button save = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
				.filter(button -> button.getMessage().getString()
						.equals(Component.translatable("patchwork.config.save").getString()))
				.findFirst().orElseThrow();
		if (!save.active) {
			throw new AssertionError("Valid loaded configuration cannot be saved");
		}
		for (NumericSetting setting : NumericSetting.values()) {
			Component label = Component.translatable("patchwork.config." + setting.key());
			if (label.getString().equals("patchwork.config." + setting.key())) {
				throw new AssertionError("Numeric setting label is not translated: " + setting.key());
			}
			EditBox input = inputs.get(setting.ordinal() + 1);
			String original = input.getValue();
			input.setValue("NaN");
			if (save.active) {
				throw new AssertionError("Invalid numeric field did not disable Save: " + setting.key());
			}
			input.setValue(original);
			if (!save.active) {
				throw new AssertionError("Correcting a numeric field did not re-enable Save: " + setting.key());
			}
		}
		EditBox minimum = inputs.get(NumericSetting.SPIDER_WEB_MIN_DISTANCE.ordinal() + 1);
		String originalMinimum = minimum.getValue();
		EditBox maximum = inputs.get(NumericSetting.SPIDER_WEB_MAX_DISTANCE.ordinal() + 1);
		String originalMaximum = maximum.getValue();
		maximum.setValue("8");
		minimum.setValue("9");
		if (save.active) {
			throw new AssertionError("Invalid paired Spider distances did not disable Save");
		}
		minimum.setValue(originalMinimum);
		maximum.setValue(originalMaximum);
		EditBox animalRadius = inputs.get(NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS.ordinal() + 1);
		animalRadius.setValue("16.5");
		save.onPress(new KeyEvent(40, 0, 0));
		if (PatchworkConfig.read().numericValues().get(NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS) != 16.5) {
			throw new AssertionError("Config screen did not persist a fractional food radius");
		}
		PatchworkConfigScreen reopened = new PatchworkConfigScreen(parent);
		client.gui.setScreen(reopened);
		OptionsList reloadedOptions = reopened.children().stream().filter(OptionsList.class::isInstance)
				.map(OptionsList.class::cast).findFirst().orElseThrow();
		List<? extends ContainerEventHandler> reloadedEntries = reloadedOptions.children();
		List<EditBox> reloadedInputs = reloadedEntries.stream()
				.flatMap(entry -> entry.children().stream()).filter(EditBox.class::isInstance)
				.map(EditBox.class::cast).toList();
		EditBox reloadedRadius = reloadedInputs.get(NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS.ordinal() + 1);
		if (!reloadedRadius.getValue().equals("16.5")) {
			throw new AssertionError("Reopened config screen did not restore saved numeric settings");
		}
		reloadedRadius.setValue("20");
		reopened.onClose();
		if (PatchworkConfig.read().numericValues().get(NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS) != 16.5) {
			throw new AssertionError("Cancelling the config screen saved unsaved edits");
		}
		client.gui.setScreen(parent);
	}
}
