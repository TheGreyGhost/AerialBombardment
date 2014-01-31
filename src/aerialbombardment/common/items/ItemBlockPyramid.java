package aerialbombardment.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

/*
This is the item that corresponds to BlockPyramid.
 */
public class ItemBlockPyramid extends ItemBlock {

  public ItemBlockPyramid(int id) {
    super(id);
    this.setMaxStackSize(64);
    this.setCreativeTab(CreativeTabs.tabBlock);
    this.setUnlocalizedName("ItemBlockPyramid");
  }
}

