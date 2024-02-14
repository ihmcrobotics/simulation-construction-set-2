package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public class MutableChunk implements Chunk
{
   private long messageStartTime;
   private long messageEndTime;
   private long recordsUncompressedLength;
   private long uncompressedCrc32;
   private Compression compression;
   private long recordsOffset;
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

   public void setRecordsOffset(long recordsOffset)
   {
      this.recordsOffset = recordsOffset;
   }

   public void setRecordsCompressedLength(long recordsCompressedLength)
   {
      elementLength = -1L;
      this.recordsCompressedLength = recordsCompressedLength;
   }

   public void setRecords(Records records)
   {
      elementLength = -1L;
      this.records = records;
   }

   private void updateElementLength()
   {
      if (elementLength != -1L)
         return;

      // TODO: Implement this method
      if (records != null)
         elementLength = 0L;
      else
         elementLength = -1L;
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
      dataOutput.putLong(recordsOffset);
      dataOutput.putCollection(records);
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
