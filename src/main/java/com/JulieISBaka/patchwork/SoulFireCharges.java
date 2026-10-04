package com.JulieISBaka.patchwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SoulFireCharges {
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM,
		Patchwork.id("soul_fire_charge"));
	public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
		new FireChargeItem(new Item.Properties().setId(ITEM_KEY)) {
			@Override
			public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
				SoulFireball projectile = new SoulFireball(PROJECTILE, level);
				projectile.setPos(pos.x(), pos.y(), pos.z());
				projectile.setItem(stack);
				projectile.accelerationPower = 0.03;
				projectile.setDeltaMovement(
					new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ()).scale(0.65));
				return projectile;
			}

			@Override
			public InteractionResult useOn(UseOnContext context) {
				BlockPos pos = context.getClickedPos();
				BlockState state = context.getLevel().getBlockState(pos);
				boolean light = CampfireBlock.canLight(state) || CandleBlock.canLight(state)
					|| CandleCakeBlock.canLight(state);
				BlockPos firePos = pos.relative(context.getClickedFace());
				if (!light && !canPlaceFire(context.getLevel(), firePos)) {
					return InteractionResult.FAIL;
				}
				if (!context.getLevel().isClientSide()) {
					if (light) {
						context.getLevel().setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
					} else {
						context.getLevel().setBlockAndUpdate(firePos, SoulFireSupport.chargeFire());
					}
					context.getLevel().playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
					context.getLevel().gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, light ? pos : firePos);
					context.getItemInHand().consume(1, context.getPlayer());
				}
				return InteractionResult.SUCCESS;
			}
		});
	private static final ResourceKey<EntityType<?>> PROJECTILE_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
		Patchwork.id("soul_fireball"));
	public static final EntityType<SoulFireball> PROJECTILE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
		PROJECTILE_KEY, EntityType.Builder.<SoulFireball>of(SoulFireball::new, MobCategory.MISC).sized(0.3125F, 0.3125F)
			.clientTrackingRange(4).updateInterval(10).build(PROJECTILE_KEY));

	private SoulFireCharges() {
	}

	public static void register() {
		DispenserBlock.registerProjectileBehavior(ITEM);
	}

	public static SoulFireball shoot(ServerLevel level, LivingEntity shooter, Vec3 origin, Vec3 direction) {
		SoulFireball projectile = new SoulFireball(PROJECTILE, level);
		projectile.setOwner(shooter);
		projectile.setPos(origin);
		projectile.accelerationPower = 0.03;
		projectile.setDeltaMovement(direction.normalize().scale(0.65));
		projectile.setItem(new ItemStack(ITEM));
		if (!level.addFreshEntity(projectile)) {
			throw new IllegalStateException("Could not spawn Soul Fire Charge at " + origin);
		}
		return projectile;
	}

	public static InteractionResult throwCharge(Player player, Level level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!PatchworkConfig.settings().throwableFireCharges() || (!stack.is(Items.FIRE_CHARGE) && !stack.is(ITEM))) {
			return InteractionResult.PASS;
		}
		if (player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.CONSUME;
		}
		Vec3 eye = player.getEyePosition();
		Vec3 direction = player.getLookAngle();
		if (level.clip(new ClipContext(eye, eye.add(direction.scale(5.0)), ClipContext.Block.OUTLINE,
			ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) {
			return InteractionResult.CONSUME;
		}
		if (level instanceof ServerLevel serverLevel) {
			if (stack.is(ITEM)) {
				shoot(serverLevel, player, eye.add(direction), direction);
			} else {
				LargeFireball fireball = new LargeFireball(serverLevel, player, direction, 0);
				fireball.accelerationPower = 0.03;
				fireball.setPos(eye.add(direction));
				fireball.setDeltaMovement(direction.scale(0.65));
				fireball.addTag(Patchwork.THROWN_FIRE_CHARGE_TAG);
				fireball.setItem(stack);
				if (!serverLevel.addFreshEntity(fireball)) {
					throw new IllegalStateException("Could not spawn Fire Charge at " + eye);
				}
			}
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE,
				SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		player.getCooldowns().addCooldown(stack, 30);
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	public static boolean canPlaceFire(Level level, BlockPos pos) {
		return level.getBlockState(pos).isAir() && SoulFireSupport.chargeFire().canSurvive(level, pos);
	}

	public static void ignite(Level level, BlockHitResult hit) {
		BlockPos struck = hit.getBlockPos();
		BlockState state = level.getBlockState(struck);
		if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
			level.setBlockAndUpdate(struck, state.setValue(BlockStateProperties.LIT, true));
			return;
		}
		BlockPos pos = struck.relative(hit.getDirection());
		if (!canPlaceFire(level, pos) && hit.getDirection().getAxis().isHorizontal()) {
			pos = struck.above();
		}
		if (canPlaceFire(level, pos)) {
			level.setBlockAndUpdate(pos, SoulFireSupport.chargeFire());
		}
	}
}
