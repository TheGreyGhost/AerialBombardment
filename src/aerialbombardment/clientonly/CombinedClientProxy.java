package aerialbombardment.clientonly;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import aerialbombardment.clientonly.eventhandlers.ClientTickHandler;
import aerialbombardment.clientonly.eventhandlers.CustomSoundsHandler;
import aerialbombardment.clientonly.eventhandlers.ItemEventHandler;
import aerialbombardment.clientonly.renderers.RegistryForRenderers;
import aerialbombardment.common.CommonProxy;


/**
 * CombinedClientProxy is used to set up the mod and start it running when installed on a standalone client.
 *   It should not contain any code necessary for proper operation on a DedicatedServer.  Code required for both
 *   CombinedClient and dedicated server should go into CommonProxy
 */
public class CombinedClientProxy extends CommonProxy {


  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  @Override
  public void preInit()
  {
    super.preInit();
    RegistryForRenderers.initialise();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  @Override
  public void load()
  {
    super.load();
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  @Override
  public void postInit()
  {
    super.postInit();
    MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
    MinecraftForge.EVENT_BUS.register(new CustomSoundsHandler());
    TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
  }
}
