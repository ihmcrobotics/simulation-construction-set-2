package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.util.ArrayList;

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
}
