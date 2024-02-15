package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPByteBufferDataOutput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public class MutableChunk implements Chunk
{
   private long messageStartTime;
   private long messageEndTime;
   private long recordsUncompressedLength;
   private long uncompressedCrc32;
   private Compression compression;
   private long recordsCompressedLength;

   private Records records;
   private long elementLength = -1L;

   public void setMessageStartTime(long messageStartTime)
   {
      this.messageStartTime = messageStartTime;
   }

   public void setMessageEndTime(long messageEndTime)
   {
      this.messageEndTime = messageEndTime;
   }

   public void setRecordsUncompressedLength(long recordsUncompressedLength)
   {
      this.recordsUncompressedLength = recordsUncompressedLength;
   }

   public void setUncompressedCrc32(long uncompressedCrc32)
   {
      this.uncompressedCrc32 = uncompressedCrc32;
   }

   public void setCompression(Compression compression)
   {
      this.compression = compression;
   }

   public void setRecords(Records records)
   {
      elementLength = -1L;
      this.records = records;
   }

   @Override
   public long messageStartTime()
   {
      return messageStartTime;
   }

   @Override
   public long messageEndTime()
   {
      return messageEndTime;
   }

   @Override
   public long recordsUncompressedLength()
   {
      return recordsUncompressedLength;
   }

   @Override
   public long uncompressedCRC32()
   {
      return uncompressedCrc32;
   }

   @Override
   public Compression compression()
   {
      return compression;
   }

   @Override
   public long recordsCompressedLength()
   {
      return recordsCompressedLength;
   }

   @Override
   public Records records()
   {
      return records;
   }

   @Override
   public long getElementLength()
   {
      return elementLength;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(messageStartTime);
      dataOutput.putLong(messageEndTime);
      dataOutput.putLong(recordsUncompressedLength);
      dataOutput.putLong(uncompressedCrc32);
      dataOutput.putString(compression.getName());
      MCAPByteBufferDataOutput recordsBuffer = new MCAPByteBufferDataOutput((int) recordsUncompressedLength, 2);
      records.forEach(element -> element.write(recordsBuffer));
      recordsBuffer.close();

      recordsBuffer.putUnsignedInt(records.stream().mapToLong(MCAPElement::getElementLength).sum());
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
