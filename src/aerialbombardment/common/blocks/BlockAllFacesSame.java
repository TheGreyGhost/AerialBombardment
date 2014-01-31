package aerialbombardment.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;


public class BlockAllFacesSame extends Block
{
  public BlockAllFacesSame(int iID)
  {
    super(iID, Material.ground);
    this.setCreativeTab(CreativeTabs.tabBlock).setUnlocalizedName("blockAllFacesSame").setTextureName("testframework:UpArrow");
  }

}
