package aerialbombardment.clientonly.eventhandlers;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import aerialbombardment.clientonly.InventoryPlayerInterceptor;
import aerialbombardment.common.items.RegistryForItems;

import java.util.EnumSet;


public class ClientTickHandler implements ITickHandler
{
  InventoryPlayerInterceptor mouseWheelInterceptor;

  public EnumSet<TickType> ticks()
  {
    return EnumSet.of(TickType.CLIENT);
  }

  // at regular intervals while the player is holding ItemSmileyFace, play a sound effect

  public void tickStart(EnumSet<TickType> type, Object... tickData)
  {
    if (!type.contains(TickType.CLIENT)) return;

    EntityClientPlayerMP entityClientPlayerMP = Minecraft.getMinecraft().thePlayer;
    if (entityClientPlayerMP != null) {
      InventoryPlayer inventoryPlayer = entityClientPlayerMP.inventory;
      if (!(inventoryPlayer instanceof InventoryPlayerInterceptor)) {
        mouseWheelInterceptor = new InventoryPlayerInterceptor(inventoryPlayer);
        Minecraft.getMinecraft().thePlayer.inventory = mouseWheelInterceptor;
        mouseWheelInterceptor.setInterceptionActive(true);
      }
    }

    final int SOUND_EFFECT_PERIOD_IN_TICKS = 40;
    EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
    if (player != null) {
      ItemStack currentItem = player.inventory.getCurrentItem();
      if (currentItem != null && currentItem.getItem() == RegistryForItems.itemSmileyFace) {
        if (++tickCount >= SOUND_EFFECT_PERIOD_IN_TICKS) {
          tickCount = 0;
          Minecraft.getMinecraft().sndManager.playSound("testframework:MultiBlockPlace",
                  (float) (player.posX), (float) (player.posY), (float) (player.posZ),
                  1.0F, 1.0F);
        }
      }
    }
  }

  public void tickEnd(EnumSet<TickType> type, Object... tickData)
  {
    if (!type.contains(TickType.CLIENT)) return;
    if (mouseWheelInterceptor != null) {
      int mouseDelta = mouseWheelInterceptor.retrieveLastMouseWheelDelta();
      if (mouseDelta != 0) {
        System.out.println("Mouse Wheel:" + mouseDelta);
      }
    }
  }

  public String getLabel()
  {
    return "ClientTickHandler";
  }

  private static int tickCount = 0;

}
