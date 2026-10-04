package com.JulieISBaka.patchwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PotionCauldronEntity extends BlockEntity {
	private PotionContents potion = PotionContents.EMPTY;

	public PotionCauldronEntity(BlockPos pos, BlockState state) {
		super(PotionCauldrons.TYPE, pos, state);
	}

	public PotionContents potion() {
		return potion;
	}

	public void setPotion(PotionContents potion) {
		this.potion = potion;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		potion = input.read("potion", PotionContents.CODEC)
				.orElseThrow(() -> new IllegalStateException("Potion cauldron has no potion at " + worldPosition));
		if (level != null && level.isClientSide()) {
			// The block update can build the mesh before its potion data arrives.
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("potion", PotionContents.CODEC, potion);
	}
}
