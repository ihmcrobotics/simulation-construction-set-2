package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

class ChunkDataInputBacked implements Chunk
{
   private final MCAPDataInput dataInput;
   private final long elementLength;
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
   private final long uncompressedCRC32;
   /**
    * compression algorithm. i.e. zstd, lz4, "". An empty string indicates no compression. Refer to
    * well-known compression formats.
    */
   private final Compression compression;
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
      this.elementLength = elementLength;

      dataInput.position(elementPosition);
      messageStartTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageStartTime");
      messageEndTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageEndTime");
      recordsUncompressedLength = MCAP.checkPositiveLong(dataInput.getLong(), "recordsUncompressedLength");
      uncompressedCRC32 = dataInput.getUnsignedInt();
      compression = Compression.fromString(dataInput.getString());
      recordsCompressedLength = MCAP.checkPositiveLong(dataInput.getLong(), "recordsCompressedLength");
      recordsOffset = dataInput.position();
      MCAP.checkLength(getElementLength(), 4 * Long.BYTES + Integer.BYTES + compression.getLength() + recordsCompressedLength);
   }

   @Override
   public long getElementLength()
   {
      return elementLength;
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

   /**
    * CRC-32 checksum of uncompressed `records` field. A value of zero indicates that CRC validation
    * should not be performed.
    */
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

   public long recordsOffset()
   {
      return recordsOffset;
   }

   @Override
   public long recordsCompressedLength()
   {
      return recordsCompressedLength;
   }

   @Override
   public Records records()
   {
      Records records = recordsRef == null ? null : recordsRef.get();

      if (records != null)
         return records;

      if (compression == Compression.NONE)
      {
         records = Records.load(dataInput, recordsOffset, (int) recordsCompressedLength);
      }
      else
      {
         ByteBuffer decompressedBuffer = dataInput.getDecompressedByteBuffer(recordsOffset,
                                                                             (int) recordsCompressedLength,
                                                                             (int) recordsUncompressedLength,
                                                                             compression,
                                                                             false);
         records = Records.load(MCAPDataInput.wrap(decompressedBuffer), 0, (int) recordsUncompressedLength);
      }

      recordsRef = new WeakReference<>(records);
      return records;
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
      dataOutput.putByteBuffer(dataInput.getByteBuffer(recordsOffset, (int) recordsCompressedLength, false));
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
