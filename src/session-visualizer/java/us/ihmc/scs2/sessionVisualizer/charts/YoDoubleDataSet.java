package us.ihmc.scs2.sessionVisualizer.charts;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.spi.DoubleDataSet;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoDoubleDataSet extends DoubleDataSet
{
   private static final long serialVersionUID = -8080028106442423091L;

   private final YoVariable<?> yoVariable;

   private double xMin, xMax, yMin, yMax;

   public YoDoubleDataSet(YoVariable<?> yoVariable, int initialSize)
   {
      super(yoVariable.getName(), initialSize);

      this.yoVariable = yoVariable;
   }

   public void setRange(double xMin, double xMax, double yMin, double yMax)
   {
      this.xMin = xMin;
      this.xMax = xMax;
      this.yMin = yMin;
      this.yMax = yMax;
   }

   public YoVariable<?> getYoVariable()
   {
      return yoVariable;
   }

   @Override
   public DoubleDataSet recomputeLimits(int dimension)
   {
      if (dimension == 0)
      {
         AxisDescription xAxisDescription = getAxisDescription(0);
         xAxisDescription.clear();
         xAxisDescription.setMin(xMin);
         xAxisDescription.setMax(xMax);
      }
      else if (dimension == 1)
      {
         AxisDescription yAxisDescription = getAxisDescription(1);
         yAxisDescription.clear();
         yAxisDescription.setMin(yMin);
         yAxisDescription.setMax(yMax);
      }
      return this;
   }
}
