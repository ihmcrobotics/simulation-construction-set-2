package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoDoubleProperty extends DoublePropertyBase implements YoVariableProperty<YoDouble, Number>
{
   private final YoDouble yoDouble;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoDoubleValue();

   private SimpleDoubleProperty lastUserInput;

   public YoDoubleProperty(YoDouble yoDouble)
   {
      this(yoDouble, null);
   }

   public YoDoubleProperty(YoDouble yoDouble, Object bean)
   {
      this.yoDouble = yoDouble;
      this.bean = bean;
      pullYoDoubleValue();
      yoDouble.addListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoDouble.removeListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(double newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoDouble.set(newValue);
   }

   private void pullYoDoubleValue()
   {
      super.set(yoDouble.getValue());
   }

   @Override
   public DoubleProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleDoubleProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoDouble getYoVariable()
   {
      return yoDouble;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoDouble.getName();
   }
}
