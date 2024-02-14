package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

public interface ChunkIndex extends MCAPElement
{
   static ChunkIndex load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new ChunkIndexDataInputBacked(dataInput, elementPosition, elementLength);
   }

   Record chunk();

   long messageStartTime();

   long messageEndTime();

   long chunkOffset();

   long chunkLength();

   long messageIndexOffsetsLength();

   MessageIndexOffsets messageIndexOffsets();

   long messageIndexLength();

   Compression compression();

   long recordsCompressedLength();

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
