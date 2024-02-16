package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

/**
 * A Summary Offset record contains the location of records within the summary section.
 * Each Summary Offset record corresponds to a group of summary records with the same opcode.
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
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-groupOpcode = " + groupOpcode();
      out += "\n\t-groupOffset = " + groupOffset();
      out += "\n\t-groupLength = " + groupLength();
      return MCAPElement.indent(out, indent);
   }
}
