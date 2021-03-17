package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongProperty extends LongPropertyBase implements YoVariableProperty<YoLong, Number>
{
   private final YoLong yoLong;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoLongValue();

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
      yoLong.addListener(propertyUpdater);
   }

   @Override
   public void finalize()
   {
      try
      {
         yoLong.removeListener(propertyUpdater);
      }
      finally
      {
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

   public void bindLongProperty(Property<Long> property)
   {
      bindLongProperty(property, null);
   }

   public void bindLongProperty(Property<Long> property, Runnable pushValueAction)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(Long.valueOf(newValue.longValue()));
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         set(newValue.longValue());
         if (pushValueAction != null)
            pushValueAction.run();
         updatingThis.setFalse();
      });
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
