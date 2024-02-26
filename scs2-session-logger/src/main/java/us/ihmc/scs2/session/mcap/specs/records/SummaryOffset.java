package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.encoding.MCAPCRC32Helper;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

/**
 * A Summary Offset record contains the location of records within the summary section.
 * Each Summary Offset record corresponds to a group of summary records with the same opcode.
 *
 * @see <a href="https://mcap.dev/spec#summary-offset-op0x0e">MCAP Summary Offset</a>
 */
public interface SummaryOffset extends MCAPElement
{
   static SummaryOffset load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new SummaryOffsetDataInputBacked(dataInput, elementPosition, elementLength);
   }

   Records group();

   Opcode groupOpcode();

   long groupOffset();

   long groupLength();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedByte(groupOpcode().id());
      dataOutput.putLong(groupOffset());
      dataOutput.putLong(groupLength());
   }

   @Override
   default MCAPCRC32Helper updateCRC(MCAPCRC32Helper crc32)
   {
      if (crc32 == null)
         crc32 = new MCAPCRC32Helper();
      crc32.addUnsignedByte(groupOpcode().id());
      crc32.addLong(groupOffset());
      crc32.addLong(groupLength());
      return crc32;
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-groupOpcode = " + groupOpcode();
      out += "\n\t-groupOffset = " + groupOffset();
      out += "\n\t-groupLength = " + groupLength();
      return MCAPElement.indent(out, indent);
   }

   @Override
   default boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof SummaryOffset other)
      {
         if (groupOpcode() != other.groupOpcode())
            return false;
         if (groupOffset() != other.groupOffset())
            return false;
         return groupLength() == other.groupLength();
      }

      return false;
   }
}
