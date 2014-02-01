package aerialbombardment.clientonly;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapInfo;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 */
public class TargetingTabletMapClient
{

  public TargetingTabletMapClient()
  {

  }

  private void updateMapRectangularPortion(World world, int wxStart, int wzStart, int mapxStart, int mapzStart, int xcount, int zcount)
  {
    int wxEndPlusOne = wxStart + xcount + 1;
    int wzEndPlusOne = wzStart + zcount + 1;

    for (int wz = wzStart; wz < wzEndPlusOne; ++wz) {
      int idx = ((wz-wzStart) + mapzStart) * mapData.Z_STRIDE + mapxStart;

      for (int wx = wxStart; wx < wxEndPlusOne; ++wx) {
        Chunk chunk = world.getChunkFromBlockCoords(wx, wz);


        int[] topBlockCount = new int[Block.blocksList.length];

        if (!chunk.isEmpty())
        {
          int cx = wx & 15;
          int cz = wz & 15;
          int cy;
          int liquidHeightValue;

          cy = chunk.getHeightValue(cx, cz); // starting y is the air block just above the ground
          int nextBlockDownID = 0;

          boolean foundSolidBlock = false;

          while (!foundSolidBlock && cy > 0) {
            nextBlockDownID = chunk.getBlockID(cx, cy - 1, cz);
            if (nextBlockDownID != 0 && Block.blocksList[nextBlockDownID].blockMaterial.materialMapColor != MapColor.airColor) {
              foundSolidBlock = true;
            } else {
              --cy;
            }
          }
          int wyTopSolidBlockPlusOne = cy;

          boolean inWater = nextBlockDownID != 0 && Block.blocksList[nextBlockDownID].blockMaterial.isLiquid();
          while (inWater && cy > 0) {
            nextBlockDownID = chunk.getBlockID(cx, cy - 1, cz);
            if (nextBlockDownID != 0 && !(Block.blocksList[nextBlockDownID].blockMaterial.isLiquid())) {
              inWater = false;
            } else {
              --cy;
            }
          }

          int waterDepth = 0;
          waterDepth = wyTopSolidBlockPlusOne - cy;


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
              d2 = (double)waterDepth * 0.1D + (double)(x + z & 1) * 0.2D;
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

  public void updateMap(World world, int xCentre, int zCentre)
  {
      short X_SIZE = 128;
      short Z_SIZE = 128;
      int scaleMultiplier = 1 << par3MapData.scale;
      int xCenter = par3MapData.xCenter;
      int zCenter = par3MapData.zCenter;
      int playerXpos = MathHelper.floor_double(par2Entity.posX - (double) xCenter) / scaleMultiplier + X_SIZE / 2;
      int playerZpos = MathHelper.floor_double(par2Entity.posZ - (double)zCenter) / scaleMultiplier + Z_SIZE / 2;
      int pixelsFor128Blocks = 128 / scaleMultiplier;

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
              Chunk chunk = world.getChunkFromBlockCoords(worldx, worldz);

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

                if (world.provider.hasNoSky)
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


  private TabletMapData mapData;

}
