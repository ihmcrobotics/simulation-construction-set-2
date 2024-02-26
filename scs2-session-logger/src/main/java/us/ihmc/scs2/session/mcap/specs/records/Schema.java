package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * A Schema record defines an individual schema.
 * Schema records are uniquely identified within a file by their schema ID.
 * A Schema record must occur at least once in the file prior to any Channel referring to its ID.
 * Any two schema records sharing a common ID must be identical.
 *
 * @see <a href="https://mcap.dev/spec#schema-op0x03">MCAP Schema</a>
 */
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
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedShort(id());
      dataOutput.putString(name());
      dataOutput.putString(encoding());
      dataOutput.putUnsignedInt(dataLength());
      dataOutput.putByteBuffer(data());
   }

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

   @Override
   default boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof Schema other)
      {
         if (id() != other.id())
            return false;
         if (!Objects.equals(name(), other.name()))
            return false;
         if (!Objects.equals(encoding(), other.encoding()))
            return false;
         if (dataLength() != other.dataLength())
            return false;
         return Arrays.equals(data().array(), other.data().array());
      }

      return false;
   }
}
