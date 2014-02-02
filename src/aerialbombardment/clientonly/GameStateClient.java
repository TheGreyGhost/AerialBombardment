package aerialbombardment.clientonly;

/**
 * User: The Grey Ghost
 * Date: 2/02/14
 */
public class GameStateClient
{
  GameStateClient()
  {
    targetingTabletMapClient = new TargetingTabletMapClient(0, 0);
  }

  public static GameStateClient getGameStateClient() {
    return gameStateClient;
  }

  public TargetingTabletMapClient getTargetingTabletMapClient() {
    return targetingTabletMapClient;
  }
  private TargetingTabletMapClient targetingTabletMapClient;
  private static GameStateClient gameStateClient = new GameStateClient();
}