package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput.Compression;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

class ChunkDataInputBacked implements Chunk
{
   private final MCAPDataInput dataInput;
   /**
    * Earliest message log_time in the chunk. Zero if the chunk has no messages.
    */
   private final long messageStartTime;
   /**
    * Latest message log_time in the chunk. Zero if the chunk has no messages.
    */
   private final long messageEndTime;
   /**
    * Uncompressed size of the records field.
    */
   private final long recordsUncompressedLength;
   /**
    * CRC32 checksum of uncompressed records field. A value of zero indicates that CRC validation
    * should not be performed.
    */
   private final long uncompressedCrc32;
   /**
    * compression algorithm. i.e. zstd, lz4, "". An empty string indicates no compression. Refer to
    * well-known compression formats.
    */
   private final String compression;
   /**
    * Offset position of the records in either in the {@code  ByteBuffer} or {@code FileChannel},
    * depending on how this chunk was created.
    */
   private final long recordsOffset;
   /**
    * Length of the records in bytes.
    */
   private final long recordsCompressedLength;
   /**
    * The decompressed records.
    */
   private WeakReference<Records> recordsRef;

   ChunkDataInputBacked(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      messageStartTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageStartTime");
      messageEndTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageEndTime");
      recordsUncompressedLength = MCAP.checkPositiveLong(dataInput.getLong(), "uncompressedSize");
      uncompressedCrc32 = dataInput.getUnsignedInt();
      compression = dataInput.getString();
      recordsCompressedLength = MCAP.checkPositiveLong(dataInput.getLong(), "recordsLength");
      recordsOffset = dataInput.position();
      MCAP.checkLength(elementLength, getElementLength());
   }

   @Override
   public long getElementLength()
   {
      return 3 * Long.BYTES + 2 * Integer.BYTES + compression.length() + Long.BYTES + (int) recordsCompressedLength;
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
   public long uncompressedSize()
   {
      return recordsUncompressedLength;
   }

   /**
    * CRC-32 checksum of uncompressed `records` field. A value of zero indicates that CRC validation
    * should not be performed.
    */
   @Override
   public long uncompressedCrc32()
   {
      return uncompressedCrc32;
   }

   @Override
   public String compression()
   {
      return compression;
   }

   @Override
   public long recordsOffset()
   {
      return recordsOffset;
   }

   @Override
   public long recordsLength()
   {
      return recordsCompressedLength;
   }

   @Override
   public Records records()
   {
      Records records = recordsRef == null ? null : recordsRef.get();

      if (records != null)
         return records;

      if (compression.equalsIgnoreCase(""))
      {
         records = Records.load(dataInput, recordsOffset, (int) recordsCompressedLength);
      }
      else
      {
         ByteBuffer decompressedBuffer = dataInput.getDecompressedByteBuffer(recordsOffset,
                                                                             (int) recordsCompressedLength,
                                                                             (int) recordsUncompressedLength,
                                                                             Compression.fromString(compression),
                                                                             false);
         records = Records.load(MCAPDataInput.wrap(decompressedBuffer), 0, (int) recordsUncompressedLength);
      }

      recordsRef = new WeakReference<>(records);
      return records;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
