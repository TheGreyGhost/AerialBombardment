package aerialbombardment.clientonly;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 * stores the pixel values for the map
 * laid out as a 2D array in increasing order of x and z, it wraps around at the edges of the array
 *   minVisibleX and minVisibleZ give the location of the minimum visible x and z.
 * address of [worldx, worldz] is   (x - (wxCentre - radius - border) + ixMinOffset)
 *                                 + Z_STRIDE * ((z - (wzCentre - radius - border) + izMinOffset) % MAP_SIZE_Z) ;
 * a border of BORDER_SIZE surrounds the visible area.
 * The minz row of the altitude map is duplicated to the maxz plus one row (makes contour calcs easier)
 */
public class TabletMapData
{
  public TabletMapData(int wxCentreInit, int wzCentreInit, boolean needHeightMap) {
    ixMinOffset = 0;
    iZMinOffset = 0;
    wxCentre = wxCentreInit;
    wzCentre = wzCentreInit;

    pixelValue = new byte[MAP_STORAGE_SIZE];
    if (needHeightMap) {
      altitude = new byte[MAP_STORAGE_SIZE];
    }
  }

  public static final int VISIBLE_BLOCKS_X_RADIUS = 100;
  public static final int VISIBLE_BLOCKS_Z_RADIUS = 100;

  public static final int VISIBLE_BLOCKS_X = 2 * VISIBLE_BLOCKS_X_RADIUS + 1;
  public static final int VISIBLE_BLOCKS_Z = 2 * VISIBLE_BLOCKS_Z_RADIUS + 1;
  public static final int BORDER_SIZE = 20;

  public static final int MAP_SIZE_X = VISIBLE_BLOCKS_X + 2 * BORDER_SIZE;
  public static final int MAP_SIZE_Z = VISIBLE_BLOCKS_Z + 2 * BORDER_SIZE;
  public static final int MAP_STORAGE_SIZE = MAP_SIZE_X * (MAP_SIZE_Z + 1);     // 1 extra row to allow for x wraparound
  public static final int ALTITUDE_STORAGE_SIZE = MAP_SIZE_X * (MAP_SIZE_Z + 2);  // 1 extra row for x wraparound plus a further extra row for z wraparound (makes contour calcs easier)

  public static final int Z_STRIDE = MAP_SIZE_X;

  public int ixMinOffset = 0;  // x index corresponding to the minimum x of the border
  public int iZMinOffset = 0;  // z index corresponding to the minimum z of the border
  public int wxCentre = 0;
  public int wzCentre = 0;

  public byte pixelValue[];
  public byte altitude[];
}
