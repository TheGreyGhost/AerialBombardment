package aerialbombardment.clientonly.renderers;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/*
This class is the renderer for BlockPyramid
It shows how to use ISimpleBlockRenderingHandler to draw a 3D block, both as a block in the world and as an item.
 */

public class ISBRendererBlockPyramid implements ISimpleBlockRenderingHandler {

  public final static int myRenderID = RenderingRegistry.getNextAvailableRenderId();

  public ISBRendererBlockPyramid() {
  }

  @Override
  public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
    Tessellator tessellator = Tessellator.instance;

    // if you don't perform this translation, the item won't sit in the player's hand properly in 3rd person view
    GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

    // for "inventory" blocks (actually for items which are equipped, dropped, or in inventory), should render in [0,0,0] to [1,1,1]
    tessellator.startDrawingQuads();
    renderPyramid(tessellator, 0.0, 0.0, 0.0);
    tessellator.draw();

    // don't forget to undo the translation you made at the start
    GL11.glTranslatef(0.5F, 0.5F, 0.5F);

  }

  @Override
  public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
    Tessellator tessellator = Tessellator.instance;

    // world blocks should render in [x,y,z] to [x+1, y+1, z+1]
    //     tessellator.startDrawingQuads() has already been called by the caller

    int lightValue = block.getMixedBrightnessForBlock(world, x, y, z);
    tessellator.setBrightness(lightValue);
    tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

    renderPyramid(tessellator, (double)x, (double)y, (double) z);
    //     tessellator.draw() will be called by the caller after return
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

  // render the pyramid at the given [x,y,z]
  // call tessellator.startDrawingQuads() before and tessellator.draw() afterwards

private void renderPyramid(Tessellator tessellator, double x, double y, double z) {

    // Tetrahedron drawn using quads where the first and second point are the same, so the face ends up as a triangle instead of a quad
    // use the Lapis lazuli block texture for the sides

    // we need to use setNormal in order to get the lighting correct, otherwise the faces will all look dark.
    // for cubes this easy because the normal points in the direction of the x,y, or z axis
    // for our pyramid it's harder - the normalised cross product of two edges of each face
    //   for example, for the east face the normal is [0.8944, 0.4472, 0]
    //   http://www.wolframalpha.com/input/?i=%5B0%2C0%2C-1%5D+cross+product+%5B-0.5%2C+1.0%2C+-0.5%5D
    final float FACE_XZ_NORMAL = 0.8944F;
    final float FACE_Y_NORMAL  = 0.4472F;

    Icon icon1 = Block.blockLapis.getIcon(0, 0);
    double minU1 = (double)icon1.getMinU();
    double minV1 = (double)icon1.getMinV();
    double maxU1 = (double)icon1.getMaxU();
    double maxV1 = (double)icon1.getMaxV();

    // east face
    tessellator.setNormal(FACE_XZ_NORMAL, FACE_Y_NORMAL, 0.0F);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+0.0, maxU1, maxV1);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+0.0, maxU1, minV1);
    tessellator.addVertexWithUV(x+0.5, y+1.0, z+0.5, minU1, minV1);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+1.0, minU1, maxV1);
    tessellator.draw();

    // west face
    tessellator.startDrawingQuads();
    tessellator.setNormal(-FACE_XZ_NORMAL, FACE_Y_NORMAL, 0.0F);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+1.0, minU1, maxV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+1.0, minU1, minV1);
    tessellator.addVertexWithUV(x+0.5, y+1.0, z+0.5, maxU1, minV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+0.0, maxU1, maxV1);
    tessellator.draw();

    // north face
    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, FACE_Y_NORMAL, -FACE_XZ_NORMAL);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+0.0, minU1, maxV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+0.0, minU1, minV1);
    tessellator.addVertexWithUV(x+0.5, y+1.0, z+0.5, maxU1, minV1);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+0.0, maxU1, maxV1);
    tessellator.draw();

    // south face
    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, FACE_Y_NORMAL, FACE_XZ_NORMAL);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+1.0, minU1, maxV1);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+1.0, minU1, minV1);
    tessellator.addVertexWithUV(x+0.5, y+1.0, z+0.5, maxU1, minV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+1.0, maxU1, maxV1);
    tessellator.draw();

    // bottom face
    tessellator.startDrawingQuads();
    tessellator.setNormal(0.0F, -1.0F, 0.0F);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+0.0, minU1, maxV1);
    tessellator.addVertexWithUV(x+1.0, y+0.0, z+1.0, minU1, minV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+1.0, maxU1, minV1);
    tessellator.addVertexWithUV(x+0.0, y+0.0, z+0.0, maxU1, maxV1);

  }

}