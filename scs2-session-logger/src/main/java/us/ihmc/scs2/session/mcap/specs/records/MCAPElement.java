package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public interface MCAPElement
{
   static String indent(String stringToIndent, int indent)
   {
      if (indent <= 0)
         return stringToIndent;
      String indentStr = "\t".repeat(indent);
      return indentStr + stringToIndent.replace("\n", "\n" + indentStr);
   }

   void write(MCAPDataOutput dataOutput);

   long getElementLength();

   default String toString(int indent)
   {
      return indent(toString(), indent);
   }
}
