package com.JulieISBaka.patchwork.client;

import com.JulieISBaka.patchwork.RabbitPet;
import com.JulieISBaka.patchwork.client.mixin.LivingEntityRendererAccessor;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Client synchronization and rendering checks for pets. */
final class PatchworkClientPetGameTests {
	private PatchworkClientPetGameTests() {
	}

	/** Runs this focused client GameTest group. */
	static void run(ClientGameTestContext context) {
		testPetsAndRendererMigration(context);
	}

	/** Checks synchronized rabbit pet state and the wolf-banner renderer layer. */
	private static void testPetsAndRendererMigration(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getServer().runCommand("fill -8 100 -8 8 100 8 minecraft:grass_block");
			world.getServer().runCommand("setblock 0 102 0 minecraft:stone");
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("tp @a 0.5 101 5.5 180 15");
			var rabbitId = new java.util.concurrent.atomic.AtomicInteger();
			var wolfId = new java.util.concurrent.atomic.AtomicInteger();
			var spiderId = new java.util.concurrent.atomic.AtomicInteger();
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
				var spider = java.util.Objects.requireNonNull(net.minecraft.world.entity.EntityTypes.SPIDER
						.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND));
				spider.snapTo(0.5, 101.1, 0.5, 0, 0);
				level.addFreshEntity(spider);
				spiderId.set(spider.getId());
			});
			context.waitFor(client -> client.level.getEntity(rabbitId.get()) instanceof RabbitPet pet
					&& pet.patchwork$isOwnedBy(client.player) && pet.patchwork$isOrderedToStay()
					&& client.level.getEntity(wolfId.get()) instanceof net.minecraft.world.entity.animal.wolf.Wolf wolf
					&& wolf.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.BANNER.white())
					&& client.level.getEntity(spiderId.get()) instanceof net.minecraft.world.entity.monster.spider.Spider);
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
				var spider = (Spider) client.level.getEntity(spiderId.get());
				@SuppressWarnings("unchecked")
				SpiderRenderer<Spider> spiderRenderer = (SpiderRenderer<Spider>) client.getEntityRenderDispatcher()
						.getRenderer(spider);
				var spiderState = spiderRenderer.createRenderState();
				spiderRenderer.extractRenderState(spider, spiderState, 0.0F);
				if (!spiderState.isUpsideDown) {
					throw new AssertionError("Ceiling-clinging spider model was not rendered upside down");
				}
			});
			world.getConnection().waitForChunksRender();
			context.takeScreenshot("patchwork-tamed-rabbit-and-wolf-banner");
		}
	}
}
