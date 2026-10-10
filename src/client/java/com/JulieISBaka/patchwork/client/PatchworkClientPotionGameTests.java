package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.PotionCauldronEntity;
import com.JulieISBaka.patchwork.PotionCauldrons;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;

/** Rendered potion-cauldron tint regression checks. */
final class PatchworkClientPotionGameTests {
	private PatchworkClientPotionGameTests() {
	}

	/** Runs this focused client GameTest group. */
	static void run(ClientGameTestContext context) {
		testPotionFirstFillTint(context);
	}

	/** Checks first-fill and data-only updates in rendered potion cauldrons. */
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

	/** Asserts that a screenshot contains enough pixels matching a potion tint. */
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
