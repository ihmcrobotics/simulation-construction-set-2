package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

public class MessageIndexOffset implements MCAPElement
{
   /**
    * Channel ID.
    */
   private final int channelId;
   /**
    * Offset of the message index record for that channel after the chunk, from the start of the file.
    */
   private final long offset;

   public MessageIndexOffset(MCAPDataInput dataInput, long elementPosition)
   {
      dataInput.position(elementPosition);
      channelId = dataInput.getUnsignedShort();
      offset = MCAP.checkPositiveLong(dataInput.getLong(), "offset");
   }

   public MessageIndexOffset(int channelId, long offset)
   {
      this.channelId = channelId;
      this.offset = offset;
   }

   @Override
   public long getElementLength()
   {
      return Short.BYTES + Long.BYTES;
   }

   public int channelId()
   {
      return channelId;
   }

   public long offset()
   {
      return offset;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-channelId = " + channelId;
      out += "\n\t-offset = " + offset;
      return MCAPElement.indent(out, indent);
   }
}
