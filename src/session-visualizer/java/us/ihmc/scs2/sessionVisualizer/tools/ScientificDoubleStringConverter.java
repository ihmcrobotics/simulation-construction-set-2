package us.ihmc.scs2.sessionVisualizer.tools;

import javafx.util.StringConverter;

public class ScientificDoubleStringConverter extends StringConverter<Double>
{
   private int precision;

   public ScientificDoubleStringConverter(int precision)
   {
      this.precision = precision;
   }

   @Override
   public Double fromString(String string)
   {
      return NumberFormatTools.parseDouble(string);
   }

   @Override
   public String toString(Double value)
   {
      if (value == null)
         return "";

      return NumberFormatTools.doubleToString(value, precision);
   }
}
