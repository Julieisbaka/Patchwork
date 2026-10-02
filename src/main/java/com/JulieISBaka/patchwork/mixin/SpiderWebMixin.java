package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public class SpiderWebMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void patchwork$spinWeb(CallbackInfo ci) {
		Spider spider = (Spider)(Object)this;
		if (!PatchworkConfig.settings().spiderWebs() || !(spider.level() instanceof ServerLevel level)
			|| !spider.isAlive() || spider.tickCount % 100 != 0
			|| !level.getGameRules().get(GameRules.MOB_GRIEFING)) {
			return;
		}
		LivingEntity target = spider.getTarget();
		if (target == null || !target.isAlive() || spider.distanceToSqr(target) > 64.0
			|| spider.distanceToSqr(target) < 4.0 || spider.getRandom().nextInt(4) != 0) {
			return;
		}
		BlockPos pos = target.blockPosition();
		if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
			&& level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState())) {
			level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(spider, Blocks.COBWEB.defaultBlockState()));
		}
	}
}
