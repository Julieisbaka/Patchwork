package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Objects;

public class SoulGolem extends IronGolem implements OwnableEntity, RangedAttackMob, Shearable {
	private static final EntityDataAccessor<Boolean> HAS_LANTERN = SynchedEntityData.defineId(SoulGolem.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> HAS_OWNER = SynchedEntityData.defineId(SoulGolem.class,
			EntityDataSerializers.BOOLEAN);
	private EntityReference<LivingEntity> owner;

	public SoulGolem(EntityType<? extends SoulGolem> type, Level level) {
		super(type, level);
		setPlayerCreated(true);
		setPersistenceRequired();
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		return false;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal || goal instanceof MoveTowardsTargetGoal);
		goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0,
				NumericSetting.SOUL_GOLEM_ATTACK_INTERVAL.intValue(), NumericSetting.SOUL_GOLEM_ATTACK_RANGE.floatValue()));
		targetSelector.removeAllGoals(goal -> true);
		targetSelector.addGoal(0, new OwnerCombatGoal(true));
		targetSelector.addGoal(1, new OwnerCombatGoal(false));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true,
				(target, level) -> !(target instanceof Creeper)));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HAS_LANTERN, true);
		builder.define(HAS_OWNER, false);
	}

	@Override
	public EntityReference<LivingEntity> getOwnerReference() {
		return owner;
	}

	public void setOwner(Player player) {
		owner = EntityReference.of(player);
		entityData.set(HAS_OWNER, true);
	}

	public boolean hasLantern() {
		return entityData.get(HAS_LANTERN);
	}

	public boolean isProtected(LivingEntity target) {
		EntityReference<LivingEntity> ownerReference = getOwnerReference();
		if (target == this || (ownerReference != null && ownerReference.matches(target)) || isAlliedTo(target)) {
			return true;
		}
		LivingEntity builder = getOwner();
		if (builder != null && builder.isAlliedTo(target)) {
			return true;
		}
		if (ownerReference != null && target instanceof OwnableEntity pet) {
			EntityReference<LivingEntity> petOwner = pet.getOwnerReference();
			return petOwner != null && Objects.equals(petOwner.getUUID(), ownerReference.getUUID());
		}
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (isProtected(target)) {
			return false;
		}
		// Players and Creepers are eligible only when involved in the owner's combat.
		if (target instanceof Player || target instanceof Creeper) {
			LivingEntity builder = getOwner();
			return builder != null && target.isAlive() && !target.isSpectator()
					&& !(target instanceof Player player && player.isCreative())
					&& (builder.getLastHurtByMob() == target || builder.getLastHurtMob() == target);
		}
		return super.canAttack(target);
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (PatchworkConfig.settings().soulGolems() && PatchworkConfig.settings().soulFireCharges()
				&& PatchworkConfig.settings().soulGolemProjectiles() && level() instanceof ServerLevel level
				&& canAttack(target) && getSensing().hasLineOfSight(target)) {
			Vec3 origin = getEyePosition().add(0, -0.1, 0);
			SoulFireCharges.shoot(level, this, origin, target.getEyePosition().subtract(origin));
			playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		EntityReference.store(owner, output, "Owner");
		output.putBoolean("HasSoulLantern", hasLantern());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		owner = EntityReference.read(input, "Owner");
		entityData.set(HAS_OWNER, owner != null);
		entityData.set(HAS_LANTERN, input.getBooleanOr("HasSoulLantern", true));
		setPlayerCreated(true);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(Items.SHEARS) && readyForShearing()) {
			if (level() instanceof ServerLevel level) {
				shear(level, SoundSource.PLAYERS, stack);
				gameEvent(GameEvent.SHEAR, player);
				stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
			}
			return InteractionResult.SUCCESS;
		}
		if (!stack.is(Items.GOLD_INGOT) || (entityData.get(HAS_OWNER) && getHealth() >= getMaxHealth())) {
			return InteractionResult.PASS;
		}
		if (!level().isClientSide()) {
			if (owner == null) {
				setOwner(player);
			}
			heal(NumericSetting.SOUL_GOLEM_HEALING.floatValue());
			stack.consume(1, player);
			playSound(SoundEvents.SOUL_SOIL_PLACE, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean readyForShearing() {
		return isAlive() && hasLantern();
	}

	@Override
	public void shear(ServerLevel level, SoundSource source, ItemStack shears) {
		if (!readyForShearing()) {
			return;
		}
		entityData.set(HAS_LANTERN, false);
		level.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, source, 1.0F, 1.0F);
		spawnAtLocation(level, new ItemStack(PumpkinLanterns.SOUL_ITEM));
	}

	private final class OwnerCombatGoal extends TargetGoal {
		private final boolean defend;
		private LivingEntity candidate;
		private int timestamp = -1;

		OwnerCombatGoal(boolean defend) {
			super(SoulGolem.this, false);
			this.defend = defend;
			setFlags(EnumSet.of(Flag.TARGET));
		}

		@Override
		public boolean canUse() {
			LivingEntity builder = getOwner();
			if (builder == null) {
				return false;
			}
			candidate = defend ? builder.getLastHurtByMob() : builder.getLastHurtMob();
			int current = defend ? builder.getLastHurtByMobTimestamp() : builder.getLastHurtMobTimestamp();
			return current != timestamp && candidate != null && SoulGolem.this.canAttack(candidate)
					&& canAttack(candidate, TargetingConditions.DEFAULT);
		}

		@Override
		public void start() {
			setTarget(candidate);
			LivingEntity builder = getOwner();
			if (builder != null) {
				timestamp = defend ? builder.getLastHurtByMobTimestamp() : builder.getLastHurtMobTimestamp();
			}
			super.start();
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SOUL_SAND_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SOUL_ESCAPE.value();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.SOUL_SOIL_STEP, 1.0F, 1.0F);
	}

	@Override
	public void playSound(SoundEvent sound, float volume, float pitch) {
		if (sound == SoundEvents.IRON_GOLEM_ATTACK) {
			sound = SoundEvents.FIRECHARGE_USE;
		} else if (sound == SoundEvents.IRON_GOLEM_DAMAGE) {
			sound = SoundEvents.SOUL_SAND_BREAK;
		}
		super.playSound(sound, volume, pitch);
	}
}
