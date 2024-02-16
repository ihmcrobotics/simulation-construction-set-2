package us.ihmc.scs2.session.mcap.specs.records;

import com.github.luben.zstd.ZstdCompressCtx;
import us.ihmc.scs2.session.mcap.encoding.LZ4FrameEncoder;
import us.ihmc.scs2.session.mcap.output.MCAPByteBufferDataOutput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;

public class MutableChunk implements Chunk
{
   private long messageStartTime;
   private long messageEndTime;
   private long recordsUncompressedLength = -1L;
   private long uncompressedCrc32;
   private Compression compression = Compression.LZ4;
   private long recordsCompressedLength = -1L;

   private Records records;

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
      if (compression == Compression.NONE)
         recordsCompressedLength = recordsUncompressedLength;
      else
         recordsCompressedLength = -1L;
   }

   public void setRecords(Records records)
   {
      this.records = records;
      recordsUncompressedLength = records.getElementLength();
      recordsCompressedLength = compression == Compression.NONE ? recordsUncompressedLength : -1L;
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
      if (recordsCompressedLength == -1)
         throw new IllegalStateException("The compressed length has not been set yet.");

      return 4 * Long.BYTES + Integer.BYTES + compression.getLength() + recordsCompressedLength;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(messageStartTime);
      dataOutput.putLong(messageEndTime);
      dataOutput.putLong(recordsUncompressedLength);
      dataOutput.putLong(uncompressedCrc32);
      dataOutput.putString(compression.getName());
      MCAPByteBufferDataOutput recordsOutput = new MCAPByteBufferDataOutput((int) recordsUncompressedLength, 2, compression == Compression.ZSTD);
      records.forEach(element -> element.write(recordsOutput));
      recordsOutput.close();

      switch (compression)
      {
         case NONE:
         {
            recordsCompressedLength = recordsUncompressedLength;
            dataOutput.putUnsignedInt(recordsCompressedLength);
            dataOutput.putByteBuffer(recordsOutput.getBuffer());
            break;
         }
         case LZ4:
         {
            LZ4FrameEncoder lz4FrameEncoder = new LZ4FrameEncoder();
            ByteBuffer compressedBuffer = lz4FrameEncoder.encode(recordsOutput.getBuffer(), null);
            recordsCompressedLength = compressedBuffer.remaining();
            dataOutput.putUnsignedInt(recordsCompressedLength);
            dataOutput.putByteBuffer(compressedBuffer);
            break;
         }
         case ZSTD:
         {
            try (ZstdCompressCtx zstdCompressCtx = new ZstdCompressCtx())
            {
               ByteBuffer compressedBuffer = zstdCompressCtx.compress(recordsOutput.getBuffer());
               recordsCompressedLength = compressedBuffer.remaining();
               dataOutput.putUnsignedInt(recordsCompressedLength);
               dataOutput.putByteBuffer(compressedBuffer);
            }
            break;
         }
         default:
         {
            throw new UnsupportedOperationException("Compression " + compression + " is not supported yet.");
         }
      }

      recordsOutput.putUnsignedInt(records.stream().mapToLong(MCAPElement::getElementLength).sum());
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
