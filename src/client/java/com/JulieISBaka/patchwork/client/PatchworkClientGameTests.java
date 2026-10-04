package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.SoulGolem;
import com.JulieISBaka.patchwork.SoulGolems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PatchworkClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -10 100 -10 10 100 10 minecraft:stone");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("weather clear");
			world.getServer().runCommand("tp @a 0.5 101 5.5 180 0");
			world.getServer().runCommand("summon patchwork:soul_golem 0.5 101 0.5 {NoAI:1b}");
			world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().getFirst()
				.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SoulGolems.SPAWN_EGG)));
			world.getConnection().waitForChunksRender();
			context.waitFor(client -> {
				for (var entity : client.level.entitiesForRendering()) {
					if (entity instanceof SoulGolem) {
						return true;
					}
				}
				return false;
			});
			context.runOnClient(client -> {
				boolean found = false;
				for (var entity : client.level.entitiesForRendering()) {
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
			});
			context.waitTicks(20);
			context.takeScreenshot("soul-golem");
		}
	}
}
