package com.JulieISBaka.patchwork;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Patchwork implements ModInitializer {
	public static final String MOD_ID = "patchwork";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final int SLIMEBALL_COOLDOWN_TICKS = 20;
	private static final ResourceConditionType<ChainmailCondition> CHAINMAIL_CONDITION =
		ResourceConditionType.create(id("chainmail_recipes"), MapCodec.unit(new ChainmailCondition()));

	private record ChainmailCondition() implements ResourceCondition {
		@Override
		public ResourceConditionType<?> getType() {
			return CHAINMAIL_CONDITION;
		}

		@Override
		public boolean test(net.minecraft.resources.RegistryOps.RegistryInfoLookup registries) {
			return PatchworkConfig.settings().chainmailRecipes();
		}
	}

	@Override
	public void onInitialize() {
		PatchworkConfig.load();
		ResourceConditions.register(CHAINMAIL_CONDITION);
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!PatchworkConfig.settings().beesDefendFlowers() || !(level instanceof ServerLevel serverLevel)
				|| !state.is(BlockTags.FLOWERS)) {
				return;
			}
			for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-4, -4, -4), pos.offset(4, 4, 4))) {
				if (nearby.distSqr(pos) > 16.0 || !serverLevel.getBlockState(nearby).is(BlockTags.BEEHIVES)
					|| !(serverLevel.getBlockEntity(nearby) instanceof BeehiveBlockEntity hive)
					|| hive.isEmpty() || hive.isSedated()) {
					continue;
				}
				hive.emptyAllLivingFromHive(player, serverLevel.getBlockState(nearby),
					BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			}
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!PatchworkConfig.settings().throwableSlimeballs() || !stack.is(Items.SLIME_BALL)) {
				return InteractionResult.PASS;
			}
			if (player.getCooldowns().isOnCooldown(stack)) {
				return InteractionResult.CONSUME;
			}

			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
				SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
			if (level instanceof ServerLevel serverLevel) {
				Projectile.spawnProjectileFromRotation(Snowball::new, serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
			}

			player.awardStat(Stats.ITEM_USED.get(Items.SLIME_BALL));
			player.getCooldowns().addCooldown(stack, SLIMEBALL_COOLDOWN_TICKS);
			stack.consume(1, player);
			return InteractionResult.SUCCESS;
		});
		LOGGER.info("Patchwork initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
