package com.JulieISBaka.patchwork;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.valueproviders.ConstantInt;

public final class SoulSandValleySpawns {
	private SoulSandValleySpawns() {
	}

	public static void register() {
		var biome = BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY);
		BiomeModifications.create(Patchwork.id("soul_sand_valley_skeletons"))
				.add(ModificationPhase.REMOVALS, biome,
						context -> context.getMobSpawnSettings().removeSpawnsOfEntityType(EntityTypes.SKELETON))
				.add(ModificationPhase.ADDITIONS, biome,
						context -> context.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
								new MobSpawnSettings.SpawnerData(EntityTypes.WITHER_SKELETON, ConstantInt.of(4)), 2));
	}

	public static boolean isSoulSandValley(net.minecraft.world.level.LevelReader level,
			net.minecraft.core.BlockPos pos) {
		return level.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY);
	}

	public static void equipBow(WitherSkeleton skeleton) {
		skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		skeleton.reassessWeaponGoal();
	}
}
