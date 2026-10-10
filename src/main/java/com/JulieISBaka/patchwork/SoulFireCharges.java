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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
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

/** Registration and gameplay helpers for Soul Fire Charges and their projectiles. */
public final class SoulFireCharges {
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM,
			Patchwork.id("soul_fire_charge"));
	/** Registered Soul Fire Charge item. */
	public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
			new FireChargeItem(new Item.Properties().setId(ITEM_KEY)) {
				@Override
				public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
					SoulFireball projectile = new SoulFireball(PROJECTILE, level);
					projectile.setPos(pos.x(), pos.y(), pos.z());
					projectile.setItem(stack);
					projectile.accelerationPower = NumericSetting.FIRE_CHARGE_ACCELERATION.get();
					projectile.setDeltaMovement(
							new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
									.scale(NumericSetting.FIRE_CHARGE_SPEED.get()));
					return projectile;
				}

				@Override
				public InteractionResult useOn(UseOnContext context) {
					if (!PatchworkConfig.settings().soulFireCharges()) {
						return InteractionResult.PASS;
					}
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
							Direction supportDirection = context.getClickedFace().getAxis().isHorizontal()
									? context.getClickedFace().getOpposite()
									: Direction.UP;
							context.getLevel().setBlockAndUpdate(firePos,
									SoulFireSupport.chargeFire(supportDirection));
						}
						context.getLevel().playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F,
								1.0F);
						context.getLevel().gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, light ? pos : firePos);
						context.getItemInHand().consume(1, context.getPlayer());
					}
					return InteractionResult.SUCCESS;
				}
			});
	private static final ResourceKey<EntityType<?>> PROJECTILE_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
			Patchwork.id("soul_fireball"));
	/** Registered entity type for Soul Fire Charge projectiles. */
	public static final EntityType<SoulFireball> PROJECTILE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
			PROJECTILE_KEY,
			EntityType.Builder.<SoulFireball>of(SoulFireball::new, MobCategory.MISC).sized(0.3125F, 0.3125F)
					.clientTrackingRange(4).updateInterval(10).build(PROJECTILE_KEY));

	private SoulFireCharges() {
	}

	/** Registers dispenser behavior when the corresponding configuration switches are enabled. */
	public static void register() {
		if (PatchworkConfig.settings().soulFireCharges()
				&& PatchworkConfig.settings().soulFireChargeDispenserProjectiles()) {
			DispenserBlock.registerProjectileBehavior(ITEM);
		}
	}

	/** Fires a Soul Fire Charge projectile from an entity at the requested origin and direction. */
	public static SoulFireball shoot(ServerLevel level, LivingEntity shooter, Vec3 origin, Vec3 direction) {
		if (!PatchworkConfig.settings().soulFireCharges()) {
			throw new IllegalStateException("Soul Fire Charges are disabled");
		}
		SoulFireball projectile = new SoulFireball(PROJECTILE, level);
		projectile.setOwner(shooter);
		projectile.setPos(origin);
		projectile.accelerationPower = NumericSetting.FIRE_CHARGE_ACCELERATION.get();
		projectile.setDeltaMovement(direction.normalize().scale(NumericSetting.FIRE_CHARGE_SPEED.get()));
		projectile.setItem(new ItemStack(ITEM));
		if (!level.addFreshEntity(projectile)) {
			throw new IllegalStateException("Could not spawn Soul Fire Charge at " + origin);
		}
		return projectile;
	}

	/** Throws a held Soul Fire Charge when player throwing is enabled. */
	public static InteractionResult throwCharge(Player player, Level level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!PatchworkConfig.settings().throwableFireCharges()
				|| (stack.is(ITEM) && !PatchworkConfig.settings().soulFireCharges())
				|| (!stack.is(Items.FIRE_CHARGE) && !stack.is(ITEM))) {
			return InteractionResult.PASS;
		}
		if (player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.CONSUME;
		}
		Vec3 eye = player.getEyePosition();
		Vec3 direction = player.getLookAngle();
		if (level.clip(new ClipContext(eye, eye.add(direction.scale(NumericSetting.FIRE_CHARGE_CLEARANCE.get())), ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) {
			return InteractionResult.CONSUME;
		}
		if (level instanceof ServerLevel serverLevel) {
			if (stack.is(ITEM)) {
				shoot(serverLevel, player, eye.add(direction), direction);
			} else {
				LargeFireball fireball = new LargeFireball(serverLevel, player, direction, 0);
				fireball.accelerationPower = NumericSetting.FIRE_CHARGE_ACCELERATION.get();
				fireball.setPos(eye.add(direction));
				fireball.setDeltaMovement(direction.scale(NumericSetting.FIRE_CHARGE_SPEED.get()));
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
		player.getCooldowns().addCooldown(stack, NumericSetting.FIRE_CHARGE_COOLDOWN.intValue());
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	/** Returns whether a Soul Fire Charge can place Soul Fire at the given position. */
	public static boolean canPlaceFire(Level level, BlockPos pos) {
		return level.getBlockState(pos).isAir() && SoulFireSupport.chargeFire().canSurvive(level, pos);
	}

	/** Applies Soul Fire Charge effects at a block impact location. */
	public static void ignite(Level level, BlockHitResult hit) {
		BlockPos struck = hit.getBlockPos();
		BlockState state = level.getBlockState(struck);
		if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
			level.setBlockAndUpdate(struck, state.setValue(BlockStateProperties.LIT, true));
			return;
		}
		BlockPos pos = struck.relative(hit.getDirection());
		Direction supportDirection = hit.getDirection().getAxis().isHorizontal()
				? hit.getDirection().getOpposite()
				: Direction.UP;
		if (!canPlaceFire(level, pos) && hit.getDirection().getAxis().isHorizontal()) {
			pos = struck.above();
			supportDirection = Direction.UP;
		}
		if (canPlaceFire(level, pos)) {
			level.setBlockAndUpdate(pos, SoulFireSupport.chargeFire(supportDirection));
		}
	}
}
