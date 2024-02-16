package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;

/**
 * Attachment records contain auxiliary artifacts such as text, core dumps, calibration data, or other arbitrary data.
 * Attachment records must not appear within a chunk.
 */
public interface Attachment extends MCAPElement
{
   static Attachment load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new AttachmentDataInputBacked(dataInput, elementPosition, elementLength);
   }

   ByteBuffer crc32Input();

   /** Time at which the attachment was recorded. */
   long logTime();

   /** Time at which the attachment was created. If not available, must be set to zero. */
   long createTime();

   /** Name of the attachment, e.g "scene1.jpg". */
   String name();

   /** <a href="https://en.wikipedia.org/wiki/Media_type">Media type</a> of the attachment (e.g "text/plain"). */
   String mediaType();

   /** Size in bytes of the attachment data. */
   long dataLength();

   /** Attachment data. */
   ByteBuffer data();

   /**
    * CRC-32 checksum of the preceding fields in the record.
    * A value of zero indicates that CRC validation should not be performed.
    */
   long crc32();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(logTime());
      dataOutput.putLong(createTime());
      dataOutput.putString(name());
      dataOutput.putString(mediaType());
      dataOutput.putLong(dataLength());
      dataOutput.putByteBuffer(data());
      dataOutput.putUnsignedInt(crc32());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-logTime = " + logTime();
      out += "\n\t-createTime = " + createTime();
      out += "\n\t-name = " + name();
      out += "\n\t-mediaType = " + mediaType();
      out += "\n\t-dataLength = " + dataLength();
      out += "\n\t-data = " + data();
      out += "\n\t-crc32 = " + crc32();
      return MCAPElement.indent(out, indent);
   }
}
