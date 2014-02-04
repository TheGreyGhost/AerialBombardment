package aerialbombardment.serveronly.eventhandlers;

import aerialbombardment.clientonly.InventoryPlayerInterceptor;
import aerialbombardment.common.items.RegistryForItems;
import aerialbombardment.serveronly.GameStateServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;


public class ServerTickHandler implements ITickHandler
{
  public EnumSet<TickType> ticks()
  {
    return EnumSet.of(TickType.SERVER);
  }

  // at regular intervals while the player is holding ItemSmileyFace, play a sound effect

  public void tickStart(EnumSet<TickType> type, Object... tickData)
  {
    if (!type.contains(TickType.SERVER)) return;
    GameStateServer.getGameStateServer().tick();
  }

  public void tickEnd(EnumSet<TickType> type, Object... tickData)
  {
    return;
  }

  public String getLabel()
  {
    return "ServerTickHandler";
  }

  private static int tickCount = 0;

}
