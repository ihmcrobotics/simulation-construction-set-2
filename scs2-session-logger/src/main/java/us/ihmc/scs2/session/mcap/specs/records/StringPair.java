package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

public class StringPair implements MCAPElement
{
   private final String key;
   private final String value;

   public StringPair(MCAPDataInput dataInput, long elementPosition)
   {
      dataInput.position(elementPosition);
      key = dataInput.getString();
      value = dataInput.getString();
   }

   public StringPair(String key, String value)
   {
      this.key = key;
      this.value = value;
   }

   @Override
   public long getElementLength()
   {
      return key.length() + value.length() + 2 * Integer.BYTES;
   }

   public String key()
   {
      return key;
   }

   public String value()
   {
      return value;
   }

   @Override
   public String toString()
   {
      return (key + ": " + value).replace("\n", "");
   }
}
