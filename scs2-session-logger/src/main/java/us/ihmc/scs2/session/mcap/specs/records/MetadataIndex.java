package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

/**
 * A metadata index record contains the location of a metadata record within the file.
 */
public interface MetadataIndex extends MCAPElement
{
   @Override
   long getElementLength();

   Record metadata();

   long metadataOffset();

   long metadataLength();

   String name();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putLong(metadataOffset());
      dataOutput.putLong(metadataLength());
      dataOutput.putString(name());
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-metadataOffset = " + metadataOffset();
      out += "\n\t-metadataLength = " + metadataLength();
      out += "\n\t-name = " + name();
      return MCAPElement.indent(out, indent);
   }
}
