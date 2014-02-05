package aerialbombardment.common.network;


import aerialbombardment.clientonly.TabletMapData;
import aerialbombardment.common.TabletMapDataSnapshot;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import speedytools.common.blocks.BlockWithMetadata;

import java.io.*;

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

    // If a previous transmission is available, convert to a XOR map to improve subsequent compression, since most of the map probably hasn't changed.
    TabletMapDataSnapshot workingTabletMap;
    xorCompression = (lastTransmission != null);
    if (xorCompression) {
      workingTabletMap = createXORmap(tabletMapToTransmit, lastTransmission);
      xorLastMasterTickCount = lastTransmission.masterTickCount;
    } else {
      workingTabletMap = tabletMapToTransmit;
    }

    NBTTagCompound tabletMapNBT = new NBTTagCompound("TabletMapData");
    tabletMapNBT.setByteArray("map", workingTabletMap.tabletMapData.mapColours);

    byte[] compressedColours = CompressedStreamTools.compress(tabletMapNBT);

    ByteArrayOutputStream bos = new ByteArrayOutputStream(1+ 4*4 + 8 + 1 + 8 + 4 + compressedColours.length);
    DataOutputStream outputStream = new DataOutputStream(bos);
    outputStream.writeByte(PacketHandler.PACKET250_TABLETMAPDATA_ID);
    outputStream.writeInt(workingTabletMap.tabletMapData.ixMinOffset);
    outputStream.writeInt(workingTabletMap.tabletMapData.iZMinOffset);
    outputStream.writeInt(workingTabletMap.tabletMapData.wxCentre);
    outputStream.writeInt(workingTabletMap.tabletMapData.wzCentre);
    outputStream.writeLong(tabletMapToTransmit.masterTickCount);
    outputStream.writeBoolean(xorCompression);
    outputStream.writeLong(xorLastMasterTickCount);
    outputStream.writeInt(compressedColours.length);
    outputStream.write(compressedColours);

    packet250 = new Packet250CustomPayload("speedytools",bos.toByteArray());
  }

  /**
   * Subtract (XOR) the new map colours from the most-recently-transmitted map; if the map hasn't changed much, this will turn it
   *   into mostly zeros, which will compress much more than the map itself
   * @param tabletMapToTransmit the map to be transmitted
   * @param lastTransmission the most recently-transmitted map (may be null)
   * @return the delta map (other member variables are a direct copy of tabletMapToTransmit), or null if lastTransmission is null
   */
  private TabletMapDataSnapshot createXORmap(TabletMapDataSnapshot tabletMapToTransmit, TabletMapDataSnapshot lastTransmission)
  {
    if (lastTransmission == null) return null;
    TabletMapDataSnapshot xorSnapshot = tabletMapToTransmit.clone();
    byte [] colourDest = xorSnapshot.tabletMapData.mapColours;
    byte [] colourLast = lastTransmission.tabletMapData.mapColours;
    for (int i = colourDest.length - 1; i >= 0; --i) {
      colourDest[i] ^= colourLast[i];
    }
    return xorSnapshot;
  }

  public Packet250CustomPayload getPacket250CustomPayload() {
    return packet250;
  }

  /**
   * Creates a Packet250SpeedyToolUse from Packet250CustomPayload
   *
   */
  public Packet250TabletMapData(Packet250CustomPayload sourcePacket250)
  {
    packet250 = sourcePacket250;
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet250.data));

    try {
      byte packetID = inputStream.readByte();
      if (packetID != PacketHandler.PACKET250_TABLETMAPDATA_ID) return;

      incomingTabletMapData = new TabletMapDataSnapshot(new TabletMapData(), 0);
      incomingTabletMapData.tabletMapData.ixMinOffset = inputStream.readInt();
      incomingTabletMapData.tabletMapData.iZMinOffset = inputStream.readInt();
      incomingTabletMapData.tabletMapData.wxCentre = inputStream.readInt();
      incomingTabletMapData.tabletMapData.wzCentre = inputStream.readInt();
      incomingTabletMapData.masterTickCount = inputStream.readLong();
      xorCompression = inputStream.readBoolean();
      xorLastMasterTickCount = inputStream.readLong();
      int compressedLength = inputStream.readInt();
      byte [] compressedColours = new byte[compressedLength];
      int bytesRead = 0;
      bytesRead = inputStream.read(compressedColours);

      if (bytesRead != compressedLength) {
        FMLLog.warning("compressed data too short in %s; expected %d bytes but got %d bytes", Packet250TabletMapData.class.getCanonicalName(), compressedLength, bytesRead);
        return;
      }

      NBTTagCompound decompressedNBT;
      decompressedNBT = CompressedStreamTools.decompress(compressedColours);
      incomingTabletMapData.tabletMapData.mapColours = decompressedNBT.getByteArray("map");

      if (!incomingTabletMapData.tabletMapData.validate()) {
        incomingTabletMapData = null;
        return;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * if this packet is XOR encoded, undo it to recover the new map.
   * @param lastTransmissionReceived the last packet transmitted, or null if none.
   * @return true for success, or false if map is invalid and should be discarded (lastTransmissionReceived is null or doesn't match the timestamp)
   */
  public boolean reverseXOR(TabletMapDataSnapshot lastTransmissionReceived)
  {
    if (!xorCompression) return true;
    if (lastTransmissionReceived == null) return false;
    if (lastTransmissionReceived.masterTickCount != xorLastMasterTickCount) return false;
    byte [] colourDest = incomingTabletMapData.tabletMapData.mapColours;
    byte [] colourLast = lastTransmissionReceived.tabletMapData.mapColours;
    for (int i = colourDest.length - 1; i >= 0; --i) {
      colourDest[i] ^= colourLast[i];
    }
    return true;
  }

  private boolean xorCompression = false;
  private long xorLastMasterTickCount;
  private TabletMapDataSnapshot incomingTabletMapData;
  private Packet250CustomPayload packet250 = null;
}
