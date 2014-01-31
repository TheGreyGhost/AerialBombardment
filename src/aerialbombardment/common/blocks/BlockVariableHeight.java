package aerialbombardment.common.blocks;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import aerialbombardment.clientonly.renderers.ISBRendererBlockVariableHeight;

import java.util.List;


public class BlockVariableHeight extends Block
{
  public BlockVariableHeight(int iID)
  {
    super(iID, Material.ground);
    this.setCreativeTab(CreativeTabs.tabBlock).setUnlocalizedName("blockVariableHeight").setTextureName("testframework:UpArrow");
  }

  //

  public void getSubBlocks(int paramBlockID, CreativeTabs par2CreativeTabs, List subBlockList) {
    for (int i = MAX_HEIGHT; i >= 0; i -= HEIGHT_INCREMENT) {
      subBlockList.add(new ItemStack(blockID, 1, i));
    }
  }

  public void registerSubBlocks()
  {
    for (int i = MAX_HEIGHT; i >= 0; i -= HEIGHT_INCREMENT) {
      ItemStack test = new ItemStack(this, 1, i);
      LanguageRegistry.addName(test, "Variable Height[" + i + "] Item");   // Add an item for every possible height of VariableHeight
    }
  }

  /**
   * Changes the height of the block (based on its metadata)
   */
  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int worldX, int worldY, int worldZ)
  {
      int height = par1IBlockAccess.getBlockMetadata(worldX, worldY, worldZ);
      float fractionalHeight = 1.0F * (height + 1.0F)/(MAX_HEIGHT+1.0F);
      this.setBlockBounds(0.0F, 0.0F, 0.0F,    1.0F, fractionalHeight, 1.0F);
  }

  static public int maxHeight()
  {
    return MAX_HEIGHT;
  }

  @Override
  public boolean isOpaqueCube()
  {
    return false;
  }

  @Override
  public int getRenderType() {
    return ISBRendererBlockVariableHeight.myRenderID;
  }

  static private final int MAX_HEIGHT = 15;
  static private final int HEIGHT_INCREMENT = 2;
}
