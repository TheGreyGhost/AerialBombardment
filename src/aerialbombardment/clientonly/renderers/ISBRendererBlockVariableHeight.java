package aerialbombardment.clientonly.renderers;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import aerialbombardment.common.blocks.BlockVariableHeight;

/*
This class is the renderer for BlockPyramid
It shows how to use ISimpleBlockRenderingHandler to draw a 3D block, both as a block in the world and as an item.
 */

public class ISBRendererBlockVariableHeight implements ISimpleBlockRenderingHandler {

  public final static int myRenderID = RenderingRegistry.getNextAvailableRenderId();

  public ISBRendererBlockVariableHeight() {
  }

  @Override
  public void renderInventoryBlock(Block block, int metadata, int renderTypeID, RenderBlocks renderer)
  {
    BlockVariableHeight blockVariableHeight = (BlockVariableHeight)block;
    Tessellator tessellator = Tessellator.instance;

    // for "inventory" blocks (actually for items which are equipped, dropped, or in inventory), should render in [0,0,0] to [1,1,1], which
    //   requires you to translate it first
    // if you don't perform this translation, the item won't sit in the player's hand properly in 3rd person view
    GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

    float fractionalHeight = 1.0F * (metadata + 1.0F)/(blockVariableHeight.maxHeight()+1.0F);
    renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, fractionalHeight, 1.0F);
    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, -1.0F, 0.0F);
    renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 0));
    tessellator.setNormal(0.0F, 1.0F, 0.0F);
    renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 1));
    tessellator.setNormal(0.0F, 0.0F, -1.0F);
    renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 2));
    tessellator.setNormal(0.0F, 0.0F, 1.0F);
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 3));
    tessellator.setNormal(-1.0F, 0.0F, 0.0F);
    renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 4));
    tessellator.setNormal(1.0F, 0.0F, 0.0F);
    renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(block, 5));
    tessellator.draw();

    // don't forget to undo the translation you made at the start
    GL11.glTranslatef(0.5F, 0.5F, 0.5F);

  }

  @Override
  public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
  {
/*
    BlockVariableHeight blockVariableHeight = (BlockVariableHeight)block;
    int metadata = world.getBlockMetadata(x, y, z);

    float fractionalHeight = 1.0F * (metadata + 1.0F)/(blockVariableHeight.maxHeight()+1.0F);
    renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, fractionalHeight, 1.0F);
*/
    renderer.renderStandardBlock(block, x, y, z);

    return true;
  }

  @Override
  public boolean shouldRender3DInInventory() {
    return true;
  }

  @Override
  public int getRenderId() {
    return myRenderID;
  }

}