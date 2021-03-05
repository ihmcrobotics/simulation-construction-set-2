package us.ihmc.scs2.sessionVisualizer.sliderboard;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import us.ihmc.commons.MathTools;

public class SliderboardControlVariable
{
   private DoubleProperty min = new SimpleDoubleProperty(this, "min", 0.0);
   private DoubleProperty max = new SimpleDoubleProperty(this, "max", 0.0);
   private DoubleProperty value = new SimpleDoubleProperty(this, "value", 0.0);

   public SliderboardControlVariable()
   {
   }

   public void setMin(double min)
   {
      this.min.set(min);
   }

   public void setMax(double max)
   {
      this.max.set(max);
   }

   public void setRange(double min, double max)
   {
      setMin(min);
      setMax(max);
   }

   public void setValue(double value)
   {
      this.value.set(value);
   }

   public double getMin()
   {
      return min.get();
   }

   public double getMax()
   {
      return max.get();
   }

   public double getValue()
   {
      return value.get();
   }

   public DoubleProperty minProperty()
   {
      return min;
   }
   
   public DoubleProperty maxProperty()
   {
      return max;
   }
   
   public DoubleProperty valueProperty()
   {
      return value;
   }

   public static int doubleToInt(double valueDouble, double minDouble, double maxDouble, int minInt, int maxInt)
   {
      if (Double.isNaN(valueDouble))
         return -1;
      double percent = (valueDouble - minDouble) / (maxDouble - minDouble);
      percent = MathTools.clamp(percent, 0.0, 1.0);
      return (int) (Math.round(percent * (maxInt - minInt)) + minInt);
   }

   public static double intToDouble(int valueInt, int minInt, int maxInt, double minDouble, double maxDouble)
   {
      double percent = (double) (valueInt - minInt) / (double) (maxInt - minInt);
      percent = MathTools.clamp(percent, 0.0, 1.0);
      return percent * (maxDouble - minDouble) + minDouble;
   }
}
