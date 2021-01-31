package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerProperty extends IntegerPropertyBase implements YoVariableProperty<YoInteger, Number>
{
   private final YoInteger yoInteger;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoIntegerValue();

   private SimpleIntegerProperty lastUserInput;

   public YoIntegerProperty(YoInteger yoInteger)
   {
      this(yoInteger, null);
   }

   public YoIntegerProperty(YoInteger yoInteger, Object bean)
   {
      this.yoInteger = yoInteger;
      this.bean = bean;
      pullYoIntegerValue();
      yoInteger.addListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoInteger.removeListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(int newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoInteger.set(newValue);
   }

   private void pullYoIntegerValue()
   {
      super.set(yoInteger.getValue());
   }

   @Override
   public IntegerProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleIntegerProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoInteger getYoVariable()
   {
      return yoInteger;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoInteger.getName();
   }
}
