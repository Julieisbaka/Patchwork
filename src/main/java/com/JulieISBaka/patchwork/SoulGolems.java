package com.JulieISBaka.patchwork;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Registration and construction logic for tameable Soul Golems. */
public final class SoulGolems {
	private static final ThreadLocal<Player> BUILDER = new ThreadLocal<>();
	private static final ResourceKey<EntityType<?>> TYPE_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
			Patchwork.id("soul_golem"));
	/** Registered Soul Golem entity type. */
	public static final EntityType<SoulGolem> TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, TYPE_KEY,
			EntityType.Builder.of(SoulGolem::new, MobCategory.MISC).sized(0.7F, 1.9F).eyeHeight(1.7F)
					.clientTrackingRange(10).noLootTable().build(TYPE_KEY));
	private static final ResourceKey<Item> EGG_KEY = ResourceKey.create(Registries.ITEM,
			Patchwork.id("soul_golem_spawn_egg"));
	/** Creative spawn egg for Soul Golems. */
	public static final Item SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, EGG_KEY,
			new SpawnEggItem(new Item.Properties().spawnEgg(TYPE).setId(EGG_KEY)));

	private SoulGolems() {
	}

	/** Registers the Soul Golem's attributes. */
	public static void register() {
		FabricDefaultAttributeRegistry.register(TYPE, IronGolem.createAttributes().add(Attributes.MAX_HEALTH, 30.0));
	}

	/** Associates a player with a block placement while allowing nested placements to restore their context. */
	public static <T> T withBuilder(Player player, Supplier<T> placement) {
		Player previous = BUILDER.get();
		BUILDER.set(player);
		try {
			return placement.get();
		} finally {
			if (previous == null) {
				BUILDER.remove();
			} else {
				BUILDER.set(previous);
			}
		}
	}

	/** Tries to create a Soul Golem from a valid soul-block structure beneath the supplied head block. */
	public static boolean trySpawn(Level level, BlockPos head) {
		BlockState lantern = level.getBlockState(head);
		if (!PatchworkConfig.settings().soulGolems() || !PatchworkConfig.settings().soulCopperLightBlocks()
				|| !(level instanceof ServerLevel serverLevel) || !lantern.is(PumpkinLanterns.SOUL_BLOCK)
				|| !isSoulBlock(level.getBlockState(head.below()))
				|| !isSoulBlock(level.getBlockState(head.below(2)))) {
			return false;
		}
		SoulGolem golem = new SoulGolem(TYPE, level);
		if (BUILDER.get() != null) {
			golem.setOwner(BUILDER.get());
		}
		BlockPos base = head.below(2);
		golem.snapTo(base.getX() + 0.5, base.getY() + 0.05, base.getZ() + 0.5,
				lantern.getValue(CarvedPumpkinBlock.FACING).toYRot(), 0.0F);
		if (!serverLevel.addFreshEntity(golem)) {
			throw new IllegalStateException("Could not spawn Soul Golem at " + base);
		}
		if (BUILDER.get() != null) {
			Patchwork.awardAdvancement(BUILDER.get(), "adventure/a_soulful_companion", "summoned_soul_golem");
		}
		for (BlockPos pos : BlockPos.betweenClosed(base, head)) {
			BlockState state = level.getBlockState(pos);
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			level.levelEvent(2001, pos, Block.getId(state));
		}
		for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class,
				golem.getBoundingBox().inflate(5.0))) {
			CriteriaTriggers.SUMMONED_ENTITY.trigger(player, golem);
		}
		return true;
	}

	private static boolean isSoulBlock(BlockState state) {
		return state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL);
	}
}
