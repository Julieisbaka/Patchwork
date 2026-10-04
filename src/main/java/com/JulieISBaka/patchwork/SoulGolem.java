package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
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

public class SoulGolem extends IronGolem {
	public SoulGolem(EntityType<? extends SoulGolem> type, Level level) {
		super(type, level);
		setPlayerCreated(true);
		setPersistenceRequired();
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			target.igniteForSeconds(2.0F);
		}
		return hit;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.GOLD_INGOT) || getHealth() >= getMaxHealth()) {
			return InteractionResult.PASS;
		}
		if (!level().isClientSide()) {
			heal(25.0F);
			stack.consume(1, player);
			playSound(SoundEvents.SOUL_SOIL_PLACE, 1.0F,
				1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
		}
		return InteractionResult.SUCCESS;
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
