package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public interface AttachmentIndex extends MCAPElement
{
   static AttachmentIndex load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new AttachmentIndexDataInputBacked(dataInput, elementPosition, elementLength);
   }

   Record attachment();

   long attachmentOffset();

   long attachmentLength();

   long logTime();

   long createTime();

   long dataSize();

   String name();

   String mediaType();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(attachmentOffset());
      dataOutput.putLong(attachmentLength());
      dataOutput.putLong(logTime());
      dataOutput.putLong(createTime());
      dataOutput.putLong(dataSize());
      dataOutput.putString(name());
      dataOutput.putString(mediaType());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-attachmentOffset = " + attachmentOffset();
      out += "\n\t-attachmentLength = " + attachmentLength();
      out += "\n\t-logTime = " + logTime();
      out += "\n\t-createTime = " + createTime();
      out += "\n\t-dataSize = " + dataSize();
      out += "\n\t-name = " + name();
      out += "\n\t-mediaType = " + mediaType();
      return MCAPElement.indent(out, indent);
   }
}
