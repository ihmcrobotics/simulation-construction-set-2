package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;

class ChunkIndexDataInputBacked implements ChunkIndex
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
    * Offset to the chunk record from the start of the file.
    */
   private final long chunkOffset;
   /**
    * Byte length of the chunk record, including opcode and length prefix.
    */
   private final long chunkLength;
   private final long messageIndexOffsetsOffset;
   /**
    * Total length in bytes of the message index records after the chunk.
    */
   private final long messageIndexOffsetsLength;
   /**
    * Mapping from channel ID to the offset of the message index record for that channel after the
    * chunk, from the start of the file. An empty map indicates no message indexing is available.
    */
   private WeakReference<MessageIndexOffsets> messageIndexOffsetsRef;
   /**
    * Total length in bytes of the message index records after the chunk.
    */
   private final long messageIndexLength;
   /**
    * The compression used within the chunk. Refer to well-known compression formats. This field should
    * match the the value in the corresponding Chunk record.
    */
   private final String compression;
   /**
    * The size of the chunk records field.
    */
   private final long compressedSize;
   /**
    * The uncompressed size of the chunk records field. This field should match the value in the
    * corresponding Chunk record.
    */
   private final long uncompressedSize;

   ChunkIndexDataInputBacked(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;
      this.elementLength = elementLength;

      dataInput.position(elementPosition);
      messageStartTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageStartTime");
      messageEndTime = MCAP.checkPositiveLong(dataInput.getLong(), "messageEndTime");
      chunkOffset = MCAP.checkPositiveLong(dataInput.getLong(), "chunkOffset");
      chunkLength = MCAP.checkPositiveLong(dataInput.getLong(), "chunkLength");
      messageIndexOffsetsLength = dataInput.getUnsignedInt();
      messageIndexOffsetsOffset = dataInput.position();
      dataInput.skip(messageIndexOffsetsLength);
      messageIndexLength = MCAP.checkPositiveLong(dataInput.getLong(), "messageIndexLength");
      compression = dataInput.getString();
      compressedSize = MCAP.checkPositiveLong(dataInput.getLong(), "compressedSize");
      uncompressedSize = MCAP.checkPositiveLong(dataInput.getLong(), "uncompressedSize");
   }

   @Override
   public long getElementLength()
   {
      return elementLength;
   }

   private WeakReference<Record> chunkRef;

   @Override
   public Record chunk()
   {
      Record chunk = chunkRef == null ? null : chunkRef.get();

      if (chunk == null)
      {
         chunk = new RecordDataInputBacked(dataInput, chunkOffset);
         chunkRef = new WeakReference<>(chunk);
      }
      return chunkRef.get();
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
   public long chunkOffset()
   {
      return chunkOffset;
   }

   @Override
   public long chunkLength()
   {
      return chunkLength;
   }

   @Override
   public long messageIndexOffsetsLength()
   {
      return messageIndexOffsetsLength;
   }

   @Override
   public MessageIndexOffsets messageIndexOffsets()
   {
      MessageIndexOffsets messageIndexOffsets = messageIndexOffsetsRef == null ? null : messageIndexOffsetsRef.get();

      if (messageIndexOffsets == null)
      {
         messageIndexOffsets = new MessageIndexOffsets(dataInput, messageIndexOffsetsOffset, messageIndexOffsetsLength);
         messageIndexOffsetsRef = new WeakReference<>(messageIndexOffsets);
      }

      return messageIndexOffsets;
   }

   @Override
   public long messageIndexLength()
   {
      return messageIndexLength;
   }

   @Override
   public String compression()
   {
      return compression;
   }

   @Override
   public long compressedSize()
   {
      return compressedSize;
   }

   @Override
   public long uncompressedSize()
   {
      return uncompressedSize;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
