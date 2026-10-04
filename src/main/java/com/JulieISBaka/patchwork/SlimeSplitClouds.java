package com.JulieISBaka.patchwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SlimeSplitClouds {
	private static final int LIFETIME_TICKS = 100;
	private static final DustParticleOptions SLIME_DUST = new DustParticleOptions(0x66C536, 1.2F);
	private static final Map<ServerLevel, List<Cloud>> CLOUDS = new HashMap<>();

	private SlimeSplitClouds() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(SlimeSplitClouds::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> CLOUDS.clear());
	}

	public static void spawn(Slime slime, ServerLevel level) {
		Vec3 from = slime.position().add(0.0, 0.5, 0.0);
		HitResult hit = level.clip(
			new ClipContext(from, from.add(0.0, -6.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, slime));
		if (!(hit instanceof BlockHitResult ground) || ground.getDirection() != Direction.UP) {
			return;
		}

		BlockPos origin = ground.getBlockPos().above();
		List<BlockPos> tiles = new ArrayList<>(9);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos tile = origin.offset(x, 0, z);
				if (level.getBlockState(tile).isAir()
					&& level.getBlockState(tile.below()).isFaceSturdy(level, tile.below(), Direction.UP)) {
					tiles.add(tile);
				}
			}
		}
		if (!tiles.isEmpty()) {
			CLOUDS.computeIfAbsent(level, unused -> new ArrayList<>())
				.add(new Cloud(List.copyOf(tiles), level.getGameTime() + LIFETIME_TICKS));
		}
	}

	private static void tick(ServerLevel level) {
		List<Cloud> clouds = CLOUDS.get(level);
		if (clouds == null) {
			return;
		}
		long time = level.getGameTime();
		clouds.removeIf(cloud -> cloud.expiresAt() <= time);
		if (clouds.isEmpty()) {
			CLOUDS.remove(level);
			return;
		}
		if (!PatchworkConfig.settings().slimeSplitClouds() || time % 5 != 0) {
			return;
		}
		for (Cloud cloud : clouds) {
			for (BlockPos tile : cloud.tiles()) {
				level.sendParticles(ParticleTypes.ITEM_SLIME, tile.getX() + 0.5, tile.getY() + 0.08, tile.getZ() + 0.5,
					2, 0.4, 0.02, 0.4, 0.01);
				level.sendParticles(SLIME_DUST, tile.getX() + 0.5, tile.getY() + 0.12, tile.getZ() + 0.5, 3, 0.42, 0.03,
					0.42, 0.005);
			}
			for (ServerPlayer player : level.players()) {
				if (!player.isSpectator() && cloud.tiles().stream()
					.anyMatch(tile -> player.getX() >= tile.getX() && player.getX() < tile.getX() + 1
						&& player.getZ() >= tile.getZ() && player.getZ() < tile.getZ() + 1
						&& Math.abs(player.getY() - tile.getY()) < 0.6)) {
					player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 1, false, false));
				}
			}
		}
	}

	private record Cloud(List<BlockPos> tiles, long expiresAt) {
	}
}
