package aerialbombardment.common.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.item.ItemBlock;
import aerialbombardment.common.items.ItemBlockNumberedFaces;
import aerialbombardment.common.items.ItemBlockPyramid;
import aerialbombardment.common.items.ItemBlockVariableHeight;

/**
 * creates and contains the instances of all of this mod's custom Blocks (and any items associated with those Blocks)
 * the instances are created manually in (eg) createBlockInstances() in order to control the creation time and order
 */
public class RegistryForBlocks
{
  // custom blocks
  public static BlockAllFacesSame     blockAllFacesSame;
  public static BlockNumberedFaces blockNumberedFaces;
  public static BlockPyramid blockPyramid;
  public static BlockVariableHeight   blockVariableHeight;

  public static void initialise()
  {
    // create instances
    final int START_BLOCK = 500;
    blockAllFacesSame = new BlockAllFacesSame(START_BLOCK);
    blockNumberedFaces = new BlockNumberedFaces(START_BLOCK + 1);
    blockPyramid = new BlockPyramid(START_BLOCK + 2);
    blockVariableHeight = new BlockVariableHeight(START_BLOCK + 3);

    // for all Blocks:
    // GameRegistry for associating an item with a Block
    // LanguageRegistry for registering the name of the Block and the name of its associated Item
    // renderers are registered elsewhere (client side only).

    GameRegistry.registerBlock(blockAllFacesSame, ItemBlock.class, "blockAllFaceSame");
    LanguageRegistry.addName(blockAllFacesSame, "All Face Same Block");

    GameRegistry.registerBlock(blockNumberedFaces,ItemBlockNumberedFaces.class, "blockNumberedFaces");
    LanguageRegistry.addName(blockNumberedFaces, "Numbered Faces Block");

    GameRegistry.registerBlock(blockPyramid, ItemBlockPyramid.class, "blockPyramid");
    LanguageRegistry.addName(blockPyramid, "Pyramid Block");

    GameRegistry.registerBlock(blockVariableHeight, ItemBlockVariableHeight.class, "blockVariableHeight");
    LanguageRegistry.addName(blockVariableHeight, "Variable Height Block");
    blockVariableHeight.registerSubBlocks();
  }

}
