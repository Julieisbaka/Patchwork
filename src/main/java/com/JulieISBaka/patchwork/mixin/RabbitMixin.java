package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.RabbitPet;
import com.JulieISBaka.patchwork.RabbitPets;
import java.util.Optional;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Rabbit.class)
public abstract class RabbitMixin extends Animal implements RabbitPet {
	@Unique
	private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> PATCHWORK_OWNER =
			SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
	@Unique
	private static final EntityDataAccessor<Boolean> PATCHWORK_STAY =
			SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.BOOLEAN);

	protected RabbitMixin(EntityType<? extends Animal> type, Level level) {
		super(type, level);
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void patchwork$definePetData(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(PATCHWORK_OWNER, Optional.empty());
		builder.define(PATCHWORK_STAY, false);
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void patchwork$registerPetGoals(CallbackInfo ci) {
		Rabbit rabbit = (Rabbit) (Object) this;
		goalSelector.addGoal(0, new RabbitPets.StayGoal(rabbit, this));
		goalSelector.addGoal(4, new RabbitPets.FollowGoal(rabbit, this));
	}

	@Inject(method = "startJumping", at = @At("HEAD"), cancellable = true)
	private void patchwork$stayOnGround(CallbackInfo ci) {
		if (patchwork$isOrderedToStay() && onGround() && !isInWater()) {
			ci.cancel();
		}
	}

	@Override
	public EntityReference<LivingEntity> getOwnerReference() {
		return entityData.get(PATCHWORK_OWNER).orElse(null);
	}

	@Override
	public boolean patchwork$isOrderedToStay() {
		return entityData.get(PATCHWORK_STAY);
	}

	@Override
	public void patchwork$setOrderedToStay(boolean stay) {
		entityData.set(PATCHWORK_STAY, getOwnerReference() != null && stay);
		getNavigation().stop();
		((Rabbit) (Object) this).setSpeedModifier(0);
		setJumping(false);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		var stack = player.getItemInHand(hand);
		Rabbit rabbit = (Rabbit) (Object) this;
		if (rabbit.getVariant() != Rabbit.Variant.EVIL && getOwnerReference() == null && stack.is(Items.CARROT)) {
			if (level() instanceof ServerLevel level) {
				entityData.set(PATCHWORK_OWNER, Optional.of(EntityReference.of(player)));
				setPersistenceRequired();
				getNavigation().stop();
				stack.consume(1, player);
				level.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.5, getZ(), 7, 0.3, 0.3, 0.3, 0);
				if (player instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
				}
			}
			return InteractionResult.SUCCESS;
		}
		if (patchwork$isOwnedBy(player) && stack.isEmpty()) {
			if (!level().isClientSide()) {
				patchwork$setOrderedToStay(!patchwork$isOrderedToStay());
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void patchwork$savePet(ValueOutput output, CallbackInfo ci) {
		EntityReference.store(getOwnerReference(), output, "PatchworkOwner");
		output.putBoolean("PatchworkStay", patchwork$isOrderedToStay());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void patchwork$loadPet(ValueInput input, CallbackInfo ci) {
		EntityReference<LivingEntity> owner = EntityReference.read(input, "PatchworkOwner");
		entityData.set(PATCHWORK_OWNER, Optional.ofNullable(owner));
		patchwork$setOrderedToStay(input.getBooleanOr("PatchworkStay", false));
		if (owner != null) {
			setPersistenceRequired();
		}
	}
}
