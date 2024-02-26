package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.List;

/**
 * MessageIndex records allow readers to locate individual records within a chunk by timestamp.
 * A sequence of Message Index records occurs immediately after each chunk.
 * Exactly one Message Index record must exist in the sequence for every channel on which a message occurs inside the chunk.
 *
 * @see <a href="https://mcap.dev/spec#message-index-op0x07">MCAP Message Index</a>
 */
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
