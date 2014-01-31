package aerialbombardment.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.Icon;

/*
  This class is used to demonstrate IItemRenderer for an ItemBlock.
  The Block (when placed) is drawn using the vanilla renderer - by overriding getIcon(int side, int metadata)
  The corresponding ItemBlock is rendered using an implementation of IItemRenderer
  Three classes are associated with this Block:
  1) BlockNumberedFaces which is the Block placed in the world
  2) ItemBlockNumberedFaces which is the Item corresponding to the Block
  3) IIRendererItemBlockNumberedFaces, which is the IItemRenderer used to draw the Item.
*/

public class BlockNumberedFaces extends Block {

  private Icon[] faceIcons;    // holds the icons for each face 0 - 5.

  public BlockNumberedFaces(int id) {
    super(id, Material.ground);
    this.setCreativeTab(CreativeTabs.tabBlock);
    this.setUnlocalizedName("BlockColouredFacesIIR");
  }

  // A numbered icon for each face
  @Override
  public void registerIcons(IconRegister iconRegister) {
    faceIcons = new Icon[6];
    int i;
    for (i=0; i < faceIcons.length; ++i) {
      faceIcons[i] = iconRegister.registerIcon("testframework:NumberedFace"+i);
    }
  }

  @Override
  public Icon getIcon(int side, int metadata) {
     if (side < 0 || side > faceIcons.length) side = 0;
     return faceIcons[side];
  }
}
