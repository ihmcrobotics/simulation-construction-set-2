package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;
import java.util.List;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

public class MessageIndex implements MCAPElement
{
   private final MCAPDataInput dataInput;
   private final long elementLength;
   private final int channelId;
   private WeakReference<List<MessageIndexEntry>> messageIndexEntriesRef;
   private final long messageIndexEntriesOffset;
   private final long messageIndexEntriesLength;

   public MessageIndex(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;
      this.elementLength = elementLength;

      dataInput.position(elementPosition);
      channelId = dataInput.getUnsignedShort();
      messageIndexEntriesLength = dataInput.getUnsignedInt();
      messageIndexEntriesOffset = dataInput.position();
   }

   @Override
   public long getElementLength()
   {
      return elementLength;
   }

   public static class MessageIndexEntry implements MCAPElement
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
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-logTime = " + logTime;
         out += "\n\t-offset = " + offset;
         return indent(out, indent);
      }
   }

   public int channelId()
   {
      return channelId;
   }

   public List<MessageIndexEntry> messageIndexEntries()
   {
      List<MessageIndexEntry> messageIndexEntries = messageIndexEntriesRef == null ? null : messageIndexEntriesRef.get();

      if (messageIndexEntries == null)
      {
         messageIndexEntries = MCAP.parseList(dataInput, MessageIndexEntry::new, messageIndexEntriesOffset, messageIndexEntriesLength);
         messageIndexEntriesRef = new WeakReference<>(messageIndexEntries);
      }

      return messageIndexEntries;
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
      List<MessageIndexEntry> messageIndexEntries = messageIndexEntries();
      out += "\n\t-messageIndexEntries = " + (messageIndexEntries == null ?
            "null" :
            "\n" + EuclidCoreIOTools.getCollectionString("\n", messageIndexEntries, e -> e.toString(indent + 1)));
      return indent(out, indent);
   }
}
