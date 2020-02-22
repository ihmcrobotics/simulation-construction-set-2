package us.ihmc.scs2.sessionVisualizer.charts;

import de.gsi.dataset.spi.DoubleDataSet;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoDoubleDataSet extends DoubleDataSet
{
   private static final long serialVersionUID = -8080028106442423091L;

   private final YoVariable<?> yoVariable;

   public YoDoubleDataSet(YoVariable<?> yoVariable, int initialSize)
   {
      super(yoVariable.getName(), initialSize);

      this.yoVariable = yoVariable;
   }

   public YoVariable<?> getYoVariable()
   {
      return yoVariable;
   }
}
