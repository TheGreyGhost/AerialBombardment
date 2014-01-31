package aerialbombardment.clientonly.renderers;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import aerialbombardment.common.blocks.RegistryForBlocks;

/**
 * RegistryForRenderers is used to create and register all of the custom renderers used by the mod
 */
public class RegistryForRenderers
{
  // custom item renderers
  private final static IIRendererItemBlockNumberedFaces iiRendererItemBlockNumberedFaces = new IIRendererItemBlockNumberedFaces();
  private final static ISBRendererBlockPyramid isbRendererBlockPyramid = new ISBRendererBlockPyramid();
  private final static ISBRendererBlockVariableHeight isbRendererBlockVariableHeight = new ISBRendererBlockVariableHeight();

  public static void initialise() {
    // register IItemRenderers
    MinecraftForgeClient.registerItemRenderer(RegistryForBlocks.blockNumberedFaces.blockID, iiRendererItemBlockNumberedFaces);

    //register ISimpleBlockRenderingHandlers
    RenderingRegistry.registerBlockHandler(isbRendererBlockPyramid);
    RenderingRegistry.registerBlockHandler(isbRendererBlockVariableHeight);
  }
}
