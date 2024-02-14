package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

public interface Chunk extends MCAPElement
{
   static Chunk load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new ChunkDataInputBacked(dataInput, elementPosition, elementLength);
   }

   /**
    * Earliest message log_time in the chunk. Zero if the chunk has no messages.
    */
   long messageStartTime();

   /**
    * Latest message log_time in the chunk. Zero if the chunk has no messages.
    */
   long messageEndTime();

   /**
    * Uncompressed size of the records field.
    */
   long recordsUncompressedLength();

   /**
    * CRC-32 checksum of uncompressed `records` field. A value of zero indicates that CRC validation
    * should not be performed.
    */
   long uncompressedCRC32();

   /**
    * compression algorithm. i.e. zstd, lz4, "". An empty string indicates no compression. Refer to
    * well-known compression formats.
    */
   Compression compression();

   /**
    * Length of the records in bytes.
    */
   long recordsCompressedLength();

   /**
    * The decompressed records.
    */
   Records records();

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-messageStartTime = " + messageStartTime();
      out += "\n\t-messageEndTime = " + messageEndTime();
      out += "\n\t-compression = " + compression();
      out += "\n\t-recordsCompressedLength = " + recordsCompressedLength();
      out += "\n\t-recordsUncompressedLength = " + recordsUncompressedLength();
      out += "\n\t-uncompressedCrc32 = " + uncompressedCRC32();
      return MCAPElement.indent(out, indent);
   }
}
