package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.util.List;

public class Metadata implements MCAPElement
{
   private final String name;
   private final List<StringPair> metadata;
   private final int metadataLength;

   Metadata(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      dataInput.position(elementPosition);
      name = dataInput.getString();
      long start = dataInput.position();
      metadata = MCAP.parseList(dataInput, StringPair::new); // TODO Looks into postponing the loading of the metadata.
      metadataLength = (int) (dataInput.position() - start);
      MCAP.checkLength(elementLength, getElementLength());
   }

   @Override
   public long getElementLength()
   {
      return Integer.BYTES + name.length() + metadataLength;
   }

   public String name()
   {
      return name;
   }

   public List<StringPair> metadata()
   {
      return metadata;
   }

   @Override
   public String toString()
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-name = " + name;
      out += "\n\t-metadata = " + EuclidCoreIOTools.getCollectionString(", ", metadata, e -> e.key());
      return out;
   }
}
