package aerialbombardment.common.network;


import aerialbombardment.clientonly.TabletMapData;
import aerialbombardment.common.TabletMapDataSnapshot;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.*;
import java.util.*;

/**
 * This class is used to transmit the TabletMapData from the server to the client.
 *   It will optionally use XOR compression to reduce the packet size (requires a copy of the last transmission)
 * Typical usage:
 * On the server:
 * (1) create a Packet250TabletMapData using the TabletMapDataSnapshot to be transmitted, and optionally the last transmission as well
 *     This creates a queue of Packet250CustomPayload to be sent
 * (2) repeatedly call getPacket250CustomPayload and send them until there are no more (returns null)
 *
 * On the client:
 * (1) every time the Packet250CustomPayload is received, call addSubPacket.  When all subPackets have been received, it will return a Packet250TabletMapData
 * (2) if .isXORCompressed is true, call undoXOR supplying the lastTransmission.
 * (3) call getTabletMapDataSnapshot
 */
public class Packet250TabletMapData
{
  /**
   * Transmits the updated TabletMap to the client.  The packet is broken up into smaller packets, to stay under the maximum packet size limit
   * Each sub-packet is transmitted with a header showing a packet ID, sub-packet number, and the total number of sub-packets.
   * @param tabletMapToTransmit the updated TabletMap
   * @param lastTransmission the last transmitted TabletMap (or null for none).  Used to reduce packet size.
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
    outputStream.writeInt(tabletMapToTransmit.masterTickCount);
    outputStream.writeBoolean(xorCompression);
    outputStream.writeInt(xorLastMasterTickCount);
    outputStream.writeInt(compressedColours.length);
    outputStream.write(compressedColours);

    int numberOfPackets = (bos.size() - 1) / PacketHandler.MAXIMUM_PACKET_SIZE + 1;
    if (numberOfPackets > Byte.MAX_VALUE) {
      if (tabletMapToTransmit == null) {
        FMLLog.severe("too many sub-packets (%d) in %s", numberOfPackets, Packet250TabletMapData.class.getCanonicalName());
        return;
      }
    }
    packet250Queue = new LinkedList<Packet250CustomPayload>();

    ByteArrayOutputStream subPacketBOS = new ByteArrayOutputStream();
    DataOutputStream subPacketDOS = new DataOutputStream(subPacketBOS);
    int inputBOSposition = 0;
    for (byte i = 0; i < numberOfPackets; ++i) {
      subPacketBOS.reset();
      subPacketDOS.writeInt(tabletMapToTransmit.masterTickCount);
      subPacketDOS.writeByte(i);
      subPacketDOS.writeByte((byte) numberOfPackets);

      int bytesToWrite = Math.min(bos.size() - inputBOSposition, PacketHandler.MAXIMUM_PACKET_SIZE);
      subPacketBOS.write(bos.toByteArray(), inputBOSposition, bytesToWrite);
      inputBOSposition += bytesToWrite;

      packet250Queue.add(new Packet250CustomPayload("aerialbombardment",subPacketBOS.toByteArray()));
    }
  }

  /**
   * Get the next packet for sending
   * @return the next packet, or null if all used.
   */

  public Packet250CustomPayload getPacket250CustomPayload() {
    return packet250Queue.poll();
  }

