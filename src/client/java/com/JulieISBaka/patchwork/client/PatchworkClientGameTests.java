package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.Patchwork;
import com.JulieISBaka.patchwork.SoulGolem;
import com.JulieISBaka.patchwork.SoulGolems;
import com.JulieISBaka.patchwork.SoulFireCharges;
import com.JulieISBaka.patchwork.SoulFireball;
import com.JulieISBaka.patchwork.UnlitLanterns;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
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
			world.getServer().runCommand("setblock 2 101 0 patchwork:supported_soul_fire");
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
			context.runOnClient(client -> {
				var lanterns = new java.util.ArrayList<net.minecraft.world.level.block.Block>();
				lanterns.add(UnlitLanterns.LANTERN);
				lanterns.add(UnlitLanterns.SOUL_LANTERN);
				lanterns.addAll(UnlitLanterns.COPPER_LANTERN.asList());
				lanterns.add(Blocks.LANTERN);
				for (var lantern : lanterns) {
					var itemState = new ItemStackRenderState();
					client.getItemModelResolver().updateForLiving(itemState, new ItemStack(lantern.asItem()),
						ItemDisplayContext.NONE, client.player);
					if (itemState.isEmpty() || itemState.usesBlockLight() || itemState.getModelBoundingBox().getZsize() > 0.07) {
						throw new AssertionError("Lantern did not use a flat generated item model: " + lantern);
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
						if (!(client.getEntityRenderDispatcher().getRenderer(projectile) instanceof ThrownItemRenderer<?>)
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
				.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(UnlitLanterns.LANTERN.asItem())));
			context.waitFor(client -> {
				for (var entity : client.level.entitiesForRendering()) {
					if (entity instanceof SoulGolem golem && !golem.hasLantern()) {
						if (!(client.getEntityRenderDispatcher().getRenderer(golem) instanceof SoulGolemRenderer renderer)) {
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
			context.takeScreenshot("sheared-soul-golem-and-unlit-lantern");
		}
	}
}
