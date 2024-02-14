package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.List;

public interface MessageIndex extends MCAPElement
{
   int channelId();

   List<MessageIndexEntry> messageIndexEntries();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedShort(channelId());
      dataOutput.putCollection(messageIndexEntries());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-channelId = " + channelId();
      List<MessageIndexEntry> messageIndexEntries = messageIndexEntries();
      out += "\n\t-messageIndexEntries = " + (messageIndexEntries == null ?
            "null" :
            "\n" + EuclidCoreIOTools.getCollectionString("\n", messageIndexEntries, e -> e.toString(indent + 1)));
      return MCAPElement.indent(out, indent);
   }
}
