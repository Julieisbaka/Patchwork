package com.JulieISBaka.patchwork;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;

public final class UnlitTorches {
	public static final Block TORCH = standing("unlit_torch", Blocks.TORCH);
	public static final Block WALL_TORCH = wall("unlit_wall_torch", "unlit_torch", Blocks.WALL_TORCH);
	public static final Block SOUL_TORCH = standing("unlit_soul_torch", Blocks.SOUL_TORCH);
	public static final Block SOUL_WALL_TORCH = wall("unlit_soul_wall_torch", "unlit_soul_torch",
			Blocks.SOUL_WALL_TORCH);
	public static final Block COPPER_TORCH = CopperTorches.UNLIT.weathering().unaffected();
	public static final Block COPPER_WALL_TORCH = CopperTorches.UNLIT_WALL.weathering().unaffected();
	public static final Block REDSTONE_TORCH = standing("unlit_redstone_torch", Blocks.REDSTONE_TORCH);
	public static final Block REDSTONE_WALL_TORCH = wall("unlit_redstone_wall_torch", "unlit_redstone_torch",
			Blocks.REDSTONE_WALL_TORCH);
	public static final Item TORCH_ITEM = item("unlit_torch", TORCH, WALL_TORCH);
	public static final Item SOUL_TORCH_ITEM = item("unlit_soul_torch", SOUL_TORCH, SOUL_WALL_TORCH);
	public static final Item COPPER_TORCH_ITEM = COPPER_TORCH.asItem();
	public static final Item REDSTONE_TORCH_ITEM = item("unlit_redstone_torch", REDSTONE_TORCH, REDSTONE_WALL_TORCH);
	private static final Map<Block, Block> UNLIT = Map.of(Blocks.TORCH, TORCH, Blocks.WALL_TORCH, WALL_TORCH,
			Blocks.SOUL_TORCH, SOUL_TORCH, Blocks.SOUL_WALL_TORCH, SOUL_WALL_TORCH, Blocks.COPPER_TORCH, COPPER_TORCH,
			Blocks.COPPER_WALL_TORCH, COPPER_WALL_TORCH, Blocks.REDSTONE_TORCH, REDSTONE_TORCH,
			Blocks.REDSTONE_WALL_TORCH,
			REDSTONE_WALL_TORCH);

	private UnlitTorches() {
	}

	public static void register() {
		CopperTorches.register();
	}

	public static BlockState extinguish(BlockState lit) {
		if (lit.getBlock() instanceof RedstoneTorchBlock && !lit.getValue(RedstoneTorchBlock.LIT)) {
			return null;
		}
		Block unlit = UNLIT.get(lit.getBlock());
		return unlit == null ? CopperTorches.extinguish(lit) : unlit.withPropertiesOf(lit);
	}

	private static BlockBehaviour.Properties properties(Block lit, ResourceKey<Block> key) {
		return BlockBehaviour.Properties.ofFullCopy(lit).lightLevel(state -> 0)
				.overrideDescription(key.identifier().toLanguageKey("block")).setId(key);
	}

	private static Block standing(String name, Block lit) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, new UnlitTorchBlock(lit, properties(lit, key)));
	}

	private static Block wall(String name, String drop, Block lit) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Patchwork.id(name));
		ResourceKey<LootTable> loot = ResourceKey.create(Registries.LOOT_TABLE, Patchwork.id("blocks/" + drop));
		return Registry.register(BuiltInRegistries.BLOCK, key,
				new UnlitWallTorchBlock(lit, properties(lit, key).overrideLootTable(Optional.of(loot))));
	}

	private static Item item(String name, Block standing, Block wall) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Patchwork.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, new StandingAndWallBlockItem(standing, wall,
				Direction.DOWN, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
		Item.BY_BLOCK.put(standing, item);
		Item.BY_BLOCK.put(wall, item);
		return item;
	}

	static InteractionResult relight(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockState lit) {
		boolean flint = stack.is(Items.FLINT_AND_STEEL);
		if (!flint && !stack.is(Items.FIRE_CHARGE)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			level.setBlockAndUpdate(pos, lit);
			if (flint) {
				stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
			} else {
				stack.consume(1, player);
			}
			level.playSound(null, pos, flint ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
					SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
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
			return relight(stack, level, pos, player, hand, lit.defaultBlockState());
		}

		@Override
		protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
			return new ItemStack(this);
		}
	}

	private static class UnlitWallTorchBlock extends WallTorchBlock {
		private final Block lit;

		UnlitWallTorchBlock(Block lit, BlockBehaviour.Properties properties) {
			super(ParticleTypes.SMOKE, properties);
			this.lit = lit;
		}

		@Override
		public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
		}

		@Override
		protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
				Player player, InteractionHand hand, BlockHitResult hit) {
			return relight(stack, level, pos, player, hand,
					lit.defaultBlockState().setValue(FACING, state.getValue(FACING)));
		}

		@Override
		protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
			return new ItemStack(this);
		}
	}
}
