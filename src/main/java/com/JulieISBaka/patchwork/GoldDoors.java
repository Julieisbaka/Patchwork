package com.JulieISBaka.patchwork;

import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public final class GoldDoors {
	private static final BlockSetType SET_TYPE = BlockSetTypeBuilder.copyOf(BlockSetType.GOLD).openableByHand(true)
			.openableByWindCharge(false).register(Patchwork.id("gold"));
	private static final ResourceKey<Block> DOOR_BLOCK_KEY = ResourceKey.create(Registries.BLOCK,
			Patchwork.id("gold_door"));
	private static final ResourceKey<Item> DOOR_ITEM_KEY = ResourceKey.create(Registries.ITEM,
			Patchwork.id("gold_door"));
	private static final ResourceKey<Block> TRAPDOOR_BLOCK_KEY = ResourceKey.create(Registries.BLOCK,
			Patchwork.id("gold_trapdoor"));
	private static final ResourceKey<Item> TRAPDOOR_ITEM_KEY = ResourceKey.create(Registries.ITEM,
			Patchwork.id("gold_trapdoor"));
	public static final Block DOOR = Registry.register(BuiltInRegistries.BLOCK, DOOR_BLOCK_KEY,
			new GoldDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).requiresCorrectToolForDrops()
					.strength(5.0F).noOcclusion().pushReaction(PushReaction.POPPED).setId(DOOR_BLOCK_KEY)));
	public static final Block TRAPDOOR = Registry.register(BuiltInRegistries.BLOCK, TRAPDOOR_BLOCK_KEY,
			new GoldTrapDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).requiresCorrectToolForDrops()
					.strength(5.0F).noOcclusion().setId(TRAPDOOR_BLOCK_KEY)));
	public static final Item DOOR_ITEM = Registry.register(BuiltInRegistries.ITEM, DOOR_ITEM_KEY,
			new DoubleHighBlockItem(DOOR, new Item.Properties().setId(DOOR_ITEM_KEY).useBlockDescriptionPrefix()));
	public static final Item TRAPDOOR_ITEM = Registry.register(BuiltInRegistries.ITEM, TRAPDOOR_ITEM_KEY,
			new BlockItem(TRAPDOOR, new Item.Properties().setId(TRAPDOOR_ITEM_KEY).useBlockDescriptionPrefix()));

	private GoldDoors() {
	}

	public static void register() {
		Item.BY_BLOCK.put(DOOR, DOOR_ITEM);
		Item.BY_BLOCK.put(TRAPDOOR, TRAPDOOR_ITEM);
		for (var tab : List.of(CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS)) {
			CreativeModeTabEvents.modifyOutputEvent(tab).register(output -> {
				output.insertAfter(Items.IRON_DOOR, DOOR_ITEM);
				output.insertAfter(Items.IRON_TRAPDOOR, TRAPDOOR_ITEM);
			});
		}
	}

	/**
	 * Powered gold doors and trapdoors are locked; only players in creative mode
	 * can still toggle them.
	 */
	public static boolean isLocked(BlockState state, Entity entity) {
		return state.getValue(BlockStateProperties.POWERED)
				&& !(entity instanceof Player player && player.hasInfiniteMaterials());
	}

	private static final class GoldDoorBlock extends DoorBlock {
		private GoldDoorBlock(BlockBehaviour.Properties properties) {
			super(SET_TYPE, properties);
		}

		@Override
		public BlockState getStateForPlacement(BlockPlaceContext context) {
			BlockState state = super.getStateForPlacement(context);
			return state == null ? null : state.setValue(OPEN, false);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
				BlockHitResult hit) {
			return isLocked(state, player) ? InteractionResult.CONSUME
					: super.useWithoutItem(state, level, pos, player, hit);
		}

		@Override
		public void setOpen(Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
			if (!isLocked(state, entity)) {
				super.setOpen(entity, level, state, pos, open);
			}
		}

		@Override
		protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
				Orientation orientation, boolean movedByPiston) {
			if (level.isClientSide() || defaultBlockState().is(block)) {
				return;
			}
			BlockPos other = pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
			boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(other);
			if (powered != state.getValue(POWERED)) {
				level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
			}
		}
	}

	private static final class GoldTrapDoorBlock extends TrapDoorBlock {
		private GoldTrapDoorBlock(BlockBehaviour.Properties properties) {
			super(SET_TYPE, properties);
		}

		@Override
		public BlockState getStateForPlacement(BlockPlaceContext context) {
			BlockState state = super.getStateForPlacement(context);
			return state == null ? null : state.setValue(OPEN, false);
		}

		@Override
		protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
				BlockHitResult hit) {
			return isLocked(state, player) ? InteractionResult.CONSUME
					: super.useWithoutItem(state, level, pos, player, hit);
		}

		@Override
		protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
				Orientation orientation, boolean movedByPiston) {
			if (level.isClientSide()) {
				return;
			}
			boolean powered = level.hasNeighborSignal(pos);
			if (powered != state.getValue(POWERED)) {
				level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
			}
		}
	}
}
