package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public interface Record extends MCAPElement
{
   int RECORD_HEADER_LENGTH = 9;

   static Record load(MCAPDataInput dataInput)
   {
      return load(dataInput, dataInput.position());
   }

   static Record load(MCAPDataInput dataInput, long elementPosition)
   {
      return new RecordDataInputBacked(dataInput, elementPosition);
   }

   Opcode op();

   <T> T body();

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-op = " + op();
      Object body = body();
      out += "\n\t-body = " + (body == null ? "null" : "\n" + ((MCAPElement) body).toString(indent + 2));
      return MCAPElement.indent(out, indent);
   }

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      write(dataOutput, true);
   }

   void write(MCAPDataOutput dataOutput, boolean writeBody);
}
