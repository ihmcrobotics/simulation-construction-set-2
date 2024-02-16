package us.ihmc.scs2.session.mcap.specs.records;

import java.util.Objects;

public class MutableChunkIndex implements ChunkIndex
{
   private Record chunk;
   private long messageStartTime;
   private long messageEndTime;
   private long chunkOffset;
   private long chunkLength;
   private long messageIndexOffsetsLength;
   private MessageIndexOffsets messageIndexOffsets;
   private long messageIndexLength;
   private Compression compression;
   private long recordsCompressedLength;
   private long recordsUncompressedLength;

   public void set(Record chunk)
   {
      this.chunk = chunk;
      Chunk body = chunk.body();
      messageStartTime = body.messageStartTime();
      messageEndTime = body.messageEndTime();
      chunkLength = chunk.getElementLength();
      messageIndexOffsets = createMessageIndexOffsets(body.records());
   }

   private MessageIndexOffsets createMessageIndexOffsets(Records records)
   {
      MessageIndexOffsets messageIndexOffsets = new MessageIndexOffsets();
      for (Record record : records)
      {
         if (record.body() instanceof MessageIndexOffset)
         {
            messageIndexOffsets.add((MessageIndexOffset) record.body());
         }
      }
      return messageIndexOffsets;
   }

   @Override
   public Record chunk()
   {
      return chunk;
   }

   public void setChunk(Record chunk)
   {
      this.chunk = chunk;
   }

   @Override
   public long messageStartTime()
   {
      return messageStartTime;
   }

   public void setMessageStartTime(long messageStartTime)
   {
      this.messageStartTime = messageStartTime;
   }

   @Override
   public long messageEndTime()
   {
      return messageEndTime;
   }

   public void setMessageEndTime(long messageEndTime)
   {
      this.messageEndTime = messageEndTime;
   }

   @Override
   public long chunkOffset()
   {
      return chunkOffset;
   }

   public void setChunkOffset(long chunkOffset)
   {
      this.chunkOffset = chunkOffset;
   }

   @Override
   public long chunkLength()
   {
      return chunkLength;
   }

   public void setChunkLength(long chunkLength)
   {
      this.chunkLength = chunkLength;
   }

   @Override
   public long messageIndexOffsetsLength()
   {
      return messageIndexOffsetsLength;
   }

   public void setMessageIndexOffsetsLength(long messageIndexOffsetsLength)
   {
      this.messageIndexOffsetsLength = messageIndexOffsetsLength;
   }

   @Override
   public MessageIndexOffsets messageIndexOffsets()
   {
      return messageIndexOffsets;
   }

   public void setMessageIndexOffsets(MessageIndexOffsets messageIndexOffsets)
   {
      this.messageIndexOffsets = messageIndexOffsets;
   }

   @Override
   public long messageIndexLength()
   {
      return messageIndexLength;
   }

   public void setMessageIndexLength(long messageIndexLength)
   {
      this.messageIndexLength = messageIndexLength;
   }

   @Override
   public Compression compression()
   {
      return compression;
   }

   public void setCompression(Compression compression)
   {
      this.compression = compression;
   }

   @Override
   public long recordsCompressedLength()
   {
      return recordsCompressedLength;
   }

   public void setRecordsCompressedLength(long recordsCompressedLength)
   {
      this.recordsCompressedLength = recordsCompressedLength;
   }

   @Override
   public long recordsUncompressedLength()
   {
      return recordsUncompressedLength;
   }

   public void setRecordsUncompressedLength(long recordsUncompressedLength)
   {
      this.recordsUncompressedLength = recordsUncompressedLength;
   }

   @Override
   public long getElementLength()
   {
      Objects.requireNonNull(messageIndexOffsets, "The message index offsets must be set before calling this method.");
      Objects.requireNonNull(compression, "The compression must be set before calling this method.");
      return 7 * Long.BYTES + 2 * Integer.BYTES + messageIndexOffsets.getElementLength() + compression.getLength();
   }
}
