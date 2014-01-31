package aerialbombardment.common;

import aerialbombardment.common.blocks.RegistryForBlocks;
import aerialbombardment.common.items.RegistryForItems;

/**
 * CommonProxy is used to set up the mod and start it running.  It contains all the code that should run on both the
 *   Standalone client and the dedicated server.
 */
public class CommonProxy {

  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
    RegistryForBlocks.initialise();
    RegistryForItems.initialise();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void load()
  {

  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {

  }
}
