package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.CopperTorches;
import com.JulieISBaka.patchwork.GardenBlocks;
import com.JulieISBaka.patchwork.LightVariants;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.NumericSetting;
import com.JulieISBaka.patchwork.PotionCauldronEntity;
import com.JulieISBaka.patchwork.PotionCauldrons;
import com.JulieISBaka.patchwork.RabbitPet;
import com.JulieISBaka.patchwork.client.mixin.LivingEntityRendererAccessor;
import com.JulieISBaka.patchwork.SoulFireCharges;
import com.JulieISBaka.patchwork.SoulFireSupport;
import com.JulieISBaka.patchwork.SoulFireball;
import com.JulieISBaka.patchwork.SoulGolem;
import com.JulieISBaka.patchwork.SoulGolems;
import com.JulieISBaka.patchwork.UnlitLanterns;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public class PatchworkClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		testConfigurationScreen(context);
		if (Boolean.getBoolean("patchwork.configScreenTestOnly")) {
			return;
		}
		testPetsAndRendererMigration(context);
		testPotionFirstFillTint(context);
		testSoulAndLightModels(context);
		testGardenAndArtwork(context);
		testNewLightVariants(context);
	}

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

	private static void verifyConfigurationScreen(net.minecraft.client.Minecraft client) {
			Screen parent = new Screen(Component.literal("Configuration test")) {
			};
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
					.filter(button -> button.getMessage().getString().equals(Component.translatable("patchwork.config.save").getString()))
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

	private static void testNewLightVariants(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -10 100 -12 10 100 12 minecraft:stone");
			world.getServer().runCommand("time set night");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 104 10.5 180 25");
			for (int stage = 0; stage < 4; stage++) {
				String prefix = List.of("", "exposed_", "weathered_", "oxidized_").get(stage);
				int x = -4 + stage * 3;
				world.getServer().runCommand("setblock " + x + " 101 0 patchwork:" + prefix
						+ "copper_candle[candles=4,lit=true]");
				world.getServer().runCommand("setblock " + x + " 101 -3 patchwork:" + prefix
						+ "copper_campfire[lit=true]");
				world.getServer().runCommand("setblock " + x + " 101 -6 patchwork:" + prefix
						+ "copper_jack_o_lantern[facing=south]");
				world.getServer().runCommand("setblock " + x + " 101 3 patchwork:" + prefix
						+ "copper_candle_cake[lit=true]");
			}
			world.getServer().runCommand("setblock -7 101 0 patchwork:soul_candle[candles=4,lit=true]");
			world.getServer().runCommand("setblock -7 101 3 patchwork:soul_candle_cake[lit=true]");
			context.waitFor(client -> client.level.getBlockState(new BlockPos(-7, 101, 0)).is(LightVariants.SOUL_CANDLE));
			context.runOnClient(client -> {
				var models = client.getModelManager().getBlockStateModelSet();
				for (var block : LightVariants.blocks()) {
					for (var state : block.getStateDefinition().getPossibleStates()) {
						if (models.get(state) == models.missingModel()) {
							throw new AssertionError("Light variant block model missing: " + state);
						}
					}
					if (block.asItem() != Items.AIR) {
						var itemState = new ItemStackRenderState();
						client.getItemModelResolver().updateForLiving(itemState, new ItemStack(block),
								ItemDisplayContext.GUI, client.player);
						if (itemState.isEmpty()) {
							throw new AssertionError("Light variant item model missing: " + block);
						}
					}
				}
				for (var item : LightVariants.unlitItems().values()) {
					var itemState = new ItemStackRenderState();
					client.getItemModelResolver().updateForLiving(itemState, new ItemStack(item),
							ItemDisplayContext.GUI, client.player);
					if (itemState.isEmpty()) {
						throw new AssertionError("Unlit light item model missing: " + item);
					}
				}
				for (String prefix : List.of("", "exposed_", "weathered_", "oxidized_")) {
					for (String name : List.of("copper_candle", "copper_candle_lit", "copper_campfire_fire",
							"copper_campfire_log_lit", "copper_jack_o_lantern")) {
						String vanilla = name.startsWith("copper_candle") ? name.replace("copper_", "")
								: name.startsWith("copper_campfire") ? name.replace("copper_", "") : "jack_o_lantern";
						try {
							var resource = client.getResourceManager().getResource(
									Patchwork.id("textures/block/" + prefix + name + ".png")).orElseThrow();
							var reference = client.getResourceManager().getResource(
									net.minecraft.resources.Identifier.withDefaultNamespace("textures/block/" + vanilla + ".png"))
									.orElseThrow();
							try (var stream = resource.open(); var vanillaStream = reference.open()) {
								var image = ImageIO.read(stream);
								var original = ImageIO.read(vanillaStream);
								if (image == null || original == null || image.getWidth() != original.getWidth()
										|| image.getHeight() != original.getHeight()) {
									throw new AssertionError("Light variant texture dimensions differ: " + name);
								}
								for (int y = 0; y < image.getHeight(); y++) {
									for (int x = 0; x < image.getWidth(); x++) {
										if ((image.getRGB(x, y) >>> 24) != (original.getRGB(x, y) >>> 24)) {
											throw new AssertionError("Light variant changed vanilla silhouette: " + name);
										}
									}
								}
							}
						} catch (IOException exception) {
							throw new AssertionError("Could not read light variant texture: " + name, exception);
						}
					}
				}
			});
			world.getConnection().waitForChunksRender();
			context.takeScreenshot("patchwork-soul-and-oxidizing-copper-lights");
		}
	}

	private static void testPetsAndRendererMigration(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -8 100 -8 8 100 8 minecraft:grass_block");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("tp @a 0.5 101 5.5 180 15");
			var rabbitId = new java.util.concurrent.atomic.AtomicInteger();
			var wolfId = new java.util.concurrent.atomic.AtomicInteger();
			world.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var level = player.level();
				var rabbit = java.util.Objects.requireNonNull(net.minecraft.world.entity.EntityTypes.RABBIT
						.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND));
				rabbit.snapTo(-1.5, 101, 0.5, 0, 0);
				level.addFreshEntity(rabbit);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
				rabbit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				rabbit.interact(player, InteractionHand.MAIN_HAND, net.minecraft.world.phys.Vec3.ZERO);
				rabbitId.set(rabbit.getId());
				var wolf = java.util.Objects.requireNonNull(net.minecraft.world.entity.EntityTypes.WOLF
						.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND));
				wolf.snapTo(1.5, 101, 0.5, 0, 0);
				wolf.tame(player);
				wolf.setOrderedToSit(true);
				wolf.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.BANNER.white()));
				level.addFreshEntity(wolf);
				wolfId.set(wolf.getId());
			});
			context.waitFor(client -> client.level.getEntity(rabbitId.get()) instanceof RabbitPet pet
					&& pet.patchwork$isOwnedBy(client.player) && pet.patchwork$isOrderedToStay()
					&& client.level.getEntity(wolfId.get()) instanceof net.minecraft.world.entity.animal.wolf.Wolf wolf
					&& wolf.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.BANNER.white()));
			context.runOnClient(client -> {
				var wolf = (net.minecraft.world.entity.animal.wolf.Wolf) client.level.getEntity(wolfId.get());
				var renderer = (net.minecraft.client.renderer.entity.WolfRenderer) client.getEntityRenderDispatcher()
						.getRenderer(wolf);
				if (((LivingEntityRendererAccessor) renderer).patchwork$getLayers().stream()
						.filter(layer -> layer instanceof WolfBannerLayer).count() != 1) {
					throw new AssertionError("Wolf banner layer was not registered exactly once");
				}
				var state = renderer.createRenderState(wolf, 0);
				if (!((WolfBannerState) state).patchwork$getBanner().is(Items.BANNER.white())) {
					throw new AssertionError("Wolf banner extraction failed after renderer migration");
				}
			});
			world.getConnection().waitForChunksRender();
			context.takeScreenshot("patchwork-tamed-rabbit-and-wolf-banner");
		}
	}

	private static void testGardenAndArtwork(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -10 100 -10 10 100 10 minecraft:grass_block");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 101 7.5 180 15");
			world.getServer().runCommand("setblock -2 101 0 patchwork:wax_block");
			world.getServer().runCommand("setblock 0 101 0 patchwork:paeonia");
			world.getServer().runCommand("setblock 2 101 0 patchwork:potted_paeonia");
			world.getServer().runCommand("setblock -4 101 0 patchwork:soul_jack_o_lantern[facing=south]");
			world.getServer().runCommand("setblock 4 101 0 patchwork:unlit_torch");
			world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst()
					.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(GardenBlocks.PAEONIA_ITEM)));
			context.waitFor(client -> client.level.getBlockState(new BlockPos(0, 101, 0)).is(GardenBlocks.PAEONIA)
					&& client.level.getBlockState(new BlockPos(-2, 101, 0)).is(GardenBlocks.WAX_BLOCK)
					&& client.level.getBlockState(new BlockPos(2, 101, 0)).is(GardenBlocks.POTTED_PAEONIA));
			context.runOnClient(client -> {
				var models = client.getModelManager().getBlockStateModelSet();
				for (var block : List.of(GardenBlocks.WAX_BLOCK, GardenBlocks.PAEONIA, GardenBlocks.POTTED_PAEONIA)) {
					if (models.get(block.defaultBlockState()) == models.missingModel()) {
						throw new AssertionError("Garden block model is missing: " + block);
					}
				}
				for (var item : List.of(GardenBlocks.WAX_ITEM, GardenBlocks.PAEONIA_ITEM)) {
					var itemState = new ItemStackRenderState();
					client.getItemModelResolver().updateForLiving(itemState, new ItemStack(item),
							ItemDisplayContext.GUI, client.player);
					if (itemState.isEmpty()) {
						throw new AssertionError("Garden item model is missing: " + item);
					}
				}
				var textures = new ArrayList<String>();
				textures.addAll(List.of("block/wax_block", "block/paeonia", "block/charcoal_block",
						"block/soul_jack_o_lantern", "item/soul_golem_spawn_egg", "item/soul_fire_charge"));
				for (String name : List.of("unlit_lantern", "unlit_soul_lantern", "unlit_copper_lantern",
						"unlit_exposed_copper_lantern", "unlit_weathered_copper_lantern",
						"unlit_oxidized_copper_lantern")) {
					textures.add("block/" + name);
					textures.add("item/" + name);
				}
				for (String name : List.of("unlit_torch", "unlit_soul_torch", "unlit_copper_torch",
						"unlit_exposed_copper_torch", "unlit_weathered_copper_torch", "unlit_oxidized_copper_torch",
						"exposed_copper_torch", "weathered_copper_torch", "oxidized_copper_torch")) {
					textures.add("block/" + name);
				}
				textures.add("entity/soul_golem");
				for (String name : textures) {
					var resource = client.getResourceManager().getResource(Patchwork.id("textures/" + name + ".png"))
							.orElseThrow(() -> new AssertionError("Texture is missing: " + name));
					try (var stream = resource.open()) {
						var image = ImageIO.read(stream);
						int size = name.equals("entity/soul_golem") ? 64 : 16;
						if (image == null || image.getWidth() != size || image.getHeight() != size) {
							throw new AssertionError("Texture has incorrect dimensions: " + name);
						}
						verifyVanillaTextureVariant(client.getResourceManager(), name, image);
						if (name.equals("block/exposed_copper_torch")) {
							verifyCopperTorchOxidationTextures(client.getResourceManager());
						}
						if ((name.startsWith("item/") || name.equals("block/paeonia"))
								&& (image.getRGB(0, 0) >>> 24) != 0) {
							throw new AssertionError("Icon/flower lost its transparent background: " + name);
						}
						if (name.equals("block/unlit_torch")) {
							for (int y = 0; y < 16; y++) {
								for (int x = 0; x < 16; x++) {
									int expectedAlpha = y >= 6 && (x == 7 || x == 8) ? 255 : 0;
									if ((image.getRGB(x, y) >>> 24) != expectedAlpha) {
										throw new AssertionError("Unlit torch does not fit the vanilla UVs at "
												+ x + "," + y);
									}
								}
							}
						}
						if (name.equals("block/wax_block") || name.equals("block/soul_jack_o_lantern")) {
							for (int y = 0; y < 16; y++) {
								for (int x = 0; x < 16; x++) {
									if ((image.getRGB(x, y) >>> 24) != 255) {
										throw new AssertionError("Solid block texture contains transparent pixels: "
												+ name);
									}
								}
							}
						}
						if (name.equals("entity/soul_golem")) {
							for (int y = 16; y < 36; y++) {
								int start = y < 26 ? 10 : 0;
								int end = y < 26 ? 30 : 40;
								for (int x = start; x < end; x++) {
									if ((image.getRGB(x, y) >>> 24) != 255) {
										throw new AssertionError(
												"Soul Golem torso UV is transparent at " + x + "," + y);
									}
								}
							}
						}
					} catch (IOException exception) {
						throw new AssertionError("Could not decode texture: " + name, exception);
					}
				}
			});
			context.waitTicks(2);
			world.getConnection().waitForChunksRender();
			context.takeScreenshot("preserved-garden-artwork-and-corrected-unlit-torch");
			world.getServer().runCommand("fill -8 101 -4 8 104 4 minecraft:air");
			world.getServer().runCommand("fill -10 100 -10 10 100 10 minecraft:stone");
			world.getServer().runCommand("tp @a 0.5 103 8.5 180 30");
			world.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				var level = player.level();
				var lanterns = new ArrayList<Block>();
				lanterns.add(UnlitLanterns.LANTERN);
				lanterns.add(UnlitLanterns.SOUL_LANTERN);
				for (var age : net.minecraft.world.level.block.WeatheringCopper.WeatherState.values()) {
					lanterns.add(UnlitLanterns.COPPER_LANTERN.weathering().pick(age));
				}
				for (int index = 0; index < lanterns.size(); index++) {
					level.setBlockAndUpdate(new BlockPos(index * 2 - 5, 101, -2),
							lanterns.get(index).defaultBlockState());
				}
				int index = 0;
				for (var age : net.minecraft.world.level.block.WeatheringCopper.WeatherState.values()) {
					level.setBlockAndUpdate(new BlockPos(index * 2 - 3, 101, 0),
							CopperTorches.LIT.weathering().pick(age).defaultBlockState());
					level.setBlockAndUpdate(new BlockPos(index * 2 - 3, 101, 2),
							CopperTorches.UNLIT.weathering().pick(age).defaultBlockState());
					index++;
				}
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(UnlitLanterns.SOUL_LANTERN.asItem()));
			});
			context.waitTicks(10);
			world.getConnection().waitForChunksRender();
			context.takeScreenshot("original-lantern-and-copper-torch-artwork");
		}
	}

	private static void verifyCopperTorchOxidationTextures(
			net.minecraft.server.packs.resources.ResourceManager resources) throws IOException {
		var vanillaResource = resources.getResource(net.minecraft.resources.Identifier.withDefaultNamespace(
				"textures/block/copper_torch.png")).orElseThrow();
		java.awt.image.BufferedImage vanilla;
		try (var stream = vanillaResource.open()) {
			vanilla = ImageIO.read(stream);
		}
		if (vanilla == null) {
			throw new AssertionError("Could not decode vanilla copper torch");
		}
		int previousBrightness = Integer.MAX_VALUE;
		for (String stage : List.of("", "exposed_", "weathered_", "oxidized_")) {
			var id = stage.isEmpty() ? net.minecraft.resources.Identifier.withDefaultNamespace(
					"textures/block/copper_torch.png") : Patchwork.id("textures/block/" + stage + "copper_torch.png");
			var resource = resources.getResource(id).orElseThrow();
			try (var stream = resource.open()) {
				var image = ImageIO.read(stream);
				if (image == null || image.getWidth() != 16 || image.getHeight() != 16) {
					throw new AssertionError("Invalid copper torch texture: " + id);
				}
				int brightness = 0;
				for (int y = 0; y < 16; y++) {
					for (int x = 0; x < 16; x++) {
						int pixel = image.getRGB(x, y);
						int original = vanilla.getRGB(x, y);
						boolean flame = (x == 7 || x == 8) && (y == 6 || y == 7);
						if ((pixel >>> 24) != (original >>> 24) || !flame && pixel != original) {
							throw new AssertionError("Copper oxidation changed vanilla silhouette or wood: " + id);
						}
						if (flame) {
							brightness += ((pixel >> 16) & 255) + ((pixel >> 8) & 255) + (pixel & 255);
						}
					}
				}
				if (brightness >= previousBrightness) {
					throw new AssertionError("Copper oxidation texture is not progressively dimmer: " + id);
				}
				previousBrightness = brightness;
			}
		}
	}

	private static void verifyVanillaTextureVariant(net.minecraft.server.packs.resources.ResourceManager resources,
			String name, java.awt.image.BufferedImage image) throws IOException {
		String vanilla = null;
		boolean lantern = name.startsWith("block/unlit_") && name.endsWith("lantern");
		boolean lanternItem = List.of("item/unlit_lantern", "item/unlit_soul_lantern",
				"item/unlit_copper_lantern").contains(name);
		boolean torch = name.startsWith("block/unlit_") && name.endsWith("torch")
				&& !name.equals("block/unlit_torch");
		if (lantern || lanternItem) {
			vanilla = name.replace("unlit_", "");
		} else if (torch) {
			vanilla = name.equals("block/unlit_soul_torch") ? "block/soul_torch" : "block/copper_torch";
		} else if (name.equals("item/soul_fire_charge")) {
			vanilla = "item/fire_charge";
		} else if (name.equals("item/soul_golem_spawn_egg")) {
			vanilla = "item/snow_golem_spawn_egg";
		} else if (name.equals("entity/soul_golem")) {
			vanilla = "entity/snow_golem/snow_golem";
		}
		if (vanilla == null) {
			return;
		}
		var resource = resources.getResource(net.minecraft.resources.Identifier.withDefaultNamespace(
				"textures/" + vanilla + ".png")).orElseThrow();
		try (var stream = resource.open()) {
			var original = ImageIO.read(stream);
			if (original == null || original.getWidth() != image.getWidth()
					|| original.getHeight() < image.getHeight()) {
				throw new AssertionError("Vanilla reference dimensions changed: " + vanilla);
			}
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int expected = original.getRGB(x, y);
					int actual = image.getRGB(x, y);
					if ((expected >>> 24) != (actual >>> 24)) {
						throw new AssertionError("Variant changed vanilla transparency: " + name);
					}
					boolean flame = lantern && x >= 1 && x < 5 && y >= 3 && y < 8
							|| lanternItem && x >= 6 && x < 10 && y >= 8 && y < 13;
					if ((lantern || lanternItem) && !flame && expected != actual
							|| torch && y >= 8 && expected != actual) {
						throw new AssertionError("Variant changed vanilla frame or handle pixels: " + name);
					}
					if (flame && (actual >>> 24) != 0
							&& Math.max((actual >> 16) & 255, Math.max((actual >> 8) & 255, actual & 255)) >= 100) {
						throw new AssertionError("Unlit lantern still has bright flame pixels: " + name);
					}
				}
			}
		}
	}

	private static void testSoulAndLightModels(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -10 100 -10 10 100 10 minecraft:stone");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 101 5.5 180 0");
			world.getServer().runCommand("summon patchwork:soul_golem 0.5 101 0.5 {NoAI:1b}");
			world.getServer().runOnServer(server -> {
				var level = server.getPlayerList().getPlayers().getFirst().level();
				level.setBlockAndUpdate(new BlockPos(2, 101, 0), SoulFireSupport.chargeFire());
				if (!level.getBlockState(new BlockPos(2, 101, 0)).equals(SoulFireSupport.chargeFire())
						|| !level.getBlockState(new BlockPos(2, 100, 0)).is(Blocks.STONE)) {
					throw new AssertionError(
							"Server charge fire setup failed: " + level.getBlockState(new BlockPos(2, 101, 0)) + " on "
									+ level.getBlockState(new BlockPos(2, 100, 0)));
				}
			});
			world.getServer().runCommand("summon patchwork:soul_fireball -2.5 102.5 0.5 {acceleration_power:0.0d}");
			world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst()
					.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulGolems.SPAWN_EGG)));
			world.getConnection().waitForChunksRender();
			context.waitFor(client -> {
				boolean golem = false;
				boolean projectile = false;
				for (var entity : client.level.entitiesForRendering()) {
					if (entity instanceof SoulGolem) {
						golem = true;
					} else if (entity instanceof SoulFireball) {
						projectile = true;
					}
				}
				return golem && projectile;
			});
			context.waitFor(
					client -> client.level.getBlockState(new BlockPos(2, 101, 0)).equals(SoulFireSupport.chargeFire()));
			context.runOnClient(client -> {
				var models = client.getModelManager().getBlockStateModelSet();
				for (var state : Blocks.SOUL_FIRE.getStateDefinition().getPossibleStates()) {
					if (models.get(state) == models.missingModel()) {
						throw new AssertionError("Soul-fire state has no model: " + state);
					}
				}
				if (!client.level.getBlockState(new BlockPos(2, 101, 0)).equals(SoulFireSupport.chargeFire())
						|| !client.level.getBlockState(new BlockPos(2, 100, 0)).is(Blocks.STONE)) {
					throw new AssertionError(
							"Charge soul fire did not reach the client without changing its stone support");
				}
				var icon = client.getResourceManager().getResource(Patchwork.id("textures/item/soul_fire_charge.png"))
						.orElseThrow(() -> new AssertionError("Original Soul Fire Charge texture is missing"));
				try (var stream = icon.open()) {
					var image = ImageIO.read(stream);
					if (image == null || image.getWidth() != 16 || image.getHeight() != 16
							|| (image.getRGB(0, 0) >>> 24) != 0) {
						throw new AssertionError("Soul Fire Charge texture is not a transparent 16x16 icon");
					}
				} catch (IOException exception) {
					throw new AssertionError("Could not read the Soul Fire Charge texture", exception);
				}
				var lanterns = new ArrayList<Block>();
				lanterns.add(UnlitLanterns.LANTERN);
				lanterns.add(UnlitLanterns.SOUL_LANTERN);
				lanterns.addAll(UnlitLanterns.COPPER_LANTERN.asList());
				lanterns.add(Blocks.LANTERN);
				for (var lantern : lanterns) {
					var itemState = new ItemStackRenderState();
					client.getItemModelResolver().updateForLiving(itemState, new ItemStack(lantern.asItem()),
							ItemDisplayContext.NONE, client.player);
					if (itemState.isEmpty() || itemState.usesBlockLight()
							|| itemState.getModelBoundingBox().getZsize() > 0.07) {
						throw new AssertionError("Lantern did not use a flat generated item model: " + lantern);
					}
				}
				for (var collection : List.of(CopperTorches.LIT, CopperTorches.LIT_WALL, CopperTorches.UNLIT,
						CopperTorches.UNLIT_WALL)) {
					for (var torch : collection.asList()) {
						for (var state : torch.getStateDefinition().getPossibleStates()) {
							if (models.get(state) == models.missingModel()) {
								throw new AssertionError("Copper torch block model is missing: " + state);
							}
						}
						var itemState = new ItemStackRenderState();
						client.getItemModelResolver().updateForLiving(itemState, new ItemStack(torch.asItem()),
								ItemDisplayContext.GUI, client.player);
						if (itemState.isEmpty()) {
							throw new AssertionError("Copper torch item model is missing: " + torch);
						}
					}
				}
				var chargeState = new ItemStackRenderState();
				client.getItemModelResolver().updateForLiving(chargeState, new ItemStack(SoulFireCharges.ITEM),
						ItemDisplayContext.GUI, client.player);
				if (chargeState.isEmpty()) {
					throw new AssertionError("Soul Fire Charge item model is missing");
				}
				boolean found = false;
				boolean foundProjectile = false;
				for (var entity : client.level.entitiesForRendering()) {
					if (entity instanceof SoulFireball projectile) {
						foundProjectile = true;
						if (!(client.getEntityRenderDispatcher()
								.getRenderer(projectile) instanceof ThrownItemRenderer<?>)
								|| !projectile.getItem().is(SoulFireCharges.ITEM)) {
							throw new AssertionError("Soul Fire Charge projectile renderer or item is incorrect");
						}
						client.getEntityRenderDispatcher().extractEntity(projectile, 0.0F);
					}
					if (entity instanceof SoulGolem golem) {
						found = true;
						var renderer = client.getEntityRenderDispatcher().getRenderer(golem);
						if (!(renderer instanceof SoulGolemRenderer soulRenderer)) {
							throw new AssertionError("Soul Golem did not use its Snow Golem renderer");
						}
						var state = soulRenderer.createRenderState();
						soulRenderer.extractRenderState(golem, state, 0.0F);
						if (state.headBlock.isEmpty() || !soulRenderer.getTextureLocation(state)
								.equals(Patchwork.id("textures/entity/soul_golem.png"))) {
							throw new AssertionError("Soul Golem head or body texture is missing");
						}
					}
				}
				if (!found) {
					throw new AssertionError("Soul Golem disappeared before checking its render state");
				}
				if (!foundProjectile) {
					throw new AssertionError("Soul Fire Charge projectile did not reach the client");
				}
			});
			context.waitTicks(20);
			context.takeScreenshot("soul-golem");
			world.getServer().runCommand("data merge entity @e[type=patchwork:soul_golem,limit=1] {HasSoulLantern:0b}");
			world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst()
					.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulFireCharges.ITEM)));
			context.waitFor(client -> {
				for (var entity : client.level.entitiesForRendering()) {
					if (entity instanceof SoulGolem golem && !golem.hasLantern()) {
						if (!(client.getEntityRenderDispatcher()
								.getRenderer(golem) instanceof SoulGolemRenderer renderer)) {
							throw new AssertionError("Sheared Soul Golem renderer changed");
						}
						var state = renderer.createRenderState();
						renderer.extractRenderState(golem, state, 0.0F);
						if (!state.headBlock.isEmpty()) {
							throw new AssertionError("Sheared Soul Golem still renders its lantern head");
						}
						return true;
					}
				}
				return false;
			});
			context.waitTicks(10);
			context.takeScreenshot("sheared-soul-golem-and-soul-charge");
		}
	}

	private static void testPotionFirstFillTint(ClientGameTestContext context) {
		BlockPos pos = new BlockPos(0, 101, 0);
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -5 100 -5 5 100 5 minecraft:stone");
			world.getServer().runCommand("fill 0 101 3 0 102 3 minecraft:stone");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 103 3.5 180 50");
			world.getServer().runCommand("setblock 0 101 0 minecraft:cauldron");
			world.getConnection().waitForChunksRender();
			for (int color : new int[] { 0xFF0000, 0x00FF00 }) {
				world.getServer().runOnServer(server -> {
					var player = server.getPlayerList().getPlayers().getFirst();
					var level = player.level();
					level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
					var contents = new PotionContents(Optional.of(Potions.HEALING), Optional.of(color), List.of(),
							Optional.empty());
					ItemStack potion = new ItemStack(Items.POTION);
					potion.set(DataComponents.POTION_CONTENTS, contents);
					player.setItemInHand(InteractionHand.MAIN_HAND, potion);
					PotionCauldrons.pour(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
							potion);
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				});
				context.waitFor(client -> client.level.getBlockEntity(pos) instanceof PotionCauldronEntity cauldron
						&& cauldron.potion().getColor() == color
						&& client.level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL) == 1);
				// Let extraction submit the dirty section before waiting for its render tasks.
				context.waitTicks(2);
				world.getConnection().waitForChunksRender();
				assertRenderedPotionColor(context, color, "potion-first-fill-" + Integer.toHexString(color));
			}
			world.getServer().runOnServer(server -> {
				var player = server.getPlayerList().getPlayers().getFirst();
				if (!(player.level().getBlockEntity(pos) instanceof PotionCauldronEntity cauldron)) {
					throw new AssertionError("Potion cauldron disappeared before updating its contents");
				}
				cauldron.setPotion(new PotionContents(Optional.of(Potions.HEALING), Optional.of(0xFF0000), List.of(),
						Optional.empty()));
			});
			context.waitFor(client -> client.level.getBlockEntity(pos) instanceof PotionCauldronEntity cauldron
					&& cauldron.potion().getColor() == 0xFF0000);
			context.waitTicks(2);
			world.getConnection().waitForChunksRender();
			assertRenderedPotionColor(context, 0xFF0000, "potion-data-only-update");
		}
	}

	private static void assertRenderedPotionColor(ClientGameTestContext context, int color, String name) {
		var screenshot = context.takeScreenshot(name);
		try {
			var image = ImageIO.read(screenshot.toFile());
			if (image == null) {
				throw new AssertionError("Could not decode potion screenshot: " + screenshot);
			}
			int matchingPixels = 0;
			for (int y = 0; y < image.getHeight() * 3 / 4; y++) {
				for (int x = image.getWidth() / 4; x < image.getWidth() * 3 / 4; x++) {
					int pixel = image.getRGB(x, y);
					int red = (pixel >> 16) & 255;
					int green = (pixel >> 8) & 255;
					int blue = pixel & 255;
					if (color == 0xFF0000 ? red > 60 && red > green * 2 && red > blue * 2
							: green > 60 && green > red * 2 && green > blue * 2) {
						matchingPixels++;
					}
				}
			}
			if (matchingPixels < 100) {
				throw new AssertionError("Potion mesh did not show its current tint without a reload: " + matchingPixels
						+ " matching pixels in " + screenshot);
			}
		} catch (IOException exception) {
			throw new AssertionError("Could not read potion screenshot: " + screenshot, exception);
		}
	}
}
