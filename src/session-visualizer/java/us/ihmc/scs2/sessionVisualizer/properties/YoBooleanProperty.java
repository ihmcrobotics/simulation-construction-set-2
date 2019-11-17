package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import us.ihmc.yoVariables.listener.VariableChangedListener;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanProperty extends BooleanPropertyBase implements YoVariableProperty<YoBoolean, Boolean>
{
   private final YoBoolean yoBoolean;
   private final Object bean;
   private final VariableChangedListener propertyUpdater = v -> pullYoBooleanValue();

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
      yoBoolean.addVariableChangedListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoBoolean.removeVariableChangedListener(propertyUpdater);
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
