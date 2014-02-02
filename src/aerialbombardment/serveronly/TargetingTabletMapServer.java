package aerialbombardment.serveronly;

import aerialbombardment.clientonly.TabletMapData;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 */
public class TargetingTabletMapServer
{

  public TargetingTabletMapServer(int wxCentreInit, int wzCentreInit)
  {
    mapData = new TabletMapDataWithAltitude(wxCentreInit, wzCentreInit);
  }

  /**
   * Initialises the entire map from the world
   * @param world
   * @param wxCentreInit  centre [x,z] of the map in world coordinates
   * @param wzCentreInit  centre [x,z] of the map in world coordinates
   */
  public void updateEntireMap(World world, int wxCentreInit, int wzCentreInit)
  {
    mapData.wxCentre = wxCentreInit;
    mapData.wzCentre = wzCentreInit;
    mapData.ixMinOffset = 0;
    mapData.iZMinOffset = 0;

    updateMapRectangularPortion(world, 0, 0, mapData.MAP_SIZE_X, mapData.MAP_SIZE_Z);
    updateContours(0, 0, mapData.MAP_SIZE_X, mapData.MAP_SIZE_Z);
  }

  /**
   * scrolls the map to the new position; fills the new edges with mapData.MAP_COLOUR_FOG
   * @param world
   * @param wxCentreNew new centre [x,z] of the map in world coordinates
   * @param wzCentreNew new centre [x,z] of the map in world coordinates
   */
  public void moveToNewCentre(World world, int wxCentreNew, int wzCentreNew)
  {
    int fogStripWidth = Math.abs(wxCentreNew - mapData.wxCentre);
    fogStripWidth = Math.min(fogStripWidth, mapData.MAP_SIZE_X);
    int fogStripHeight = Math.abs(wzCentreNew - mapData.wzCentre);
    fogStripHeight = Math.min(fogStripHeight, mapData.MAP_SIZE_Z);

    int fogStripXmin;
    int fogStripZmin;
    if (wxCentreNew < mapData.wxCentre) {
      fogStripXmin = mapData.ixMinOffset + mapData.MAP_SIZE_X - fogStripWidth;
    } else {
      fogStripXmin = mapData.ixMinOffset;
    }

    if (wzCentreNew < mapData.wzCentre) {
      fogStripZmin = mapData.iZMinOffset + mapData.MAP_SIZE_Z - fogStripHeight;
    } else {
      fogStripZmin = mapData.iZMinOffset;
    }

    for (int z = 0; z < fogStripHeight; ++z) {
      for (int x = 0; x < mapData.MAP_SIZE_X; ++x) {
        int idx =  ((fogStripZmin + z ) % mapData.MAP_SIZE_Z ) * mapData.Z_STRIDE
                  + (fogStripXmin + x ) % mapData.MAP_SIZE_X;
        mapData.mapColours[idx] = mapData.MAP_COLOUR_FOG;
      }
    }

    for (int z = fogStripHeight; z < mapData.MAP_SIZE_Z; ++z) {
      for (int x = 0; x < fogStripWidth; ++x) {
        int idx =  ((fogStripZmin + z ) % mapData.MAP_SIZE_Z ) * mapData.Z_STRIDE
                + (fogStripXmin + x ) % mapData.MAP_SIZE_X;
        mapData.mapColours[idx] = mapData.MAP_COLOUR_FOG;
      }
    }

    mapData.wxCentre = wxCentreNew;
    mapData.wzCentre = wzCentreNew;
  }

