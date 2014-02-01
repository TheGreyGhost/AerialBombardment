package aerialbombardment.common.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapInfo;

import java.util.List;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 */
public class ItemTargetingTablet extends Item
{
  protected ItemTargetingTablet(int itemID)
  {
    super(itemID);
    this.setHasSubtypes(true);          // TODO: required?
    this.setUnlocalizedName("targetingtablet");
    this.setTextureName("targetingtablet");
  }


  public void updateMapData(World par1World, Entity par2Entity, MapData par3MapData)
  {
    if (par1World.provider.dimensionId == par3MapData.dimension && par2Entity instanceof EntityPlayer)
    {
      short X_SIZE = 128;
      short Z_SIZE = 128;
      int scaleMultiplier = 1 << par3MapData.scale;
      int xCenter = par3MapData.xCenter;
      int zCenter = par3MapData.zCenter;
      int playerXpos = MathHelper.floor_double(par2Entity.posX - (double) xCenter) / scaleMultiplier + X_SIZE / 2;
      int playerZpos = MathHelper.floor_double(par2Entity.posZ - (double)zCenter) / scaleMultiplier + Z_SIZE / 2;
      int pixelsFor128Blocks = 128 / scaleMultiplier;

      if (par1World.provider.hasNoSky)
      {
        pixelsFor128Blocks /= 2;
      }

      MapInfo mapinfo = par3MapData.func_82568_a((EntityPlayer)par2Entity);
      ++mapinfo.updateCountMask;

      for (int x = playerXpos - pixelsFor128Blocks + 1; x < playerXpos + pixelsFor128Blocks; ++x)
      {
        if ((x & 15) == (mapinfo.updateCountMask & 15))
        {
          int minZchanged = 255;
          int maxZchanged = 0;
          double d0 = 0.0D;

          for (int z = playerZpos - pixelsFor128Blocks - 1; z < playerZpos + pixelsFor128Blocks; ++z)
          {
            if (x >= 0 && z >= -1 && x < X_SIZE && z < Z_SIZE)
            {
              int dx = x - playerXpos;
              int dz = z - playerZpos;
              boolean outsideUpdateRadius = dx * dx + dz * dz > (pixelsFor128Blocks - 2) * (pixelsFor128Blocks - 2);
              int worldx = (xCenter / scaleMultiplier + x - X_SIZE / 2) * scaleMultiplier;
              int worldz = (zCenter / scaleMultiplier + z - Z_SIZE / 2) * scaleMultiplier;
              int[] topBlockCount = new int[Block.blocksList.length];
              Chunk chunk = par1World.getChunkFromBlockCoords(worldx, worldz);

              if (!chunk.isEmpty())
              {
                int xlsnibble = worldx & 15;
                int zlsnibble = worldz & 15;
                int underwaterDepth = 0;
                double weightedHeightSum = 0.0D;
                int xoffs;
                int zoffs;
                int heightValue;
                int liquidHeightValue;

                if (par1World.provider.hasNoSky)
                {
                  xoffs = worldx + worldz * 231871;
                  xoffs = xoffs * xoffs * 31287121 + xoffs * 11;

                  if ((xoffs >> 20 & 1) == 0)
                  {
                    topBlockCount[Block.dirt.blockID] += 10;
                  }
                  else
                  {
                    topBlockCount[Block.stone.blockID] += 10;
                  }

                  weightedHeightSum = 100.0D;
                }
                else
                {
                  for (xoffs = 0; xoffs < scaleMultiplier; ++xoffs)
                  {
                    for (zoffs = 0; zoffs < scaleMultiplier; ++zoffs)
                    {
                      heightValue = chunk.getHeightValue(xoffs + xlsnibble, zoffs + zlsnibble) + 1;
                      int topBlockID = 0;

                      if (heightValue > 1)
                      {
                        boolean topBlockIsSolid;

                        do
                        {
                          topBlockIsSolid = true;
                          topBlockID = chunk.getBlockID(xoffs + xlsnibble, heightValue - 1, zoffs + zlsnibble);

                          if (topBlockID == 0)
                          {
                            topBlockIsSolid = false;
                          }
                          else if (heightValue > 0 && topBlockID > 0 && Block.blocksList[topBlockID].blockMaterial.materialMapColor == MapColor.airColor)
                          {
                            topBlockIsSolid = false;
                          }

                          if (!topBlockIsSolid)
                          {
                            --heightValue;

                            if (heightValue <= 0)
                            {
                              break;
                            }

                            topBlockID = chunk.getBlockID(xoffs + xlsnibble, heightValue - 1, zoffs + zlsnibble);
                          }
                        }
                        while (heightValue > 0 && !topBlockIsSolid);

                        if (heightValue > 0 && topBlockID != 0 && Block.blocksList[topBlockID].blockMaterial.isLiquid())
                        {
                          liquidHeightValue = heightValue - 1;
                          boolean flag2 = false;
                          int liquidBlockID;

                          do
                          {
                            liquidBlockID = chunk.getBlockID(xoffs + xlsnibble, liquidHeightValue--, zoffs + zlsnibble);
                            ++underwaterDepth;
                          }
                          while (liquidHeightValue > 0 && liquidBlockID != 0 && Block.blocksList[liquidBlockID].blockMaterial.isLiquid());
                        }
                      }

                      weightedHeightSum += (double)heightValue / (double)(scaleMultiplier * scaleMultiplier);
                      ++topBlockCount[topBlockID];
                    }
                  }
                }

                underwaterDepth /= scaleMultiplier * scaleMultiplier;
                xoffs = 0;
                zoffs = 0;

                for (heightValue = 0; heightValue < Block.blocksList.length; ++heightValue)
                {
                  if (topBlockCount[heightValue] > xoffs)
                  {
                    zoffs = heightValue;
                    xoffs = topBlockCount[heightValue];
                  }
                }

                double d2 = (weightedHeightSum - d0) * 4.0D / (double)(scaleMultiplier + 4) + ((double)(x + z & 1) - 0.5D) * 0.4D;
                byte underwaterDepthPaletteOffset = 1;

                if (d2 > 0.6D)
                {
                  underwaterDepthPaletteOffset = 2;
                }

                if (d2 < -0.6D)
                {
                  underwaterDepthPaletteOffset = 0;
                }

                liquidHeightValue = 0;

                if (zoffs > 0)      // zoffs is now ID of most prevalent block
                {
                  MapColor mapcolor = Block.blocksList[zoffs].blockMaterial.materialMapColor;

                  if (mapcolor == MapColor.waterColor)
                  {
                    d2 = (double)underwaterDepth * 0.1D + (double)(x + z & 1) * 0.2D;
                    underwaterDepthPaletteOffset = 1;

                    if (d2 < 0.5D)
                    {
                      underwaterDepthPaletteOffset = 2;
                    }

                    if (d2 > 0.9D)
                    {
                      underwaterDepthPaletteOffset = 0;
                    }
                  }

                  liquidHeightValue = mapcolor.colorIndex;
                }

                d0 = weightedHeightSum;

                if (z >= 0 && dx * dx + dz * dz < pixelsFor128Blocks * pixelsFor128Blocks && (!outsideUpdateRadius || (x + z & 1) != 0))
                {
                  byte b1 = par3MapData.colors[x + z * X_SIZE];
                  byte b2 = (byte)(liquidHeightValue * 4 + underwaterDepthPaletteOffset);

                  if (b1 != b2)
                  {
                    if (minZchanged > z)
                    {
                      minZchanged = z;
                    }

                    if (maxZchanged < z)
                    {
                      maxZchanged = z;
                    }

                    par3MapData.colors[x + z * X_SIZE] = b2;
                  }
                }
              }
            }
          }

          if (minZchanged <= maxZchanged)
          {
            par3MapData.setColumnDirty(x, minZchanged, maxZchanged);
          }
        }
      }
    }
  }

