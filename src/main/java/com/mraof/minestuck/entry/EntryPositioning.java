package com.mraof.minestuck.entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface EntryPositioning
{
	BlockPos getTeleportOffset();
	
	// The provided consumer should return true if it succeeds and should continue,
	// or false if it failed and should stop iterating
	boolean forEachBlockTry(BiPredicate<BlockPos, Boolean> consumer);
	
	boolean forEachXZTry(BiPredicate<BlockPos, Boolean> consumer);
	
	default void forEachBlock(BiConsumer<BlockPos, Boolean> consumer)
	{
		forEachBlockTry((pos, edge) -> {consumer.accept(pos, edge); return true;});
	}
	
	default void forEachXZ(BiConsumer<BlockPos, Boolean> consumer)
	{
		forEachXZTry((pos, edge) -> {consumer.accept(pos, edge); return true;});
	}
	
	List<Entity> getOtherEntitiesToTeleport(ServerPlayerEntity playerToExclude, ServerWorld originWorld);
	
	int updateBlocksPostEntry(ServerWorld world, int index);
	
	void writeToNBTPostEntry(CompoundNBT nbt);
	
	static EntryPositioning readNBTPostEntry(CompoundNBT nbt)
	{
		byte type = nbt.getByte("entryType");
		BlockPos center = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
		int range = nbt.getInt("entrySize");
		return new DefaultPositioning(center, range);
	}
}