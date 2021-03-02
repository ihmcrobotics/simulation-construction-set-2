package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.Property;
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

   public void bindIntegerProperty(Property<Integer> property)
   {
      bindIntegerProperty(property, null);
   }

   public void bindIntegerProperty(Property<Integer> property, Runnable pushValueAction)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(Integer.valueOf(newValue.intValue()));
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         set(newValue.intValue());
         if (pushValueAction != null)
            pushValueAction.run();
         updatingThis.setFalse();
      });
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