  /**
   * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
   * update it's contents.
   */
  public void onUpdate(ItemStack par1ItemStack, World par2World, Entity player, int inventoryPosition, boolean isCurrentlyHeld)
  {
    if (!par2World.isRemote)
    {
      MapData mapdata = this.getMapData(par1ItemStack, par2World);

      if (player instanceof EntityPlayer)
      {
        EntityPlayer entityplayer = (EntityPlayer) player;
        mapdata.updateVisiblePlayers(entityplayer, par1ItemStack);
      }

      if (isCurrentlyHeld)
      {
        this.updateMapData(par2World, player, mapdata);
      }
    }
  }

  /**
   * returns null if no update is to be sent
   */
  public Packet createMapDataPacket(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
  {
    byte[] abyte = this.getMapData(par1ItemStack, par2World).getUpdatePacketData(par1ItemStack, par2World, par3EntityPlayer);
    return abyte == null ? null : new Packet131MapData((short) Item.map.itemID, (short)par1ItemStack.getItemDamage(), abyte);
  }

  /**
   * Called when item is crafted/smelted. Used only by maps so far.
   */
  public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
  {
    if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().getBoolean("map_is_scaling"))
    {
      MapData mapdata = Item.map.getMapData(par1ItemStack, par2World);
      par1ItemStack.setItemDamage(par2World.getUniqueDataId("map"));
      MapData mapdata1 = new MapData("map_" + par1ItemStack.getItemDamage());
      mapdata1.scale = (byte)(mapdata.scale + 1);

      if (mapdata1.scale > 4)
      {
        mapdata1.scale = 4;
      }

      mapdata1.xCenter = mapdata.xCenter;
      mapdata1.zCenter = mapdata.zCenter;
      mapdata1.dimension = mapdata.dimension;
      mapdata1.markDirty();
      par2World.setItemData("map_" + par1ItemStack.getItemDamage(), mapdata1);
    }
  }

  @SideOnly(Side.CLIENT)

  /**
   * allows items to add custom lines of information to the mouseover description
   */
  public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
  {
    MapData mapdata = this.getMapData(par1ItemStack, par2EntityPlayer.worldObj);

    if (par4)
    {
      if (mapdata == null)
      {
        par3List.add("Unknown map");
      }
      else
      {
        par3List.add("Scaling at 1:" + (1 << mapdata.scale));
        par3List.add("(Level " + mapdata.scale + "/" + 4 + ")");
      }
    }
  }
}

