package com.JulieISBaka.patchwork;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

public interface RabbitPet extends OwnableEntity {
	boolean patchwork$isOrderedToStay();

	void patchwork$setOrderedToStay(boolean stay);

	default boolean patchwork$isOwnedBy(LivingEntity entity) {
		var owner = getOwnerReference();
		return owner != null && owner.matches(entity);
	}
}
