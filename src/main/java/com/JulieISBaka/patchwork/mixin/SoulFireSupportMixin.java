package com.JulieISBaka.patchwork.mixin;

import com.JulieISBaka.patchwork.SoulFireSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoulFireBlock.class)
public abstract class SoulFireSupportMixin extends BaseFireBlock {
	protected SoulFireSupportMixin(BlockBehaviour.Properties properties, float damage) {
		super(properties, damage);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void patchwork$ordinaryFireDefault(BlockBehaviour.Properties properties, CallbackInfo ci) {
		registerDefaultState(defaultBlockState().setValue(SoulFireSupport.CHARGE_PLACED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SoulFireSupport.CHARGE_PLACED);
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void patchwork$chargeSupport(BlockState state, LevelReader level, BlockPos pos,
			CallbackInfoReturnable<Boolean> cir) {
		if (state.getValue(SoulFireSupport.CHARGE_PLACED)
				&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void patchwork$preserveChargeSupport(BlockState state, LevelReader level, ScheduledTickAccess ticks,
			BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random,
			CallbackInfoReturnable<BlockState> cir) {
		if (state.getValue(SoulFireSupport.CHARGE_PLACED)) {
			cir.setReturnValue(state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState());
		}
	}
}
