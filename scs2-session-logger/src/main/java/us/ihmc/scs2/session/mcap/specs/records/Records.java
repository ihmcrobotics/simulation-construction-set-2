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
