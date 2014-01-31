package aerialbombardment.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockVariableHeight extends ItemBlock
{
  public ItemBlockVariableHeight(int id) {
    super(id);
    this.setMaxStackSize(64);
    this.setHasSubtypes(true);
    this.setCreativeTab(CreativeTabs.tabBlock);
    this.setUnlocalizedName("ItemBlockPyramid");
  }
  @Override
  public int getMetadata (int damageValue) {
    return damageValue;
  }

  @Override
  public String getUnlocalizedName(ItemStack itemstack) {
    //System.out.println(getUnlocalizedName() + ".height" + itemstack.getItemDamage());
    return getUnlocalizedName() + ".height" + itemstack.getItemDamage();
  }

}
