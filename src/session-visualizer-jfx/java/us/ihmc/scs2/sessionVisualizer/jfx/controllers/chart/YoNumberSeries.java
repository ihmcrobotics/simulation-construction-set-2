package us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart;

import java.util.concurrent.atomic.AtomicBoolean;

import us.ihmc.scs2.sessionVisualizer.jfx.charts.NumberSeries;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ChartTools;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoNumberSeries extends NumberSeries
{
   private final YoVariable yoVariable;
   private final AtomicBoolean dirty = new AtomicBoolean(true);
   private final YoVariableChangedListener dirtyListener = v -> dirty.set(true);

   public YoNumberSeries(YoVariable yoVariable)
   {
      super(yoVariable.getName());

      this.yoVariable = yoVariable;
      yoVariable.addListener(dirtyListener);
   }

   public void updateLegend()
   {
      if (dirty.getAndSet(false))
         setCurrentValue(ChartTools.defaultYoVariableValueFormatter(yoVariable));
   }

   public YoVariable getYoVariable()
   {
      return yoVariable;
   }

   public void close()
   {
      yoVariable.removeListener(dirtyListener);
   }
}