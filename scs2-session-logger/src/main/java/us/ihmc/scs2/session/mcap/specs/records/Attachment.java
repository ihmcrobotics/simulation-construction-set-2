package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;

public interface Attachment extends MCAPElement
{
   static Attachment load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new AttachmentDataInputBacked(dataInput, elementPosition, elementLength);
   }

   ByteBuffer crc32Input();

   long logTime();

   long createTime();

   String name();

   String mediaType();

   long dataOffset();

   long dataLength();

   ByteBuffer data();

   /**
    * CRC-32 checksum of the preceding fields in the record. A value of zero indicates that CRC validation
    * should not be performed.
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
      out += "\n\t-dataOffset = " + dataOffset();
      out += "\n\t-dataLength = " + dataLength();
      out += "\n\t-data = " + data();
      out += "\n\t-crc32 = " + crc32();
      return MCAPElement.indent(out, indent);
   }
}
