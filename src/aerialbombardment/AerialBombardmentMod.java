package aerialbombardment;

import aerialbombardment.common.CommonProxy;
import aerialbombardment.common.network.PacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

/**
   * This class is just used to pass control to the appropriate "Proxy" class depending on whether the mod has been installed in a Standalone Client or a Dedicated Server.
   */
  @Mod(modid="aerialbombardment", name="Aerial Bombardment", version="0.0.1")
  @NetworkMod(clientSideRequired=true, serverSideRequired=false,channels={"aerialbombardment"}, packetHandler = PacketHandler.class)
  public class AerialBombardmentMod
{
    // The instance of your mod that Forge uses.
    @Mod.Instance("aerialbombardment")
    public static AerialBombardmentMod instance;

    // the proxy reference will be set to either CombinedClientProxy or DedicatedServerProxy depending on whether this mod is running in a standalone client or a dedicated server.
    //  NB this is not the same as client-side vs server-side:
    //     CombinedClientProxy contains both client-side code and server-side code (used by the integrated server)
    //     DedicatedServerProxy doesn't contain any client-side code
    @SidedProxy(clientSide="aerialbombardment.clientonly.CombinedClientProxy", serverSide="aerialbombardment.serveronly.DedicatedServerProxy")
    public static CommonProxy proxy;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
      proxy.preInit();
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send FMLInterModComms messages to other mods.
     */
    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
      proxy.load();
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
      proxy.postInit();
    }


}
