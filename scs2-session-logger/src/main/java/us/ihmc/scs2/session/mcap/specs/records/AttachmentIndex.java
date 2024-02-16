package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

/**
 * An Attachment Index record contains the location of an attachment in the file.
 * An Attachment Index record exists for every Attachment record in the file.
 */
public interface AttachmentIndex extends MCAPElement
{
   static AttachmentIndex load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new AttachmentIndexDataInputBacked(dataInput, elementPosition, elementLength);
   }

   @Override
   default long getElementLength()
   {
      return 5 * Long.BYTES + 2 * Integer.BYTES + name().length() + mediaType().length();
   }

   Record attachment();

   /** Byte offset from the start of the file to the attachment record. */
   long attachmentOffset();

   /** Byte length of the attachment record, including opcode and length prefix. */
   long attachmentLength();

   /** Time at which the attachment was recorded. */
   long logTime();

   /** Time at which the attachment was created. If not available, must be set to zero. */
   long createTime();

   /** Size of the attachment data. */
   long dataLength();

   /** Name of the attachment. */
   String name();

   /** <a href="https://en.wikipedia.org/wiki/Media_type">Media type</a> of the attachment (e.g "text/plain"). */
   String mediaType();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(attachmentOffset());
      dataOutput.putLong(attachmentLength());
      dataOutput.putLong(logTime());
      dataOutput.putLong(createTime());
      dataOutput.putLong(dataLength());
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
      out += "\n\t-dataLength = " + dataLength();
      out += "\n\t-name = " + name();
      out += "\n\t-mediaType = " + mediaType();
      return MCAPElement.indent(out, indent);
   }
}
