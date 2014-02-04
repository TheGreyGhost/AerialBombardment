package aerialbombardment.serveronly;

import aerialbombardment.clientonly.TargetingTabletMapClient;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: The Grey Ghost
 * Date: 2/02/14
 * Maintains a record of all the server objects which define the game state.
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

  /**
   * @param player
   * @return the targeting tablet map for this player, or null if none
   */
  public TargetingTabletMapServer getTargetingTabletMapServer(EntityPlayerMP player)
  {
    return playerTabletMaps.get(player);
  }

  /**
   * adds a new player to the game.  no effect if already present.
   * @param newplayer
   * @return true if a new player was added, false if already present
   */
  public boolean addPlayer(EntityPlayerMP newplayer)
  {
    if (!(playerTabletMaps.containsKey(newplayer))) return false;
    playerTabletMaps.put(newplayer, new TargetingTabletMapServer(newplayer.serverPosX, newplayer.serverPosZ));
    return true;
  }

  /**
   * tick all the elements of the game state that need updating
   */
  public void tick()
  {
    int tabletMapCount = playerTabletMaps.size();
    int tabletNumber = 0;

    for (EntityPlayerMP player : playerTabletMaps.keySet()) {
      playerTabletMaps.get(player).tick(player.getEntityWorld(), player.serverPosX, player.serverPosZ, masterTickCount, tabletNumber, tabletMapCount);
      ++tabletNumber;
    }
    ++masterTickCount;
  }

  /**
   *
   * @return the master tick count for the server
   */
  public long getMasterTickCount() {
    return masterTickCount;
  }

  private Map<EntityPlayerMP, TargetingTabletMapServer> playerTabletMaps;
  private static GameStateServer gameStateServer = new GameStateServer();
  private long masterTickCount = 0;
}