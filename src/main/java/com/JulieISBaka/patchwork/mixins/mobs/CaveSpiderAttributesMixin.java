package com.JulieISBaka.patchwork.mixins.mobs;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CaveSpider.class)
public abstract class CaveSpiderAttributesMixin {
	@Inject(method = "createCaveSpider", at = @At("RETURN"))
	private static void patchwork$increaseCaveSpiderAttributes(
			CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		cir.getReturnValue().add(Attributes.MOVEMENT_SPEED, 0.4).add(Attributes.FOLLOW_RANGE, 32.0);
	}
}
