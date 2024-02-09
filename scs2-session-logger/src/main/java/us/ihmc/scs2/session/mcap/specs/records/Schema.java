package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

import java.nio.ByteBuffer;
import java.util.Arrays;

public interface Schema extends MCAPElement
{
   static Schema load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new SchemaDataInputBacked(dataInput, elementPosition, elementLength);
   }

   @Override
   default long getElementLength()
   {
      return Short.BYTES + 3 * Integer.BYTES + name().length() + encoding().length() + (int) dataLength();
   }

   int id();

   String name();

   String encoding();

   long dataLength();

   ByteBuffer data();

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-id = " + id();
      out += "\n\t-name = " + name();
      out += "\n\t-encoding = " + encoding();
      out += "\n\t-dataLength = " + dataLength();
      out += "\n\t-data = " + Arrays.toString(data().array());
      return MCAPElement.indent(out, indent);
   }
}
