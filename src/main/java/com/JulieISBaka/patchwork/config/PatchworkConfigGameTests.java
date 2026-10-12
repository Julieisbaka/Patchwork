package com.JulieISBaka.patchwork.config;

import com.JulieISBaka.patchwork.config.NumericSetting;
import com.JulieISBaka.patchwork.config.PatchworkConfig;
import com.JulieISBaka.patchwork.features.mobs.AnimalFoodGoal;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;

public final class PatchworkConfigGameTests {
	private static final BlockPos CENTER = new BlockPos(3, 1, 3);

	@GameTest
	public void animalFoodUsesConfiguredRadiusAndToggle(GameTestHelper test) {
		boolean original = test.getLevel().getGameRules().get(GameRules.MOB_GRIEFING);
		test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, true, test.getLevel().getServer());
		try {
			var animal = test.spawn(EntityTypes.COW, CENTER);
			animal.setNoAi(true);
			double radius = NumericSetting.ANIMAL_FOOD_SEARCH_RADIUS.get();
			ItemEntity food = new ItemEntity(test.getLevel(), animal.getX() + radius + 2, animal.getY(), animal.getZ(),
					new ItemStack(Items.WHEAT, 2));
			food.setNoPickUpDelay();
			test.assertTrue(test.getLevel().addFreshEntity(food), "Could not spawn food for configured radius test");
			AnimalFoodGoal outside = new AnimalFoodGoal(animal);
			test.assertTrue(!outside.canUse(), "Animal searched beyond its configured food radius");
			food.setPos(animal.getX() + radius * 0.75, animal.getY(), animal.getZ());
			AnimalFoodGoal inside = new AnimalFoodGoal(animal);
			boolean enabled = PatchworkConfig.settings().animalDroppedFood();
			test.assertTrue(inside.canUse() == enabled, "Animal food radius or feature toggle was ignored");
			if (enabled) {
				test.assertTrue(inside.canContinueToUse(), "Food inside configured radius was not pursued");
				food.setPos(animal.getX() + radius + 1, animal.getY(), animal.getZ());
				test.assertTrue(!inside.canContinueToUse(), "Animal pursued food outside configured radius");
			}
			food.discard();
			test.succeed();
		} finally {
			test.getLevel().getGameRules().set(GameRules.MOB_GRIEFING, original, test.getLevel().getServer());
		}
	}
}
