package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

public interface Footer extends MCAPElement
{
   static Footer load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new FooterDataInputBacked(dataInput, elementPosition, elementLength);
   }

   Records summarySection();

   Records summaryOffsetSection();

   Integer ofsSummaryCrc32Input();

   byte[] summaryCrc32Input();

   long ofsSummarySection();

   long ofsSummaryOffsetSection();

   long summaryCrc32();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(ofsSummarySection());
      dataOutput.putLong(ofsSummaryOffsetSection());
      dataOutput.putUnsignedInt(summaryCrc32());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-ofsSummarySection = " + ofsSummarySection();
      out += "\n\t-ofsSummaryOffsetSection = " + ofsSummaryOffsetSection();
      out += "\n\t-summaryCrc32 = " + summaryCrc32();
      return indent(out, indent);
   }
}
