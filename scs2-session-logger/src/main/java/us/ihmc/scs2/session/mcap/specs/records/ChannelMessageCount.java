package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public class ChannelMessageCount implements MCAPElement
{
   private final int channelId;
   private final long messageCount;

   public ChannelMessageCount(MCAPDataInput dataInput, long elementPosition)
   {
      dataInput.position(elementPosition);
      channelId = dataInput.getUnsignedShort();
      messageCount = dataInput.getLong();
   }

   public ChannelMessageCount(int channelId, long messageCount)
   {
      this.channelId = channelId;
      this.messageCount = messageCount;
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

   public long messageCount()
   {
      return messageCount;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedShort(channelId);
      dataOutput.putLong(messageCount);
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
      out += "\n\t-messageCount = " + messageCount;
      return MCAPElement.indent(out, indent);
   }
}
