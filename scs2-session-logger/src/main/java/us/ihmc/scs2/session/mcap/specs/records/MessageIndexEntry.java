package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

public class MessageIndexEntry implements MCAPElement
{
   /**
    * Time at which the message was recorded.
    */
   private final long logTime;

   /**
    * Offset is relative to the start of the uncompressed chunk data.
    */
   private final long offset;

   public MessageIndexEntry(MCAPDataInput dataInput, long elementPosition)
   {
      dataInput.position(elementPosition);
      logTime = MCAP.checkPositiveLong(dataInput.getLong(), "logTime");
      offset = MCAP.checkPositiveLong(dataInput.getLong(), "offset");
   }

   @Override
   public long getElementLength()
   {
      return 2 * Long.BYTES;
   }

   public long logTime()
   {
      return logTime;
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
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(logTime);
      dataOutput.putLong(offset);
   }

   @Override
   public String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-logTime = " + logTime;
      out += "\n\t-offset = " + offset;
      return MCAPElement.indent(out, indent);
   }
}
