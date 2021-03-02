package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanProperty extends BooleanPropertyBase implements YoVariableProperty<YoBoolean, Boolean>
{
   private final YoBoolean yoBoolean;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoBooleanValue();

   private SimpleBooleanProperty lastUserInput;

   public YoBooleanProperty(YoBoolean yoBoolean)
   {
      this(yoBoolean, null);
   }

   public YoBooleanProperty(YoBoolean yoBoolean, Object bean)
   {
      this.yoBoolean = yoBoolean;
      this.bean = bean;
      pullYoBooleanValue();
      yoBoolean.addListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoBoolean.removeListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(boolean newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoBoolean.set(newValue);
   }

   private void pullYoBooleanValue()
   {
      super.set(yoBoolean.getValue());
   }

   public void bindBooleanProperty(Property<Boolean> property)
   {
      bindBooleanProperty(property, null);
   }

   public void bindBooleanProperty(Property<Boolean> property, Runnable pushValueAction)
   {
      property.setValue(getValue());

      MutableBoolean updatingControl = new MutableBoolean(false);
      MutableBoolean updatingThis = new MutableBoolean(false);

      addListener((o, oldValue, newValue) ->
      { // YoVariable changed, updating control
         if (updatingThis.isTrue())
            return;

         updatingControl.setTrue();
         property.setValue(newValue);
         updatingControl.setFalse();
      });

      property.addListener((o, oldValue, newValue) ->
      {
         if (updatingControl.isTrue())
            return;

         updatingThis.setTrue();
         set(newValue);
         if (pushValueAction != null)
            pushValueAction.run();
         updatingThis.setFalse();
      });
   }

   @Override
   public BooleanProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleBooleanProperty(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoBoolean getYoVariable()
   {
      return yoBoolean;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoBoolean.getName();
   }
}
