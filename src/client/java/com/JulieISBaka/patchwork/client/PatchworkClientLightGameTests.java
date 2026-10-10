package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.CopperTorches;
import com.JulieISBaka.patchwork.LightVariants;
import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.SoulFireCharges;
import com.JulieISBaka.patchwork.SoulFireSupport;
import com.JulieISBaka.patchwork.SoulFireball;
import com.JulieISBaka.patchwork.SoulGolem;
import com.JulieISBaka.patchwork.SoulGolems;
import com.JulieISBaka.patchwork.UnlitLanterns;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Model and texture checks for light variants, Soul Golems, and Soul Fire Charges. */
final class PatchworkClientLightGameTests {
	private PatchworkClientLightGameTests() {
	}

	/** Runs this focused client GameTest group. */
	static void run(ClientGameTestContext context) {
		testSoulAndLightModels(context);
		testNewLightVariants(context);
	}

	/** Checks loaded models and render state for Soul features and lantern variants. */
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

	/** Checks copper and Soul light models and texture silhouettes. */
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
}
