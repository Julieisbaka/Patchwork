package com.JulieISBaka.patchwork;

import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gameevent.GameEvent;

public final class AnimalFoodGoal extends Goal {
	private static final double SEARCH_RADIUS = 8.0;
	private static final double EAT_DISTANCE_SQR = 2.25;
	private final Animal animal;
	private ItemEntity food;
	private int nextSearchTick;
	private int nextEatTick;

	public AnimalFoodGoal(Animal animal) {
		this.animal = animal;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!this.canEat() || this.animal.tickCount < this.nextEatTick
				|| this.animal.tickCount < this.nextSearchTick) {
			return false;
		}
		this.nextSearchTick = this.animal.tickCount + 10;
		this.food = this.animal.level().getEntitiesOfClass(ItemEntity.class,
				this.animal.getBoundingBox().inflate(SEARCH_RADIUS), this::isEdible).stream()
				.min(Comparator.comparingDouble(this.animal::distanceToSqr)).orElse(null);
		return this.food != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.canEat() && this.isEdible(this.food)
				&& this.animal.distanceToSqr(this.food) <= SEARCH_RADIUS * SEARCH_RADIUS;
	}

	@Override
	public void start() {
		this.moveToFood();
	}

	@Override
	public void tick() {
		if (this.animal.distanceToSqr(this.food) <= EAT_DISTANCE_SQR) {
			this.eat();
		} else if (this.animal.tickCount % 10 == 0) {
			this.moveToFood();
		}
	}

	@Override
	public void stop() {
		this.food = null;
		this.animal.getNavigation().stop();
	}

	private boolean canEat() {
		return this.animal.isAlive() && this.animal.level() instanceof ServerLevel serverLevel
				&& serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
	}

	private boolean isEdible(ItemEntity item) {
		return item != null && item.isAlive() && !item.hasPickUpDelay()
				&& this.animal.isFood(item.getItem());
	}

	private void moveToFood() {
		if (this.food != null) {
			this.animal.getNavigation().moveTo(this.food, 1.1);
		}
	}

	private void eat() {
		var thrower = this.food.getOwner();
		ItemStack stack = this.food.getItem();
		stack.shrink(1);
		if (stack.isEmpty()) {
			this.food.discard();
		} else {
			this.food.setItem(stack);
		}
		if (this.animal.isBaby() && this.animal.canAgeUp()) {
			this.animal.ageUp(Animal.getSpeedUpSecondsWhenFeeding(this.animal.getAge()), true);
		} else if (this.animal.canFallInLove()) {
			this.animal.setInLove(null);
		}
		this.animal.level().gameEvent(GameEvent.EAT, this.animal.position(), GameEvent.Context.of(this.animal));
		if (thrower instanceof ServerPlayer player) {
			Patchwork.awardAdvancement(player, "adventure/creature_comforts", "animal_ate_dropped_food");
		}
		this.nextEatTick = this.animal.tickCount + 40;
		this.food = null;
		this.animal.getNavigation().stop();
	}
}
