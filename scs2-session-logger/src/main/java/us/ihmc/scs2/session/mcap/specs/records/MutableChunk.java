package us.ihmc.scs2.session.mcap.specs.records;

import com.github.luben.zstd.ZstdCompressCtx;
import us.ihmc.scs2.session.mcap.encoding.LZ4FrameEncoder;
import us.ihmc.scs2.session.mcap.encoding.MCAPCRC32Helper;
import us.ihmc.scs2.session.mcap.output.MCAPByteBufferDataOutput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class MutableChunk implements Chunk
{
   private long messageStartTime;
   private long messageEndTime;
   private long recordsUncompressedLength = -1L;
   private long uncompressedCRC32;
   private Compression compression = Compression.LZ4;
   private long recordsCompressedLength = -1L;

   private Records records;
   private ByteBuffer recordsCompressedData;

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

   public void setUncompressedCRC32(long uncompressedCRC32)
   {
      this.uncompressedCRC32 = uncompressedCRC32;
   }

   public void setCompression(Compression compression)
   {
      this.compression = compression;
      if (compression == Compression.NONE)
         recordsCompressedLength = recordsUncompressedLength;
      else
         recordsCompressedLength = -1L;
   }

   public void setRecords(Records records)
   {
      this.records = records;
      recordsUncompressedLength = records.getElementLength();
      recordsCompressedData = null;
      recordsCompressedLength = -1L;
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
      return uncompressedCRC32;
   }

   @Override
   public Compression compression()
   {
      return compression;
   }

   @Override
   public long recordsCompressedLength()
   {
      getRecordsCompressedBuffer(); // Make sure the compressed data is available.
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
      getRecordsCompressedBuffer(); // Make sure the compressed data is available.
      return 4 * Long.BYTES + Integer.BYTES + compression.getLength() + recordsCompressedLength;
   }

   @Override
   public ByteBuffer getRecordsCompressedBuffer(boolean directBuffer)
   {
      //      if (recordsCompressedData != null)
      //         return recordsCompressedData;

      Objects.requireNonNull(compression, "The compression has not been set yet.");
      Objects.requireNonNull(records, "The records have not been set yet.");

      ByteBuffer uncompressedBuffer = getRecordsUncompressedBuffer(compression == Compression.ZSTD);

      recordsCompressedData = switch (compression)
      {
         case NONE:
         {
            recordsCompressedLength = recordsUncompressedLength;
            yield uncompressedBuffer;
         }
         case LZ4:
         {
            LZ4FrameEncoder lz4FrameEncoder = new LZ4FrameEncoder();
            ByteBuffer compressedBuffer = lz4FrameEncoder.encode(uncompressedBuffer, null);
            recordsCompressedLength = compressedBuffer.remaining();
            yield compressedBuffer;
         }
         case ZSTD:
         {
            try (ZstdCompressCtx zstdCompressCtx = new ZstdCompressCtx())
            {
               ByteBuffer compressedBuffer = zstdCompressCtx.compress(uncompressedBuffer);
               recordsCompressedLength = compressedBuffer.remaining();
               yield compressedBuffer;
            }
         }
      };
      recordsCompressedData.order(ByteOrder.LITTLE_ENDIAN);
      return recordsCompressedData;
   }

   @Override
   public ByteBuffer getRecordsUncompressedBuffer(boolean directBuffer)
   {
      MCAPByteBufferDataOutput recordsOutput = new MCAPByteBufferDataOutput((int) recordsUncompressedLength, 2, directBuffer);
      records.forEach(element -> element.write(recordsOutput));
      recordsOutput.close();
      return recordsOutput.getBuffer();
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(messageStartTime);
      dataOutput.putLong(messageEndTime);
      dataOutput.putLong(recordsUncompressedLength);
      dataOutput.putUnsignedInt(uncompressedCRC32);
      dataOutput.putString(compression.getName());
      dataOutput.putLong(recordsCompressedLength);
      dataOutput.putByteBuffer(getRecordsCompressedBuffer());
   }

   @Override
   public MCAPCRC32Helper updateCRC(MCAPCRC32Helper crc32)
   {
      if (crc32 == null)
         crc32 = new MCAPCRC32Helper();
      crc32.addLong(messageStartTime);
      crc32.addLong(messageEndTime);
      crc32.addLong(recordsUncompressedLength);
      crc32.addUnsignedInt(uncompressedCRC32);
      crc32.addString(compression.getName());
      crc32.addLong(recordsCompressedLength);
      crc32.addByteBuffer(getRecordsCompressedBuffer());
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
      return object instanceof Chunk other && Chunk.super.equals(other);
   }
}
