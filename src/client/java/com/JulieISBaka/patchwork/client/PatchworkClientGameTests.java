package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.CopperTorches;
import com.JulieISBaka.patchwork.GardenBlocks;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.PotionCauldronEntity;
import com.JulieISBaka.patchwork.PotionCauldrons;
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
		testPotionFirstFillTint(context);
		testSoulAndLightModels(context);
		testGardenAndArtwork(context);
	}

	private static void testGardenAndArtwork(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -10 100 -10 10 100 10 minecraft:grass_block");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 101 5.5 180 15");
			world.getServer().runCommand("setblock -2 101 0 patchwork:wax_block");
			world.getServer().runCommand("setblock 0 101 0 patchwork:paeonia");
			world.getServer().runCommand("setblock 2 101 0 patchwork:potted_paeonia");
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
						"block/soul_jack_o_lantern", "item/soul_golem_spawn_egg"));
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
						if ((name.startsWith("item/") || name.equals("block/paeonia"))
								&& (image.getRGB(0, 0) >>> 24) != 0) {
							throw new AssertionError("Icon/flower lost its transparent background: " + name);
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
			context.takeScreenshot("wax-block-and-paeonia");
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
