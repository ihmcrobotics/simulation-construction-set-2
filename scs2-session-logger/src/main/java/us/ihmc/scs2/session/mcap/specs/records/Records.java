package us.ihmc.scs2.session.mcap.specs.records;

import gnu.trove.map.hash.TIntObjectHashMap;
import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

public class Records extends ArrayList<Record>
{
   public Records()
   {
   }

   public static Records load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      Records records = new Records();
      MCAP.parseList(dataInput, RecordDataInputBacked::new, elementPosition, elementLength, records);
      return records;
   }

   public Records crop(long startTimestamp, long endTimestamp)
   {
      Records croppedRecords = new Records();

      for (Record record : this)
      {
         switch (record.op())
         {
            case HEADER:
            case FOOTER:
            case SCHEMA:
            case CHANNEL:
            case METADATA:
            case METADATA_INDEX:
            {
               croppedRecords.add(record);
               break;
            }
            case MESSAGE:
            {
               Message message = record.body();
               if (message.logTime() >= startTimestamp && message.logTime() <= endTimestamp)
                  croppedRecords.add(record);
               break;
            }
            case ATTACHMENT:
            {
               Attachment attachment = record.body();
               if (attachment.logTime() >= startTimestamp && attachment.logTime() <= endTimestamp)
                  croppedRecords.add(record);
               break;
            }
            default:
               throw new IllegalArgumentException("Unexpected value: " + record.op());
         }
      }
      return croppedRecords;
   }

   public long getElementLength()
   {
      // TODO Improve this by keeping track of modifications to the records.
      return stream().mapToLong(Record::getElementLength).sum();
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   public String toString(int indent)
   {
      if (isEmpty())
         return indent(getClass().getSimpleName() + ": []", indent);

      String out = getClass().getSimpleName() + "[\n";
      out += EuclidCoreIOTools.getCollectionString("\n", this, r -> r.toString(indent + 1));
      return indent(out, indent);
   }

   public List<MessageIndex> generateMessageIndexList()
   {
      TIntObjectHashMap<MutableMessageIndex> messageIndexMap = new TIntObjectHashMap<>();
      long messageIndexOffset = 0;

      for (Record record : this)
      {
         if (record.op() == Opcode.MESSAGE)
         {
            Message message = record.body();
            int channelId = message.channelId();

            MutableMessageIndex messageIndex = messageIndexMap.get(channelId);

            if (messageIndex == null)
            {
               messageIndex = new MutableMessageIndex();
               messageIndexMap.put(channelId, messageIndex);
            }

            MessageIndexEntry messageIndexEntry = new MessageIndexEntry(message.logTime(), messageIndexOffset);
            messageIndex.addMessageIndexEntry(messageIndexEntry);
         }
         messageIndexOffset += record.getElementLength();
      }

      return Arrays.asList(messageIndexMap.values(new MutableMessageIndex[messageIndexMap.size()]));
   }

   public static List<MessageIndexOffset> generateMessageIndexOffsets(long offset, List<? extends Record> messageIndexRecordList)
   {
      List<MessageIndexOffset> messageIndexOffsets = new ArrayList<>();

      long messageIndexOffset = offset;

      for (Record messageIndexRecord : messageIndexRecordList)
      {
         if (messageIndexRecord.op() != Opcode.MESSAGE_INDEX)
            throw new IllegalArgumentException("Expected a message index record, but got: " + messageIndexRecord.op());
         messageIndexOffset += messageIndexRecord.getElementLength();
         MessageIndex messageIndex = messageIndexRecord.body();
         messageIndexOffsets.add(new MessageIndexOffset(messageIndex.channelId(), messageIndexOffset));
      }

      return messageIndexOffsets;
   }
}
