package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public class RaidMixin {
	@Inject(method = "spawnGroup", at = @At("TAIL"))
	private void patchwork$spawnIllusioner(ServerLevel level, BlockPos pos, CallbackInfo ci) {
		if (!PatchworkConfig.settings().illusionerRaidSpawns()) {
			return;
		}
		Raid raid = (Raid) (Object) this;
		int wave = raid.getGroupsSpawned();
		if (wave < 5) {
			return;
		}
		var illusioner = Objects.requireNonNull(EntityTypes.ILLUSIONER.create(level, EntitySpawnReason.EVENT),
				"Could not create an illusioner for raid wave " + wave);
		raid.joinRaid(level, wave, illusioner, pos, false);
	}
}