  /**
   * Adds an incoming sub-packet.  If all sub-packets have arrived, returns the full packet.  Otherwise, stores the sub-packet.
   * If an incoming sub-packet is newer than the stored sub-packets, the old sub-packets are discarded.
   * @param sourcePacket250
   * @return the full packet (if all sub-packets arrived), null otherwise.
   */
  public static Packet250TabletMapData addSubPacket(Packet250CustomPayload sourcePacket250)
  {
    try {
      DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(sourcePacket250.data));
      int pktMasterTickCount = inputStream.readInt();
      byte subPacketNumber = inputStream.readByte();
      byte totalSubPackets = inputStream.readByte();

      if (currentSubPacketsMasterTickCount != NO_CURRENT_PACKET_VALUE) {
        if (pktMasterTickCount < currentSubPacketsMasterTickCount) {
          FMLLog.warning("discarding incoming packet %d because store contains newer packet %d in %s",
                          pktMasterTickCount, currentSubPacketsMasterTickCount, Packet250TabletMapData.class.getCanonicalName());
          return null;
        }
        if (pktMasterTickCount > currentSubPacketsMasterTickCount) {
          FMLLog.warning("discarding stored packet %d because incoming packet %d is newer, in %s",
                          currentSubPacketsMasterTickCount, pktMasterTickCount, Packet250TabletMapData.class.getCanonicalName());
          subPackets = null;
        }
      }

      if (subPackets == null) {
        subPackets = new HashMap<Byte, Packet250CustomPayload>(totalSubPackets);
      }
      if (subPackets.containsKey(subPacketNumber)) {
        FMLLog.warning("ignored incoming subpacket %d with same number as stored subpacket, in %s",
                subPacketNumber, Packet250TabletMapData.class.getCanonicalName());
      } else {
        subPackets.put(subPacketNumber, sourcePacket250);
      }

      currentSubPacketsMasterTickCount = pktMasterTickCount;
      currentTotalSubPackets = totalSubPackets;
      if (subPackets.size() != currentTotalSubPackets) {
        return null;
      }

      return reassemblePacket();

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * if this packet is XOR encoded, undo it to recover the new map.
   * @param lastTransmissionReceived the last packet transmitted, or null if none.
   * @return true for success, or false if map is invalid and should be discarded (lastTransmissionReceived is null or doesn't match the timestamp)
   */
  public boolean undoXOR(TabletMapDataSnapshot lastTransmissionReceived)
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

  public TabletMapDataSnapshot getTabletMapDataSnapshot() {
    return incomingTabletMapData;
  }

  public boolean isXorCompressed() {
    return xorCompression;
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

  /**
   * reassemble the sub-packets into a single packet containing a TabletMapDataSnapshot incomingTabletMapData
   * @return the reassembled packet, or null if failed
   */
  private static Packet250TabletMapData reassemblePacket()
  {
    try {
      ByteArrayOutputStream rebuiltPacket = new ByteArrayOutputStream( );

      for (byte i = 0; i < currentTotalSubPackets; ++i) {
        rebuiltPacket.write(subPackets.get(i).data);
      }

      DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(rebuiltPacket.toByteArray()));
      Packet250TabletMapData retval = new Packet250TabletMapData(inputStream);
      if (retval.incomingTabletMapData == null) return null;
      return retval;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * create a Packet250TabletMapData from a packet's byte stream.
   * @param inputStream
   */
  private Packet250TabletMapData(DataInputStream inputStream)
  {
    try {
      byte packetID = inputStream.readByte();
      if (packetID != PacketHandler.PACKET250_TABLETMAPDATA_ID) {
        FMLLog.warning("unexpected packet ID %d in %s; ", packetID, Packet250TabletMapData.class.getCanonicalName());
        return;
      }

      TabletMapDataSnapshot newTabletMapData = new TabletMapDataSnapshot(new TabletMapData(), 0);
      newTabletMapData.tabletMapData.ixMinOffset = inputStream.readInt();
      newTabletMapData.tabletMapData.iZMinOffset = inputStream.readInt();
      newTabletMapData.tabletMapData.wxCentre = inputStream.readInt();
      newTabletMapData.tabletMapData.wzCentre = inputStream.readInt();
      newTabletMapData.masterTickCount = inputStream.readInt();
      xorCompression = inputStream.readBoolean();
      xorLastMasterTickCount = inputStream.readInt();
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
      newTabletMapData.tabletMapData.mapColours = decompressedNBT.getByteArray("map");

      if (!newTabletMapData.tabletMapData.validate()) {
        newTabletMapData = null;
      }
      incomingTabletMapData = newTabletMapData;

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static final int NO_CURRENT_PACKET_VALUE = -1;
  private static Map<Byte, Packet250CustomPayload> subPackets;
  private static int currentSubPacketsMasterTickCount = NO_CURRENT_PACKET_VALUE;
  private static byte currentTotalSubPackets = 0;

  private boolean xorCompression = false;
  private int xorLastMasterTickCount;

  private TabletMapDataSnapshot incomingTabletMapData;
  private Queue<Packet250CustomPayload> packet250Queue = null;
}
