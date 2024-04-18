package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.encoding.MCAPCRC32Helper;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MutableMessage implements Message
{
   private int channelId;
   private long sequence;
   private long logTime;
   private long publishTime;
   private long dataOffset;
   private int dataLength;
   private byte[] messageData;
   private ByteBuffer messageDataBuffer;

   public MutableMessage()
   {
   }

   public MutableMessage(int channelId, byte[] data)
   {
      this.channelId = channelId;
      this.messageData = data;
      this.dataLength = data.length;
   }

   @Override
   public int channelId()
   {
      return channelId;
   }

   public void setChannelId(int channelId)
   {
      this.channelId = channelId;
   }

   @Override
   public long sequence()
   {
      return sequence;
   }

   public void setSequence(long sequence)
   {
      this.sequence = sequence;
   }

   @Override
   public long logTime()
   {
      return logTime;
   }

   public void setLogTime(long logTime)
   {
      this.logTime = logTime;
   }

   @Override
   public long publishTime()
   {
      return publishTime;
   }

   public void setPublishTime(long publishTime)
   {
      this.publishTime = publishTime;
   }

   @Override
   public int dataLength()
   {
      return dataLength;
   }

   public void setDataLength(int dataLength)
   {
      this.dataLength = dataLength;
   }

   @Override
   public byte[] messageData()
   {
      return messageData;
   }

   @Override
   public ByteBuffer messageBuffer()
   {
      if (messageDataBuffer == null || messageDataBuffer.array() != messageData)
         messageDataBuffer = ByteBuffer.wrap(messageData).order(ByteOrder.LITTLE_ENDIAN);
      return messageDataBuffer;
   }

   public void setMessageData(byte[] messageData)
   {
      this.messageData = messageData;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedShort(channelId());
      dataOutput.putUnsignedInt(sequence());
      dataOutput.putLong(logTime());
      dataOutput.putLong(publishTime());
      dataOutput.putBytes(messageData(), 0, dataLength());
   }

   @Override
   public MCAPCRC32Helper updateCRC(MCAPCRC32Helper crc32)
   {
      if (crc32 == null)
         crc32 = new MCAPCRC32Helper();
      crc32.addUnsignedShort(channelId());
      crc32.addUnsignedInt(sequence());
      crc32.addLong(logTime());
      crc32.addLong(publishTime());
      crc32.addBytes(messageData(), 0, dataLength());
      return crc32;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public boolean equals(Object object)
   {
      return object instanceof Message other && Message.super.equals(other);
   }
}
