package aerialbombardment.common.items;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.item.Item;

/**
 * creates and contains the instances of all of this mod's custom Items
 * the instances are created manually in order to control the creation time and order
 * NB - ItemBlocks (Item corresponding to a Block) are created in the RegistryForBlocks
 */
public class RegistryForItems
{
  // custom items
  public static Item itemSmileyFace;

  public static void initialise()
  {
    final int START_ITEM = 5000;
    itemSmileyFace = new ItemSmileyFace(START_ITEM);

   // for all Items:
   // LanguageRegistry for registering the name of the item.  renderers are registered elsewhere (client side only).
    LanguageRegistry.addName(itemSmileyFace, "Smiley Face Item");
  }
}
