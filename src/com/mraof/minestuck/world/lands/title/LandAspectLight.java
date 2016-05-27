package com.mraof.minestuck.world.lands.title;

import net.minecraft.util.math.Vec3d;

import com.mraof.minestuck.world.lands.gen.ChunkProviderLands;
import com.mraof.minestuck.world.lands.terrain.TerrainLandAspect;

public class LandAspectLight extends TitleLandAspect
{
	
	@Override
	public String getPrimaryName()
	{
		return "light";
	}
	
	@Override
	public String[] getNames()
	{
		return new String[] {"light", "brightness"};
	}
	
	@Override
	protected void prepareChunkProvider(ChunkProviderLands chunkProvider)
	{
		chunkProvider.dayCycle = 1;
		
		if(chunkProvider.decorators != null)
		{
			/*ChestGenHooks chestGen = chunkProvider.lootMap.get(AlchemyRecipeHandler.BASIC_MEDIUM_CHEST);
			chestGen.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.glowstone, 1, 0), 1, 4, 2));
			chestGen.addItem(new WeightedRandomChestContent(new ItemStack(Items.glowstone_dust, 1, 0), 2, 5, 3));
			chestGen.addItem(new WeightedRandomChestContent(new ItemStack(Blocks.torch, 1, 0), 2, 10, 5));*/
		}
		
		chunkProvider.mergeFogColor(new Vec3d(1, 1, 0.8), 0.5F);
	}
	
	@Override
	public boolean isAspectCompatible(TerrainLandAspect aspect)
	{
		return aspect.getDayCycleMode() != 2 && (aspect.getWeatherType() == -1 || (aspect.getWeatherType() & 2) == 0);
	}
	
}