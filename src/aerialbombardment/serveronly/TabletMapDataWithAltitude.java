package aerialbombardment.serveronly;

import aerialbombardment.clientonly.TabletMapData;

/**
 * User: The Grey Ghost
 * Date: 1/02/14
 * stores the pixel values for the map as per TabletMapData, as well as the corresponding altitude, which is necessary for generating contours
 * The altitude map contains the y value of the air block just above the highest solid block.  [0 - 255].
 * It is laid out in the same format as the mapColours array.
 */
public class TabletMapDataWithAltitude extends TabletMapData
{
  public TabletMapDataWithAltitude(int wxCentreInit, int wzCentreInit)
  {
    super(wxCentreInit, wzCentreInit);
    altitude = new byte[ALTITUDE_STORAGE_SIZE];
  }

  public static final int ALTITUDE_STORAGE_SIZE = TabletMapData.MAP_SIZE_X * TabletMapData.MAP_SIZE_Z;

  public byte altitude[];    // see class preamble for description

}
