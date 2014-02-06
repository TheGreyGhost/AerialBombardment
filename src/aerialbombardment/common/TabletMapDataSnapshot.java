package aerialbombardment.common;

import aerialbombardment.clientonly.TabletMapData;
import cpw.mods.fml.common.FMLLog;

/**
 * User: The Grey Ghost
 * Date: 5/02/14
 */
public class TabletMapDataSnapshot
{
  public TabletMapDataSnapshot(TabletMapData initTabletMapData, int initMasterTickCount)
  {
    tabletMapData = initTabletMapData;
    masterTickCount = initMasterTickCount;
  }

  public TabletMapDataSnapshot clone()
  {
    try {
      TabletMapDataSnapshot retval = new TabletMapDataSnapshot(tabletMapData.clone(), masterTickCount);
      return retval;
    } catch (CloneNotSupportedException e) {
      FMLLog.severe("Threw exception %s in %s", e.getLocalizedMessage(), TabletMapDataSnapshot.class.getCanonicalName());
      return null;
    }
  }

  public TabletMapData tabletMapData;
  public int masterTickCount;
}
