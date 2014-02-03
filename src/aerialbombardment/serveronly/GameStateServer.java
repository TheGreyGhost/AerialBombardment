package aerialbombardment.serveronly;

import aerialbombardment.clientonly.TargetingTabletMapClient;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: The Grey Ghost
 * Date: 2/02/14
 */
public class GameStateServer
{
  GameStateServer()
  {
    playerTabletMaps = new HashMap<EntityPlayerMP, TargetingTabletMapServer>(8);
  }

  public static GameStateServer getGameStateServer() {
    return gameStateServer;
  }

  public TargetingTabletMapServer getTargetingTabletMapServer(EntityPlayerMP player)
  {
    return playerTabletMaps.get(player);
  }

  private Map<EntityPlayerMP, TargetingTabletMapServer> playerTabletMaps;
  private static GameStateServer gameStateServer = new GameStateServer();
}