package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.encoding.MCAPCRC32Helper;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import static us.ihmc.scs2.session.mcap.specs.records.MCAPElement.indent;

/**
 * Footer records contain end-of-file information. MCAP files must end with a Footer record.
 *
 * @see <a href="https://mcap.dev/spec#footer-op0x02">MCAP Footer</a>
 */
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
   default MCAPCRC32Helper updateCRC(MCAPCRC32Helper crc32)
   {
      if (crc32 == null)
         crc32 = new MCAPCRC32Helper();
      crc32.addLong(ofsSummarySection());
      crc32.addLong(ofsSummaryOffsetSection());
      crc32.addUnsignedInt(summaryCrc32());
      return crc32;
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

   @Override
   default boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof Footer other)
      {
         if (ofsSummarySection() != other.ofsSummarySection())
            return false;
         if (ofsSummaryOffsetSection() != other.ofsSummaryOffsetSection())
            return false;
         return summaryCrc32() == other.summaryCrc32();
      }

      return false;
   }
}
