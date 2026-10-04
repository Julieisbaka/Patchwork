package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import com.JulieISBaka.patchwork.SlimeSplitClouds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCubeMob.class)
public class SlimeSplitMixin {
	@Inject(method = "remove", at = @At("HEAD"))
	private void patchwork$spawnSplitCloud(Entity.RemovalReason reason, CallbackInfo ci) {
		if (PatchworkConfig.settings().slimeSplitClouds() && (Object) this instanceof Slime slime
				&& reason == Entity.RemovalReason.KILLED && !slime.isRemoved() && slime.getSize() > 1
				&& slime.isDeadOrDying() && slime.level() instanceof ServerLevel level) {
			SlimeSplitClouds.spawn(slime, level);
		}
	}
}
