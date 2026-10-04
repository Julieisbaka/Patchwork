package com.JulieISBaka.patchwork.mixin;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface PotionArrowItemAccessor {
	@Accessor("age")
	void patchwork$setAge(int age);

	@Accessor("pickupDelay")
	int patchwork$getPickupDelay();

	@Accessor("target")
	UUID patchwork$getTarget();

	@Accessor("thrower")
	EntityReference<Entity> patchwork$getThrower();

	@Accessor("thrower")
	void patchwork$setThrower(EntityReference<Entity> thrower);
}
