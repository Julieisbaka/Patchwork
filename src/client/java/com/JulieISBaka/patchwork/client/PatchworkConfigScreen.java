package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class PatchworkConfigScreen extends OptionsSubScreen {
	private static final String[] KEYS = { "witherDifficultyHealth", "witherBirthExplosion", "chainmailRecipes",
			"wolfBanners", "ownerSweepProtection", "shulkerDyeing", "throwableSlimeballs", "callHornRecall",
			"cauldronCleaning", "beesDefendFlowers", "creeperChainReactions", "endermanDefense", "spiderWebs",
			"throwableFireCharges", "slimeSplitClouds", "breezeShockwave", "breezeTorchExtinguishing", "hoglinCharge",
			"skeletonCover", "potionCauldrons", "pumpkinLanterns", "experienceClumping" };
	private final boolean[] enabled;
	private String radiusText;
	private Button saveButton;

	public PatchworkConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("patchwork.config.title"));
		PatchworkConfig.Settings loaded = PatchworkConfig.read();
		this.enabled = new boolean[] { loaded.witherDifficultyHealth(), loaded.witherBirthExplosion(),
				loaded.chainmailRecipes(), loaded.wolfBanners(), loaded.ownerSweepProtection(), loaded.shulkerDyeing(),
				loaded.throwableSlimeballs(), loaded.callHornRecall(), loaded.cauldronCleaning(),
				loaded.beesDefendFlowers(), loaded.creeperChainReactions(), loaded.endermanDefense(),
				loaded.spiderWebs(), loaded.throwableFireCharges(), loaded.slimeSplitClouds(), loaded.breezeShockwave(),
				loaded.breezeTorchExtinguishing(), loaded.hoglinCharge(), loaded.skeletonCover(),
				loaded.potionCauldrons(), loaded.pumpkinLanterns(), loaded.experienceClumping() };
		this.radiusText = Integer.toString(loaded.callHornRecallRadius());
	}

	@Override
	protected void addOptions() {
		OptionsList options = Objects.requireNonNull(this.list);
		options.addHeader(Component.translatable("patchwork.config.restart"));
		options.addHeader(Component.translatable("patchwork.config.multiplayer"));
		for (int index = 0; index < KEYS.length; index++) {
			final int setting = index;
			String key = "patchwork.config." + KEYS[index];
			Button toggle = Button.builder(label(index), button -> {
				this.enabled[setting] = !this.enabled[setting];
				button.setMessage(label(setting));
			}).tooltip(Tooltip.create(Component.translatable(key + ".description"))).build();
			options.addBig(toggle);
			options.addBig(new StringWidget(Component.translatable(key + ".summary"), this.font).setMaxWidth(310));
		}

		options.addHeader(Component.translatable("patchwork.config.callHornRecallRadius"));
		EditBox radius = new EditBox(this.font, 310, 20,
				Component.translatable("patchwork.config.callHornRecallRadius"));
		radius.setMaxLength(3);
		radius.setTooltip(Tooltip.create(Component.translatable("patchwork.config.callHornRecallRadius.description")));
		radius.setValue(this.radiusText);
		radius.setResponder(value -> {
			this.radiusText = value;
			if (this.saveButton != null) {
				this.saveButton.active = validRadius();
			}
		});
		options.addBig(radius);
		options
				.addBig(new StringWidget(Component.translatable("patchwork.config.callHornRecallRadius.summary"),
						this.font)
						.setMaxWidth(310));
	}

	@Override
	protected void addFooter() {
		LinearLayout footer = LinearLayout.horizontal().spacing(8);
		this.saveButton = footer.addChild(
				Button.builder(Component.translatable("patchwork.config.save"), button -> save()).width(150).build());
		this.saveButton.active = validRadius();
		footer.addChild(Button.builder(Component.translatable("gui.cancel"), button -> onClose()).width(150).build());
		this.layout.addToFooter(footer);
	}

	private Component label(int index) {
		return Component.translatable("patchwork.config." + KEYS[index]).append(": ")
				.append(Component.translatable(this.enabled[index] ? "options.on" : "options.off"));
	}

	private boolean validRadius() {
		try {
			int radius = Integer.parseInt(this.radiusText);
			return radius >= 16 && radius <= 256;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private void save() {
		if (!validRadius()) {
			return;
		}
		try {
			PatchworkConfig.save(new PatchworkConfig.Settings(this.enabled[0], this.enabled[1], this.enabled[2],
					this.enabled[3], this.enabled[4], this.enabled[5], this.enabled[6], this.enabled[7],
					this.enabled[8],
					this.enabled[9], this.enabled[10], this.enabled[11], this.enabled[12], this.enabled[13],
					this.enabled[14], this.enabled[15], this.enabled[16], this.enabled[17], this.enabled[18],
					this.enabled[19], this.enabled[20], this.enabled[21], Integer.parseInt(this.radiusText)));
		} catch (IllegalStateException e) {
			Patchwork.LOGGER.error("Could not save Patchwork settings", e);
			this.saveButton.setMessage(Component.translatable("patchwork.config.save_failed"));
			this.saveButton.setTooltip(Tooltip.create(Component.literal(e.getMessage())));
			return;
		}
		onClose();
	}

	@Override
	public void removed() {
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
