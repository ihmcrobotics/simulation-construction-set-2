package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

public class DataEnd implements MCAPElement
{
   private final long dataSectionCRC32;

   public DataEnd(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      dataInput.position(elementPosition);
      dataSectionCRC32 = dataInput.getUnsignedInt();
      MCAP.checkLength(elementLength, getElementLength());
   }

   public DataEnd(long dataSectionCrc32)
   {
      this.dataSectionCRC32 = dataSectionCrc32;
   }

   @Override
   public long getElementLength()
   {
      return Integer.BYTES;
   }

   /**
    * CRC-32 of all bytes in the data section. A value of 0 indicates the CRC-32 is not available.
    */
   public long dataSectionCRC32()
   {
      return dataSectionCRC32;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedInt(dataSectionCRC32);
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + ":\n\t-dataSectionCrc32 = " + dataSectionCRC32;
   }
}
