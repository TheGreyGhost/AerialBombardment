package aerialbombardment.clientonly;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 * stores the pixel values for the map
 * laid out as a 2D array in increasing order of x and z, it wraps around at the edges of the array
 *   minVisibleX and minVisibleZ give the location of the minimum visible x and z.
 * pixel address of [worldx, worldz] is               (x - (wxCentre - radius - border) + ixMinOffset) % MAP_SIZE_X
 *                                      + Z_STRIDE * ((z - (wzCentre - radius - border) + izMinOffset) % MAP_SIZE_Z) ;
 * a border of BORDER_SIZE surrounds the visible area.
 *   The mapColours array contains the colour (palette index) of each pixel, calculated as:
 *     MAP_COLOUR_MULTIPLIER * BaseColourDependingOnMaterial + brightnessmodifier [0 - 2]
 *     The BaseColourDependingOnMaterial is taken from highestSolidBlock.blockMaterial.materialMapColor;
 *     brightnessmodifer is MAP_COLOUR_LIGHTER, MAP_COLOUR_BASE, or MAP_COLOUR_DARKER
 *     For water (materialMapColor is waterColor), this is calculated from the water depth.  For all others, it is calculated for contours (changes in altitude)
 */
public class TabletMapData
{
  public TabletMapData(int wxCentreInit, int wzCentreInit) {
    ixMinOffset = 0;
    iZMinOffset = 0;
    wxCentre = wxCentreInit;
    wzCentre = wzCentreInit;

    mapColours = new byte[MAP_STORAGE_SIZE];
  }

  public static final int BORDER_SIZE = 20;
  public static final int VISIBLE_BLOCKS_X_RADIUS = 100;
  public static final int VISIBLE_BLOCKS_Z_RADIUS = 100;
  public static final int MAP_X_RADIUS = VISIBLE_BLOCKS_X_RADIUS + BORDER_SIZE;
  public static final int MAP_Z_RADIUS = VISIBLE_BLOCKS_Z_RADIUS + BORDER_SIZE;

  public static final int VISIBLE_BLOCKS_X = 2 * VISIBLE_BLOCKS_X_RADIUS + 1;
  public static final int VISIBLE_BLOCKS_Z = 2 * VISIBLE_BLOCKS_Z_RADIUS + 1;

  public static final int MAP_SIZE_X = VISIBLE_BLOCKS_X + 2 * BORDER_SIZE;
  public static final int MAP_SIZE_Z = VISIBLE_BLOCKS_Z + 2 * BORDER_SIZE;
  public static final int MAP_STORAGE_SIZE = MAP_SIZE_X * MAP_SIZE_Z;

  public static final int Z_STRIDE = MAP_SIZE_X;

  public int ixMinOffset = 0;  // map x index corresponding to the minimum x of the border
  public int iZMinOffset = 0;  // map z index corresponding to the minimum z of the border
  public int wxCentre = 0;     // the world coordinates on which the map is centred [x, z]
  public int wzCentre = 0;     // the world coordinates on which the map is centred [x, z]

  public static final byte MAP_COLOUR_MULTIPLIER = 4;
  public static final byte MAP_COLOUR_LIGHTER = 2;
  public static final byte MAP_COLOUR_BASE = 1;
  public static final byte MAP_COLOUR_DARKER = 0;
  public static final byte MAP_COLOUR_FOG = (byte)255;

  public byte mapColours[];   // see class preamble for description
}
