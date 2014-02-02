package aerialbombardment.common.network;

import aerialbombardment.clientonly.GameStateClient;
import aerialbombardment.clientonly.TargetingTabletMapClient;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import speedytools.serveronly.SpeedyToolWorldManipulator;

public class PacketHandler implements IPacketHandler
{
  public static final byte PACKET250_TABLETMAPDATA_ID = 0;

  @Override
  public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player playerEntity)
  {
    if (packet.channel.equals("aerialbombardment")) {
      Side side = FMLCommonHandler.instance().getEffectiveSide();

      switch (packet.data[0]) {
        case PACKET250_TABLETMAPDATA_ID: {
          Packet250TabletMapData tabletMapDataPacket = new Packet250TabletMapData(packet);
          GameStateClient.getGameStateClient().getTargetingTabletMapClient().updateMapFromPacket(tabletMapDataPacket);
          break;
        }

        default: {
          malformedPacketError(playerEntity, "Malformed Packet250SpeedyTools:Invalid packet type ID");
          return;
        }

      }
    }
  }

  private void malformedPacketError(Player player, String message) {
    Side side = FMLCommonHandler.instance().getEffectiveSide();
    switch (side) {
      case CLIENT: {
        Minecraft.getMinecraft().getLogAgent().logWarning(message);
        break;
      }
      case SERVER: {
        MinecraftServer.getServer().getLogAgent().logWarning(message);
        break;
      }
      default:
        assert false: "invalid Side";
    }
  }
}

