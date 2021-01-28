package com.mraof.minestuck.entry;

import com.mraof.minestuck.MinestuckConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

public class DefaultPositioning implements EntryPositioning
{
	private final int artifactRange;
	private final BlockPos center, destCenter;
	private final int topY;
	
	DefaultPositioning(BlockPos center, ServerWorld world)
	{
		artifactRange = MinestuckConfig.SERVER.artifactRange.get();
		this.center = center;
		
		topY = MinestuckConfig.SERVER.adaptEntryBlockHeight.get() ? getTopHeight(world) : center.getY() + artifactRange;
		destCenter = new BlockPos(0, 127 - (topY - center.getY()), 0);
	}
	
	DefaultPositioning(BlockPos destCenter, int artifactRange)
	{
		this.artifactRange = artifactRange;
		this.destCenter = destCenter;
		//These aren't needed post-entry, so the value doesn't matter
		center = BlockPos.ZERO;
		topY = 0;
	}
	
	/**
	 * Gives the Y-value of the highest non-air block within artifact range of the coordinates provided in the given world.
	 */
	private int getTopHeight(ServerWorld world)
	{
		AtomicInteger maxY = new AtomicInteger(center.getY());
		forEachXZ((pos, edge) ->
		{
			int height = calculateYHeight(pos, center);
			for(int blockY = Math.min(255, center.getY() + height); blockY > maxY.get(); blockY--)
			{
				((BlockPos.Mutable) pos).setY(blockY);
				if(!world.isAirBlock(pos))
				{
					maxY.set(blockY);
					break;
				}
			}
		});
		
		return maxY.get();
	}
	
	private int calculateYHeight(BlockPos pos, BlockPos center)
	{
		return (int) Math.sqrt(artifactRange * artifactRange - (((pos.getX() - center.getX()) * (pos.getX() - center.getX())
				+ (pos.getZ() - center.getZ()) * (pos.getZ() - center.getZ())) / 2F));
	}
	
	@Override
	public BlockPos getTeleportOffset()
	{
		return destCenter.subtract(center);
	}
	
	@Override
	public boolean forEachBlockTry(BiPredicate<BlockPos, Boolean> consumer)
	{
		return forEachBlockWithCenter(center, topY, consumer);
	}
	
	@Override
	public boolean forEachXZTry(BiPredicate<BlockPos, Boolean> consumer)
	{
		return forEachXZWithCenter(center, consumer);
	}
	
	private boolean forEachXZWithCenter(BlockPos centerPos, BiPredicate<BlockPos, Boolean> consumer)
	{
		BlockPos.Mutable pos = new BlockPos.Mutable(centerPos);
		int minX = centerPos.getX() - artifactRange, maxX = centerPos.getX() + artifactRange;
		for(int blockX = minX; blockX <= maxX; blockX++)
		{
			boolean xEdge = blockX == minX || blockX == maxX;
			pos.setX(blockX);
			int zWidth = (int) Math.sqrt(artifactRange * artifactRange - (blockX - centerPos.getX()) * (blockX - centerPos.getX()));
			int minZ = centerPos.getZ() - zWidth, maxZ = centerPos.getZ() + zWidth;
			for(int blockZ = minZ; blockZ <= maxZ; blockZ++)
			{
				pos.setZ(blockZ);
				if(!consumer.test(pos, xEdge || blockZ == minZ || blockZ == maxZ))
					return false;
			}
		}
		return true;
	}
	
	private boolean forEachBlockWithCenter(BlockPos centerPos, int topY, BiPredicate<BlockPos, Boolean> consumer)
	{
		return forEachXZWithCenter(centerPos, (pos, edge) ->
		{
			int height = calculateYHeight(pos, centerPos);
			int maxY = Math.min(topY, centerPos.getY() + height);
			int minY = Math.max(0, centerPos.getY() - height);
			for(int blockY = minY; blockY <= maxY; blockY++)
			{
				((BlockPos.Mutable) pos).setY(blockY);
				if(!consumer.test(pos, edge || blockY == minY || blockY == maxY))
					return false;
			}
			return true;
		});
	}
	
	@Override
	public List<Entity> getOtherEntitiesToTeleport(ServerPlayerEntity playerToExclude, ServerWorld originWorld)
	{
		return originWorld.getEntitiesInAABBexcluding(playerToExclude, getBoundingBoxForEntities(),
				EntityPredicates.NOT_SPECTATING.and(this::isEntityInRange));
	}
	
	private boolean isEntityInRange(Entity entity)
	{
		return center.distanceSq(entity.getPositionVec(), true) <= artifactRange*artifactRange;
	}
	
	private AxisAlignedBB getBoundingBoxForEntities()
	{
		return new AxisAlignedBB(center.getX() + 0.5 - artifactRange, center.getY() + 0.5 - artifactRange,
				center.getZ() + 0.5 - artifactRange, center.getX() + 0.5 + artifactRange,
				center.getY() + 0.5 + artifactRange, center.getZ() + 0.5 + artifactRange);
	}
	
	@Override
	public int updateBlocksPostEntry(ServerWorld world, int index)
	{
		long time = System.currentTimeMillis() + PostEntryTask.MIN_TIME;
		AtomicInteger i = new AtomicInteger(0);
		boolean finished = forEachBlockWithCenter(destCenter, 128, (pos, edge) -> {
			
			if(index <= i.getAndIncrement())
				PostEntryTask.updateBlock(pos, world, edge);
			return time > System.currentTimeMillis();
		});
		
		if(finished)
			return -1;
		else return Math.max(i.get(), index);
	}
	
	@Override
	public void writeToNBTPostEntry(CompoundNBT nbt)
	{
		nbt.putInt("x", destCenter.getX());
		nbt.putInt("y", destCenter.getY());
		nbt.putInt("z", destCenter.getZ());
		nbt.putInt("entrySize", artifactRange);
		nbt.putByte("entryType", (byte) 0);
	}
}