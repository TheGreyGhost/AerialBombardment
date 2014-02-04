package aerialbombardment.common;

import aerialbombardment.clientonly.TabletMapData;

/**
 * User: The Grey Ghost
 * Date: 5/02/14
 */
public class TabletMapDataSnapshot
{
  TabletMapDataSnapshot(TabletMapData initTabletMapData, long initMasterTickCount)
  {
    tabletMapData = initTabletMapData;
    masterTickCount = initMasterTickCount;
  }

  public TabletMapData tabletMapData;
  public long masterTickCount;
}
