package com.JulieISBaka.patchwork;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class UnlitTorches {
	public static final Block TORCH = standing("unlit_torch", Blocks.TORCH);
	public static final Block WALL_TORCH = wall("unlit_wall_torch", Blocks.WALL_TORCH, Blocks.TORCH);
	public static final Block SOUL_TORCH = standing("unlit_soul_torch", Blocks.SOUL_TORCH);
	public static final Block SOUL_WALL_TORCH = wall("unlit_soul_wall_torch", Blocks.SOUL_WALL_TORCH, Blocks.SOUL_TORCH);
	public static final Block COPPER_TORCH = standing("unlit_copper_torch", Blocks.COPPER_TORCH);
	public static final Block COPPER_WALL_TORCH = wall("unlit_copper_wall_torch", Blocks.COPPER_WALL_TORCH, Blocks.COPPER_TORCH);
	private static final Map<Block, Block> UNLIT = Map.of(
		Blocks.TORCH, TORCH, Blocks.WALL_TORCH, WALL_TORCH,
		Blocks.SOUL_TORCH, SOUL_TORCH, Blocks.SOUL_WALL_TORCH, SOUL_WALL_TORCH,
		Blocks.COPPER_TORCH, COPPER_TORCH, Blocks.COPPER_WALL_TORCH, COPPER_WALL_TORCH
	);

	private UnlitTorches() {
	}

	public static void register() {
	}

	public static BlockState extinguish(BlockState lit) {
		Block unlit = UNLIT.get(lit.getBlock());
		return unlit == null ? null : unlit.withPropertiesOf(lit);
	}

	private static BlockBehaviour.Properties properties(Block lit, ResourceKey<Block> key) {
		return BlockBehaviour.Properties.ofLegacyCopy(lit).lightLevel(state -> 0)
			.overrideLootTable(lit.getLootTable()).overrideDescription(lit.getDescriptionId()).setId(key);
	}

	private static Block standing(String name, Block lit) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key,
			new UnlitTorchBlock(lit, properties(lit, key)));
	}

	private static Block wall(String name, Block lit, Block item) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key,
			new UnlitWallTorchBlock(lit, item, properties(lit, key)));
	}

	private static InteractionResult relight(ItemStack stack, Level level, BlockPos pos, Player player, BlockState lit) {
		if (!stack.is(Items.FIRE_CHARGE)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			level.setBlockAndUpdate(pos, lit);
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}

	private static class UnlitTorchBlock extends TorchBlock {
		private final Block lit;

		UnlitTorchBlock(Block lit, BlockBehaviour.Properties properties) {
			super(ParticleTypes.SMOKE, properties);
			this.lit = lit;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
			return relight(stack, level, pos, player, lit.defaultBlockState());
		}

		@Override
		protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
			return new ItemStack(lit);
		}
	}

	private static class UnlitWallTorchBlock extends WallTorchBlock {
		private final Block lit;
		private final Block item;

		UnlitWallTorchBlock(Block lit, Block item, BlockBehaviour.Properties properties) {
			super(ParticleTypes.SMOKE, properties);
			this.lit = lit;
			this.item = item;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
			return relight(stack, level, pos, lit.defaultBlockState().setValue(FACING, state.getValue(FACING)));
		}

		@Override
		protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
			return new ItemStack(item);
		}
	}
}
