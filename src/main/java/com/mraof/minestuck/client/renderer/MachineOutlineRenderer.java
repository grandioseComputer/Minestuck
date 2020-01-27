package com.mraof.minestuck.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mraof.minestuck.Minestuck;
import com.mraof.minestuck.client.util.GuiUtil;
import com.mraof.minestuck.item.MSItems;
import com.mraof.minestuck.item.block.MultiblockItem;
import com.mraof.minestuck.util.MSRotationUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Minestuck.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MachineOutlineRenderer
{
	@SubscribeEvent
	public static void renderWorld(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		
		if (mc.player != null && mc.getRenderViewEntity() == mc.player && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
		{
			if(mc.objectMouseOver instanceof EntityRayTraceResult) return;
			BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
			if (rayTraceResult == null || rayTraceResult.getFace() != Direction.UP)
				return;
			
			if (!renderCheckItem(mc.player, Hand.MAIN_HAND, mc.player.getHeldItemMainhand(), event.getContext(), rayTraceResult, event.getPartialTicks()))
				 renderCheckItem(mc.player, Hand.OFF_HAND, mc.player.getHeldItemOffhand(), event.getContext(), rayTraceResult, event.getPartialTicks());
		}
	}
	
	private static boolean renderCheckItem(ClientPlayerEntity player, Hand hand, ItemStack stack, WorldRenderer render, BlockRayTraceResult rayTraceResult, float partialTicks)
	{

		if(stack.isEmpty())
			return false;
		if(stack.getItem() instanceof MultiblockItem)
				//||stack.getItem()==Item.getItemFromBlock(MinestuckBlocks.jumperBlockExtension[0]))
		{
			MultiblockItem item = (MultiblockItem) stack.getItem();
			BlockPos pos = rayTraceResult.getPos();
			BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, hand, rayTraceResult));

			BlockState block = player.world.getBlockState(pos);
			boolean flag = block.isReplaceable(context);
					//(player.world, player, stack, pos, rayTraceResult.getFace(), (float) rayTraceResult.getPos().getZ() - pos.getX(), (float) rayTraceResult.getPos().getY() - pos.getY(), (float) rayTraceResult.getPos().getZ() - pos.getZ()));
			
			if (!flag)
				pos = pos.up();

			Direction placedFacing = player.getHorizontalFacing().getOpposite();
			double hitX = rayTraceResult.getPos().getX() - pos.getX(), hitZ = rayTraceResult.getPos().getZ() - pos.getZ();
			boolean r = placedFacing.getAxis() == Direction.Axis.Z;
			boolean f = placedFacing== Direction.NORTH || placedFacing==Direction.EAST;
			double d1 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
			double d2 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
			double d3 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
			
			boolean placeable;
			AxisAlignedBB boundingBox;
			
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.lineWidth(2.0F);
			GlStateManager.disableTexture();
			GlStateManager.depthMask(false);	//GL stuff was copied from the standard mouseover bounding box drawing, which is likely why the alpha isn't working
			
			placeable = item.canPlaceAt(context, pos, placedFacing);
			
			BlockPos placementPos = pos;
			pos = item.getPlacementPos(pos, placedFacing, hitX, hitZ);
			
			/*else if(MinestuckBlocks.jumperBlockExtension[0].asItem())
			{
				pos = pos.offset(placedFacing.rotateY());
				
				if (placedFacing.getXOffset() > 0 && hitZ >= 0.5F || placedFacing.getXOffset() < 0 && hitZ < 0.5F
						|| placedFacing.getZOffset() > 0 && hitX < 0.5F || placedFacing.getZOffset() < 0 && hitX >= 0.5F)
					pos = pos.offset(placedFacing.rotateY());
				
				BlockPos placementPos = pos;
				if(placedFacing == Direction.WEST)
					pos = pos.offset(placedFacing, 0);
				if (placedFacing == Direction.SOUTH)
					pos = pos.offset(placedFacing.getOpposite(), 3);    
				if (placedFacing == Direction.EAST)
					pos = pos.offset(placedFacing.getOpposite(), 3).offset(placedFacing.rotateYCCW(), 4);    
				if(placedFacing == Direction.NORTH)
					pos = pos.offset(placedFacing.rotateYCCW(), 4);
				
				boundingBox = new AxisAlignedBB(0, 0, 0, (r ? 5 : 4), 1, (r ? 4 : 5)).offset(pos).offset(-d1, -d2, -d3).shrink(0.002);
				placeable = false;//JumperBlockItem.checkOutline(stack, player, player.world, placementPos, placedFacing);
			} */
			if(item == MSItems.ALCHEMITER)//Alchemiter
			{
				
				int xOffset = - placedFacing.getXOffset() - placedFacing.rotateY().getXOffset();
				int zOffset = - placedFacing.getZOffset() - placedFacing.rotateY().getZOffset();
				//If you don't want the extra details to the alchemiter outline, comment out the following two lines
				WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(3*xOffset + 1F/4F,1,3*zOffset + 1F/4F, 3*xOffset + 3F/4F, 4, 3*zOffset + 3F/4F).offset(placementPos).offset(-d1, -d2, -d3).shrink(0.002), placeable ? 0 : 1, placeable ? 1 : 0, 0, 0.5F);
				WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(0,0,0, 4, 1, 4).offset(pos).offset(-d1, -d2, -d3).shrink(0.002), placeable ? 0 : 1, placeable ? 1 : 0, 0, 0.5F);
			}
			
			boundingBox = GuiUtil.fromBoundingBox(item.getMultiblock().getBoundingBox(MSRotationUtil.fromDirection(placedFacing))).offset(pos).offset(-d1, -d2, -d3).shrink(0.002);
			
			WorldRenderer.drawSelectionBoundingBox(boundingBox, placeable ? 0 : 1, placeable ? 1 : 0, 0, 0.5F);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture();
			GlStateManager.disableBlend();
			return true;
		}
		return false;
	}
}
