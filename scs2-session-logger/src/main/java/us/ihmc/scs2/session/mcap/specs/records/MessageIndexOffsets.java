package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

import java.util.ArrayList;
import java.util.List;

public class MessageIndexOffsets implements MCAPElement
{
   private final List<MessageIndexOffset> entries = new ArrayList<>();
   private long elementLength;

   public MessageIndexOffsets(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      long currentPos = elementPosition;

      while (this.elementLength < elementLength)
      {
         currentPos = elementPosition + this.elementLength;
         add(new MessageIndexOffset(dataInput, currentPos));
      }

      if (this.elementLength != elementLength)
         throw new IllegalArgumentException("Invalid element length. Expected: " + elementLength + ", actual: " + this.elementLength);
   }

   public void add(MessageIndexOffset entry)
   {
      entries.add(entry);
      elementLength += entry.getElementLength();
   }

   @Override
   public long getElementLength()
   {
      return elementLength;
   }

   public List<MessageIndexOffset> entries()
   {
      return entries;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-entries = " + (entries == null ? "null" : "\n" + EuclidCoreIOTools.getCollectionString("\n", entries, e -> e.toString(indent + 1)));
      return MCAPElement.indent(out, indent);
   }
}