  /**
   * Updates a rectangular portion of the map with altitude and mapColours values calculated from the world
   *   mapColours will be adjusted for water depth but not yet for contours (requires subsequent call to createContours)
   * REQUIRES:
   * (1) mapData must have space allocated for both mapColours and altitude
   * (2) the rectangle is allowed to overlap the borders (wraparound is permitted)
   * @param world
   * @param mapxStart  the start (minimum) [mapx, mapz] coordinates [0 - MAP_SIZE_X]
   * @param mapzStart  the start (minimum) [mapx, mapz] coordinates [0 - MAP_SIZE_Z]
   * @param xCount the width of the rectangle to be updated [0 - MAP_SIZE_X]
   * @param zCount the height of the rectangle to be updated [0 - MAP_SIZE_Z]
   */
  protected void updateMapRectangularPortion(World world, int mapxStart, int mapzStart, int xCount, int zCount)
  {


    if (mapData == null ||  mapData.altitude == null || mapData.mapColours == null ) {
      FMLLog.severe("mapData not properly initialised in " + TargetingTabletMapServer.class.getCanonicalName());
      return;
    }
    if (mapxStart < 0 || mapxStart > mapData.MAP_SIZE_X) {
      FMLLog.severe("mapxStart out of range (%d) in " + TargetingTabletMapServer.class.getCanonicalName(), mapxStart);
      return;
    }
    if (mapzStart < 0 || mapzStart > mapData.MAP_SIZE_Z) {
      FMLLog.severe("mapzStart out of range (%d) in " + TargetingTabletMapServer.class.getCanonicalName(), mapzStart);
      return;
    }
    if (xCount < 0 || xCount > mapData.MAP_SIZE_X) {
      FMLLog.severe("xCount out of range (%d) in " + TargetingTabletMapServer.class.getCanonicalName(), xCount);
      return;
    }
    if (zCount < 0 || zCount > mapData.MAP_SIZE_Z) {
      FMLLog.severe("zCount out of range (%d) in " + TargetingTabletMapServer.class.getCanonicalName(), zCount);
      return;
    }

    int wxStart = mapData.wxCentre - mapData.MAP_X_RADIUS + (mapxStart - mapData.ixMinOffset);
    int wzStart = mapData.wzCentre - mapData.MAP_Z_RADIUS + (mapzStart - mapData.iZMinOffset);

    for (int zPos = 0; zPos < zCount; ++zPos) {
      for (int xPos = 0; xPos < xCount; ++xPos) {
        int wx = wxStart + xPos;
        int wz = wzStart + zPos;

        Chunk chunk = world.getChunkFromBlockCoords(wx, wz);

        int cx = wx & 15;
        int cz = wz & 15;
        int cy = 0;
        if (!chunk.isEmpty()) {
          cy = chunk.getHeightValue(cx, cz); // starting y is the air block just above the ground
        }
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

        boolean inWater = nextBlockDownID != 0 && Block.blocksList[nextBlockDownID].blockMaterial.materialMapColor == MapColor.waterColor;
        int waterDepthPaletteOffset = TabletMapData.MAP_COLOUR_BASE;
        if (inWater) {
          while (inWater && cy > 0) {
            nextBlockDownID = chunk.getBlockID(cx, cy - 1, cz);
            if (nextBlockDownID != 0 && !(Block.blocksList[nextBlockDownID].blockMaterial.materialMapColor == MapColor.waterColor)) {
              inWater = false;
            } else {
              --cy;
            }
          }

          int waterDepth = wyTopSolidBlockPlusOne - cy;
          int ditherDepth = 2 * (cx + cz & 1);
          waterDepth += ditherDepth;
          if (waterDepth < 5) {
            waterDepthPaletteOffset = TabletMapData.MAP_COLOUR_LIGHTER;
          } else if (waterDepth < 10) {
            waterDepthPaletteOffset = TabletMapData.MAP_COLOUR_BASE;
          } else {
            waterDepthPaletteOffset = TabletMapData.MAP_COLOUR_DARKER;
          }
        }
        int colourIndex = 0;
        if (nextBlockDownID != 0) {
          colourIndex = Block.blocksList[nextBlockDownID].blockMaterial.materialMapColor.colorIndex;
        }
        colourIndex = colourIndex * mapData.MAP_COLOUR_MULTIPLIER + waterDepthPaletteOffset;

        int idx =  ((mapzStart + zPos) % mapData.MAP_SIZE_Z) * mapData.Z_STRIDE
                + (mapxStart + xPos) % mapData.MAP_SIZE_X;
        mapData.mapColours[idx] = (byte)colourIndex;
        mapData.altitude[idx] = (byte)cy;
      }  // for wx
    } // for wz
  }

  /** Updates a rectangular region of the map with contour lines
   *  Both the mapColours and altitude must have previously been updated in this region for the contours to be valid
   *  Note - will update the contours for mapxStart+1 to mapxStart + xCount-1 inclusive.  likewise z.  i.e. the top row
   *      and the left column won't be updated.
   *  @param mapxStart  the start (minimum) [mapx, mapz] coordinates [0 - MAP_SIZE_X]
   *  @param mapzStart  the start (minimum) [mapx, mapz] coordinates [0 - MAP_SIZE_Z]
   *  @param xCount the width of the rectangle to be updated [0 - MAP_SIZE_X]
   *  @param zCount the height of the rectangle to be updated [0 - MAP_SIZE_Z]
  */
  protected void updateContours(int mapxStart, int mapzStart, int xCount, int zCount)
  {
    for (int zPos = 1; zPos < zCount; ++zPos) {
      for (int xPos = 1; xPos < xCount; ++xPos) {

        int        idx =  ((mapzStart + zPos) % mapData.MAP_SIZE_Z) * mapData.Z_STRIDE
                         + (mapxStart + xPos) % mapData.MAP_SIZE_X;

        if (    mapData.mapColours[idx] != mapData.MAP_COLOUR_FOG
            &&  mapData.mapColours[idx] / mapData.MAP_COLOUR_MULTIPLIER != MapColor.waterColor.colorIndex) {  // contours for non-fog and non-water only
          int idxPrevRow =  ((mapzStart + zPos-1) % mapData.MAP_SIZE_Z) * mapData.Z_STRIDE
                           + (mapxStart + xPos) % mapData.MAP_SIZE_X;
          int idxPrevCol =  ((mapzStart + zPos) % mapData.MAP_SIZE_Z) * mapData.Z_STRIDE
                           + (mapxStart + xPos-1) % mapData.MAP_SIZE_X;

          int altitudeAtIdx = mapData.altitude[idx] & 0xff;  // convert to unsigned
          int altitudePrevRow = mapData.altitude[idxPrevRow] & 0xff;
          int altitudePrevCol = mapData.altitude[idxPrevCol] & 0xff;

          int contourPaletteOffset = TabletMapData.MAP_COLOUR_BASE;
          if (altitudeAtIdx > altitudePrevRow) {
            contourPaletteOffset = TabletMapData.MAP_COLOUR_LIGHTER;
          } else if (altitudeAtIdx < altitudePrevRow) {
            contourPaletteOffset = TabletMapData.MAP_COLOUR_DARKER;
          } else if (altitudeAtIdx > altitudePrevCol) {
            contourPaletteOffset = TabletMapData.MAP_COLOUR_LIGHTER;
          } else if (altitudeAtIdx < altitudePrevCol) {
            contourPaletteOffset = TabletMapData.MAP_COLOUR_DARKER;
          }

          int newColour = mapData.mapColours[idx] & 0xff;         // convert to unsigned
          newColour -= newColour % mapData.MAP_COLOUR_MULTIPLIER;
          newColour += contourPaletteOffset;
          mapData.mapColours[idx] = (byte)newColour;
        } // non-water
      }  // for wx
    } // for wz
  }

  private TabletMapDataWithAltitude mapData;

}
