package aerialbombardment.common.network;


import aerialbombardment.clientonly.TabletMapData;
import aerialbombardment.common.TabletMapDataSnapshot;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChunkCoordinates;
import speedytools.common.blocks.BlockWithMetadata;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to inform the server when the user has used a SpeedyTool, and pass it information about the affected blocks.
 */
public class Packet250TabletMapData
{

  /**
   * Transmits the updated TabletMap to the client
   * @param tabletMapToTransmit the updated TabletMap
   * @param lastTransmission the last transmitted TabletMap (or null for none)
   * @throws IOException
   */
  public Packet250TabletMapData(TabletMapDataSnapshot tabletMapToTransmit, TabletMapDataSnapshot lastTransmission) throws IOException
  {
    if (tabletMapToTransmit == null) {
      FMLLog.severe("tabletMapToTransmit is null in " + Packet250TabletMapData.class.getCanonicalName());
      return;
    }

    TabletMapDataSnapshot workingTabletMap;
    if (lastTransmission == null) {
      workingTabletMap = tabletMapToTransmit;
    } else {
      workingTabletMap = calculateDelta(tabletMapToTransmit, lastTransmission);
    }

    int blockID = (blockToPlace == null) ? 0 : blockToPlace.block.blockID;
    int metaData = (blockToPlace == null) ? 0 : blockToPlace.metaData;

    ByteArrayOutputStream bos = new ByteArrayOutputStream(1+ 4*5 + 12 * currentlySelectedBlocks.size());
    DataOutputStream outputStream = new DataOutputStream(bos);
    outputStream.writeByte(PacketHandler.PACKET250_SPEEDY_TOOL_USE_ID);
    outputStream.writeInt(toolItemID);
    outputStream.writeInt(button);
    outputStream.writeInt(blockID);
    outputStream.writeInt(metaData);
    outputStream.writeInt(currentlySelectedBlocks.size());

    for (ChunkCoordinates cc : currentlySelectedBlocks) {
      outputStream.writeInt(cc.posX);
      outputStream.writeInt(cc.posY);
      outputStream.writeInt(cc.posZ);
    }
    packet250 = new Packet250CustomPayload("speedytools",bos.toByteArray());
  }



  public Packet250CustomPayload getPacket250CustomPayload() {
    return packet250;
  }

  /**
   * Creates a Packet250SpeedyToolUse from Packet250CustomPayload
   * @param sourcePacket250
   * @return the converted packet, or null if failure
   */
  public Packet250TabletMapData(Packet250CustomPayload sourcePacket250)
  {
    packet250 = sourcePacket250;
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet250.data));

    try {
      byte packetID = inputStream.readByte();
      if (packetID != PacketHandler.PACKET250_SPEEDY_TOOL_USE_ID) return;

      toolItemID = inputStream.readInt();
      button = inputStream.readInt();
      int blockID = inputStream.readInt();

      blockToPlace = new BlockWithMetadata();
      blockToPlace.block = (blockID == 0) ? null : Block.blocksList[blockID];
      blockToPlace.metaData = inputStream.readInt();

      int blockCount = inputStream.readInt();
      for (int i = 0; i < blockCount; ++i) {
        ChunkCoordinates newCC = new ChunkCoordinates();
        newCC.posX = inputStream.readInt();
        newCC.posY = inputStream.readInt();
        newCC.posZ = inputStream.readInt();
        currentlySelectedBlocks.add(newCC);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean deltaCompression = false;
  private Packet250CustomPayload packet250 = null;
}
