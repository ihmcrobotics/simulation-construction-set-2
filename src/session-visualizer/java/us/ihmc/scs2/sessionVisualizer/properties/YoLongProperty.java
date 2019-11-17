package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import us.ihmc.yoVariables.listener.VariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongProperty extends LongPropertyBase implements YoVariableProperty<YoLong, Number>
{
   private final YoLong yoLong;
   private final Object bean;
   private final VariableChangedListener propertyUpdater = v -> pullYoLongValue();

   private SimpleLongProperty lastUserInput;

   public YoLongProperty(YoLong yoLong)
   {
      this(yoLong, null);
   }

   public YoLongProperty(YoLong yoLong, Object bean)
   {
      this.yoLong = yoLong;
      this.bean = bean;
      pullYoLongValue();
      yoLong.addVariableChangedListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoLong.removeVariableChangedListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(long newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoLong.set(newValue);
   }

   private void pullYoLongValue()
   {
      super.set(yoLong.getValue());
   }

   @Override
   public LongProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleLongProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoLong getYoVariable()
   {
      return yoLong;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoLong.getName();
   }
}
