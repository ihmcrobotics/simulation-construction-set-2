package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

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

   String compression();

   long compressedSize();

   long uncompressedSize();

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
      out += "\n\t-compressedSize = " + compressedSize();
      out += "\n\t-uncompressedSize = " + uncompressedSize();
      return indent(out, indent);
   }
}
