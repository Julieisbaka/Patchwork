package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.CopperTorches;
import com.JulieISBaka.patchwork.GardenBlocks;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.UnlitLanterns;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Garden block models and supplied/reused artwork checks. */
final class PatchworkClientArtworkGameTests {
	private PatchworkClientArtworkGameTests() {
	}

	/** Runs this focused client GameTest group. */
	static void run(ClientGameTestContext context) {
		testGardenAndArtwork(context);
	}

	/** Checks garden models and supplied textures against their visual contracts. */
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
				for (String stage : List.of("copper", "exposed_copper", "weathered_copper", "oxidized_copper",
						"waxed_copper", "waxed_exposed_copper", "waxed_weathered_copper",
						"waxed_oxidized_copper")) {
					textures.add("item/" + stage + "_campfire");
					textures.add("item/unlit_" + stage + "_campfire");
				}
				for (String stage : List.of("copper", "exposed_copper", "weathered_copper", "oxidized_copper")) {
					textures.add("block/" + stage + "_campfire_fire");
				}
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
						if (name.startsWith("item/") && name.endsWith("_campfire")) {
							verifyCampfireInventorySprite(client.getResourceManager(), name, image);
						}
						if (name.equals("block/copper_campfire_fire")) {
							verifyCampfireOxidationTextures(client.getResourceManager());
						}
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
							for (int y = 36; y < 48; y++) {
								for (int x = 0; x < 48; x++) {
									if ((image.getRGB(x, y) >>> 24) != 255) {
										throw new AssertionError(
												"Soul Golem lower-body UV is transparent at " + x + "," + y);
									}
								}
							}
							for (int[] soulMark : new int[][] { { 13, 30 }, { 14, 30 }, { 24, 33 }, { 25, 33 } }) {
								if (image.getRGB(soulMark[0], soulMark[1]) != 0xFF52D3BE) {
									throw new AssertionError("Soul Golem base is missing its Soul Sand markings");
								}
							}
							for (int[] soulMark : new int[][] { { 13, 42 }, { 14, 42 }, { 24, 45 }, { 25, 45 } }) {
								if (image.getRGB(soulMark[0], soulMark[1]) != 0xFF52D3BE) {
									throw new AssertionError("Soul Golem lower body is missing its Soul Sand markings");
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

	/** Ensures each copper oxidation stage visibly changes the campfire flame color. */
	private static void verifyCampfireOxidationTextures(
			net.minecraft.server.packs.resources.ResourceManager resources) throws IOException {
		int[][] colors = new int[4][3];
		String[] stages = { "copper", "exposed_copper", "weathered_copper", "oxidized_copper" };
		for (int stage = 0; stage < stages.length; stage++) {
			var resource = resources.getResource(Patchwork.id(
					"textures/block/" + stages[stage] + "_campfire_fire.png")).orElseThrow();
			try (var stream = resource.open()) {
				var image = ImageIO.read(stream);
				if (image == null) {
					throw new AssertionError("Could not decode copper campfire flame texture: " + stages[stage]);
				}
				int count = 0;
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						int pixel = image.getRGB(x, y);
						if ((pixel >>> 24) != 0) {
							colors[stage][0] += (pixel >> 16) & 255;
							colors[stage][1] += (pixel >> 8) & 255;
							colors[stage][2] += pixel & 255;
							count++;
						}
					}
				}
				if (count == 0) {
					throw new AssertionError("Copper campfire flame texture is empty: " + stages[stage]);
				}
				for (int channel = 0; channel < 3; channel++) {
					colors[stage][channel] /= count;
				}
			}
		}
		for (int first = 0; first < colors.length; first++) {
			for (int second = first + 1; second < colors.length; second++) {
				int red = colors[first][0] - colors[second][0];
				int green = colors[first][1] - colors[second][1];
				int blue = colors[first][2] - colors[second][2];
				if (red * red + green * green + blue * blue < 400) {
					throw new AssertionError("Copper campfire oxidation stages share indistinguishable flame colors: "
							+ stages[first] + " and " + stages[second]);
				}
			}
		}
	}

	/** Checks custom copper campfire icons against vanilla's inventory sprite silhouette. */
	private static void verifyCampfireInventorySprite(
			net.minecraft.server.packs.resources.ResourceManager resources, String name,
			java.awt.image.BufferedImage image) throws IOException {
		var reference = resources.getResource(net.minecraft.resources.Identifier.withDefaultNamespace(
				"textures/item/campfire.png")).orElseThrow();
		boolean unlit = name.contains("/unlit_");
		try (var stream = reference.open()) {
			var original = ImageIO.read(stream);
			if (original == null || original.getWidth() != image.getWidth()
					|| original.getHeight() != image.getHeight()) {
				throw new AssertionError("Vanilla campfire item texture dimensions changed");
			}
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int expected = original.getRGB(x, y);
					int actual = image.getRGB(x, y);
					int red = (expected >> 16) & 255;
					int green = (expected >> 8) & 255;
					int blue = expected & 255;
					boolean flame = y <= 8 && (expected >>> 24) != 0 && red >= 150 && green >= 50 && blue <= 135
							&& red > green * 1.17 && green > blue * 1.15;
					if (flame && unlit) {
						if ((actual >>> 24) != 0) {
							throw new AssertionError("Unlit campfire icon retains flame pixels: " + name);
						}
					} else if (flame) {
						if ((actual >>> 24) != (expected >>> 24) || actual == expected) {
							throw new AssertionError("Copper campfire icon did not recolor vanilla flames: " + name);
						}
					} else if (actual != expected) {
						throw new AssertionError("Campfire icon changed vanilla log or transparent pixels: " + name);
					}
				}
			}
		}
	}

	/** Checks that copper oxidation changes only the torch flame and dims each stage. */
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

	/** Checks a supplied texture against its vanilla silhouette and unchanged regions. */
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
}
