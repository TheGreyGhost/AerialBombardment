package aerialbombardment.clientonly.EventHandlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.ForgeSubscribe;
import aerialbombardment.common.items.RegistryForItems;

/**
 Contains the custom Forge Event Handlers for Items
 */
public class ItemEventHandler {

  /**
   * If ItemSmileyFace is in the player's hand, draw nothing
   * Otherwise, cancel the event so that the normal selection box is drawn.
   * @param event
   */
  @ForgeSubscribe
  public void blockHighlightDecider(DrawBlockHighlightEvent event)
  {
    EntityPlayer player = event.player;
    ItemStack currentItem = player.inventory.getCurrentItem();

    if (currentItem == null || currentItem.getItem() != RegistryForItems.itemSmileyFace) {
      return;
    }

    event.setCanceled(true);
    return;
  }
}
