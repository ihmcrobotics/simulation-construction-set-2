package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.NumberFormatTools;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class LineChartTools
{
   public static String defaultYoVariableValueFormatter(YoVariable yoVariable)
   {
      if (yoVariable instanceof YoDouble)
         return NumberFormatTools.doubleToString(yoVariable.getValueAsDouble(), 3);
      else
         return LineChartTools.getYoVariableValueAsString(yoVariable, EuclidCoreIOTools.getStringFormat(11, 8));
   }

   public static String getYoVariableValueAsString(YoVariable yoVariable, String format)
   {
      if (yoVariable instanceof YoDouble)
         return String.format(format, ((YoDouble) yoVariable).getValue());
      if (yoVariable instanceof YoBoolean)
         return Boolean.toString(((YoBoolean) yoVariable).getValue());
      if (yoVariable instanceof YoInteger)
         return Integer.toString(((YoInteger) yoVariable).getValue());
      if (yoVariable instanceof YoLong)
         return Long.toString(((YoLong) yoVariable).getValue());
      if (yoVariable instanceof YoEnum<?>)
         return ((YoEnum<?>) yoVariable).getStringValue();

      throw new UnsupportedOperationException("Unsupported YoVariable type: " + yoVariable.getClass().getSimpleName());
   }
}
