package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.util.Objects;

/**
 * Header is the first record in an MCAP file.
 *
 * @see <a href="https://mcap.dev/spec#header">MCAP Header</a>
 */
public class Header implements MCAPElement
{
   private final String profile;
   private final String library;

   public Header(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      dataInput.position(elementPosition);
      profile = dataInput.getString();
      library = dataInput.getString();
      MCAP.checkLength(elementLength, getElementLength());
   }

   public Header(String profile, String library)
   {
      this.profile = profile;
      this.library = library;
   }

   @Override
   public long getElementLength()
   {
      return 2 * Integer.BYTES + profile.length() + library.length();
   }

   public String profile()
   {
      return profile;
   }

   public String library()
   {
      return library;
   }

   @Override
   public void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putString(profile);
      dataOutput.putString(library);
   }

   @Override
   public String toString()
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-profile = " + profile;
      out += "\n\t-library = " + library;
      return out;
   }

   @Override
   public boolean equals(Object object)
   {
      return object instanceof Header other && equals(other);
   }

   @Override
   public boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof Header other)
      {
         if (!Objects.equals(profile(), other.profile()))
            return false;
         return Objects.equals(library(), other.library());
      }

      return false;
   }
}
