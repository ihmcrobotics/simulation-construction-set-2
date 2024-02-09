package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

public class FooterDataInputBacked implements Footer
{
   private final MCAPDataInput dataInput;
   private final long ofsSummarySection;
   private final long ofsSummaryOffsetSection;
   private final long summaryCrc32;
   private Integer ofsSummaryCrc32Input;
   private Records summaryOffsetSection;
   private Records summarySection;

   public FooterDataInputBacked(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      ofsSummarySection = MCAP.checkPositiveLong(dataInput.getLong(), "ofsSummarySection");
      ofsSummaryOffsetSection = MCAP.checkPositiveLong(dataInput.getLong(), "ofsSummaryOffsetSection");
      summaryCrc32 = dataInput.getUnsignedInt();
      MCAP.checkLength(elementLength, getElementLength());
   }

   public static long computeOffsetFooter(MCAPDataInput dataInput)
   {
      return (((((dataInput.size() - 1L) - 8L) - 20L) - 8L));
   }

   @Override
   public long getElementLength()
   {
      return 2 * Long.BYTES + Integer.BYTES;
   }

   @Override
   public Records summarySection()
   {
      if (summarySection == null && ofsSummarySection != 0)
      {
         long length = ((ofsSummaryOffsetSection != 0 ? ofsSummaryOffsetSection : computeOffsetFooter(dataInput)) - ofsSummarySection);
         summarySection = Records.load(dataInput, ofsSummarySection, (int) length);
      }
      return summarySection;
   }

   @Override
   public Records summaryOffsetSection()
   {
      if (summaryOffsetSection == null && ofsSummaryOffsetSection != 0)
      {
         summaryOffsetSection = Records.load(dataInput, ofsSummaryOffsetSection, (int) (computeOffsetFooter(dataInput) - ofsSummaryOffsetSection));
      }
      return summaryOffsetSection;
   }

   @Override
   public Integer ofsSummaryCrc32Input()
   {
      if (ofsSummaryCrc32Input == null)
      {
         ofsSummaryCrc32Input = (int) ((ofsSummarySection() != 0 ? ofsSummarySection() : computeOffsetFooter(dataInput)));
      }
      return ofsSummaryCrc32Input;
   }

   private byte[] summaryCrc32Input;

   @Override
   public byte[] summaryCrc32Input()
   {
      if (summaryCrc32Input == null)
      {
         long length = dataInput.size() - ofsSummaryCrc32Input() - 8 - 4;
         summaryCrc32Input = dataInput.getBytes(ofsSummaryCrc32Input(), (int) length);
      }
      return summaryCrc32Input;
   }

   @Override
   public long ofsSummarySection()
   {
      return ofsSummarySection;
   }

   @Override
   public long ofsSummaryOffsetSection()
   {
      return ofsSummaryOffsetSection;
   }

   /**
    * A CRC-32 of all bytes from the start of the Summary section up through and including the end of
    * the previous field (summary_offset_start) in the footer record. A value of 0 indicates the CRC-32
    * is not available.
    */
   @Override
   public long summaryCrc32()
   {
      return summaryCrc32;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
