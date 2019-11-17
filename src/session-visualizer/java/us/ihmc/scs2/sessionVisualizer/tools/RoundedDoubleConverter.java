package us.ihmc.scs2.sessionVisualizer.tools;

public class RoundedDoubleConverter extends javafx.util.converter.DoubleStringConverter
{
   @Override
   public String toString(Double value)
   {
      return Long.toString(Math.round(value));
   }
}