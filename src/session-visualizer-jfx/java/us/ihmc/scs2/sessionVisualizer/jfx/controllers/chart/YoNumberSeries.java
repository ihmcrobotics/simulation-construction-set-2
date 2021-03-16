package us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart;

import us.ihmc.scs2.sessionVisualizer.jfx.charts.NumberSeries;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ChartTools;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoNumberSeries extends NumberSeries
{
   private final YoVariable yoVariable;

   public YoNumberSeries(YoVariable yoVariable)
   {
      super(yoVariable.getName());

      this.yoVariable = yoVariable;
   }

   public void updateLegend()
   {
      setCurrentValue(ChartTools.defaultYoVariableValueFormatter(yoVariable));
   }

   public YoVariable getYoVariable()
   {
      return yoVariable;
   }
}