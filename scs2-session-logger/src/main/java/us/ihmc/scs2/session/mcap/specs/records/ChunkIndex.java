package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

/**
 * ChunkIndex records contain the location of a Chunk record and its associated MessageIndex records.
 * A ChunkIndex record exists for every Chunk in the file.
 */
public interface ChunkIndex extends MCAPElement
{
   static ChunkIndex load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new ChunkIndexDataInputBacked(dataInput, elementPosition, elementLength);
   }

   Record chunk();

   /**
    * Earliest message log_time in the chunk. Zero if the chunk has no messages.
    */
   long messageStartTime();

   /**
    * Latest message log_time in the chunk. Zero if the chunk has no messages.
    */
   long messageEndTime();

   /**
    * Offset to the chunk record from the start of the file.
    */
   long chunkOffset();

   /**
    * Byte length of the chunk record, including opcode and length prefix.
    */
   long chunkLength();

   /**
    * Total length in bytes of the message index records after the chunk.
    */
   long messageIndexOffsetsLength();

   /**
    * Mapping from channel ID to the offset of the message index record for that channel after the
    * chunk, from the start of the file. An empty map indicates no message indexing is available.
    */
   MessageIndexOffsets messageIndexOffsets();

   /**
    * Total length in bytes of the message index records after the chunk.
    */
   long messageIndexLength();

   /**
    * The compression used within the chunk.
    * Refer to well-known compression formats.
    * This field should match the value in the corresponding Chunk record.
    */
   Compression compression();

   /**
    * The size of the chunk records field.
    */
   long recordsCompressedLength();

   /**
    * The uncompressed size of the chunk records field. This field should match the value in the
    * corresponding Chunk record.
    */
   long recordsUncompressedLength();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(messageStartTime());
      dataOutput.putLong(messageEndTime());
      dataOutput.putLong(chunkOffset());
      dataOutput.putLong(chunkLength());
      dataOutput.putUnsignedInt(messageIndexOffsetsLength());
      if (messageIndexOffsets() != null)
         messageIndexOffsets().write(dataOutput);
      dataOutput.putLong(messageIndexLength());
      dataOutput.putString(compression().getName());
      dataOutput.putLong(recordsCompressedLength());
      dataOutput.putLong(recordsUncompressedLength());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-messageStartTime = " + messageStartTime();
      out += "\n\t-messageEndTime = " + messageEndTime();
      out += "\n\t-chunkOffset = " + chunkOffset();
      out += "\n\t-chunkLength = " + chunkLength();
      out += "\n\t-messageIndexOffsetsLength = " + messageIndexOffsetsLength();
      out += "\n\t-messageIndexOffsets = " + (messageIndexOffsets() == null ? "null" : "\n" + messageIndexOffsets().toString(indent + 1));
      out += "\n\t-messageIndexLength = " + messageIndexLength();
      out += "\n\t-compression = " + compression();
      out += "\n\t-compressedSize = " + recordsCompressedLength();
      out += "\n\t-uncompressedSize = " + recordsUncompressedLength();
      return indent(out, indent);
   }
}
