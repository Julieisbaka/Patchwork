package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.PatchworkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
public class EndermanDefenseMixin {
	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void patchwork$placeDefensiveBlock(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		Enderman enderman = (Enderman)(Object)this;
		BlockState carried = enderman.getCarriedBlock();
		if (!PatchworkConfig.settings().endermanDefense() || !cir.getReturnValue() || !enderman.isAlive()
			|| carried == null || !(source.getEntity() instanceof LivingEntity attacker)
			|| !level.getGameRules().get(GameRules.MOB_GRIEFING) || enderman.getRandom().nextInt(3) != 0) {
			return;
		}

		double dx = attacker.getX() - enderman.getX();
		double dz = attacker.getZ() - enderman.getZ();
		if (dx * dx + dz * dz < 1.0) {
			return;
		}
		BlockPos pos = enderman.blockPosition().offset(
			Math.abs(dx) >= Math.abs(dz) ? (int)Math.signum(dx) : 0,
			0,
			Math.abs(dx) < Math.abs(dz) ? (int)Math.signum(dz) : 0
		);
		BlockPos below = pos.below();
		BlockState placement = Block.updateFromNeighbourShapes(carried, level, pos);
		if (!placement.isAir() && level.getBlockState(pos).isAir()
			&& level.getBlockState(below).isCollisionShapeFullBlock(level, below)
			&& !level.getBlockState(below).is(Blocks.BEDROCK) && placement.canSurvive(level, pos)
			&& level.getEntities(enderman, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos))).isEmpty()
			&& level.setBlockAndUpdate(pos, placement)) {
			level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(enderman, placement));
			enderman.setCarriedBlock(null);
		}
	}
}
