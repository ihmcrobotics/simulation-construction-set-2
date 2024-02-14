package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.List;

public interface Statistics extends MCAPElement
{
   static Statistics load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new StatisticsDataInputBacked(dataInput, elementPosition, elementLength);
   }

   long messageCount();

   int schemaCount();

   long channelCount();

   long attachmentCount();

   long metadataCount();

   long chunkCount();

   long messageStartTime();

   long messageEndTime();

   List<ChannelMessageCount> channelMessageCounts();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(messageCount());
      dataOutput.putUnsignedShort(schemaCount());
      dataOutput.putUnsignedInt(channelCount());
      dataOutput.putUnsignedInt(attachmentCount());
      dataOutput.putUnsignedInt(metadataCount());
      dataOutput.putUnsignedInt(chunkCount());
      dataOutput.putLong(messageStartTime());
      dataOutput.putLong(messageEndTime());
      dataOutput.putCollection(channelMessageCounts());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-messageCount = " + messageCount();
      out += "\n\t-schemaCount = " + schemaCount();
      out += "\n\t-channelCount = " + channelCount();
      out += "\n\t-attachmentCount = " + attachmentCount();
      out += "\n\t-metadataCount = " + metadataCount();
      out += "\n\t-chunkCount = " + chunkCount();
      out += "\n\t-messageStartTime = " + messageStartTime();
      out += "\n\t-messageEndTime = " + messageEndTime();
      out += "\n\t-channelMessageCounts = \n" + EuclidCoreIOTools.getCollectionString("\n", channelMessageCounts(), e -> e.toString(1));
      return out;
   }
}
