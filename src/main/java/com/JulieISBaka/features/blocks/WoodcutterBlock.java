package com.JulieISBaka.patchwork.features.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;


public class WoodcutterBlock extends StonecutterBlock {
	private static final Component CONTAINER_TITLE = Component.translatable("container.patchwork.woodcutter");

	public WoodcutterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (!level.isClientSide()) {
			player.openMenu(state.getMenuProvider(level, pos));
			player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return new SimpleMenuProvider((containerId, inventory, player) -> new WoodcutterMenu(containerId, inventory,
				ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
	}
}
