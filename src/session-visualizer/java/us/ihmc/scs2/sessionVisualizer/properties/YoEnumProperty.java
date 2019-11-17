package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.yoVariables.listener.VariableChangedListener;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumProperty<E extends Enum<E>> extends ObjectPropertyBase<E> implements YoVariableProperty<YoEnum<E>, E>
{
   private final YoEnum<E> yoEnum;
   private final Object bean;
   private final VariableChangedListener propertyUpdater = v -> pullYoEnumValue();

   private SimpleObjectProperty<E> lastUserInput;

   public YoEnumProperty(YoEnum<E> yoEnum)
   {
      this(yoEnum, null);
   }

   public YoEnumProperty(YoEnum<E> yoEnum, Object bean)
   {
      this.yoEnum = yoEnum;
      this.bean = bean;
      pullYoEnumValue();
      yoEnum.addVariableChangedListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoEnum.removeVariableChangedListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(E newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoEnum.set(newValue);
   }

   private void pullYoEnumValue()
   {
      super.set(yoEnum.getValue());
   }

   @Override
   public ObjectProperty<E> userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleObjectProperty<>(this, getName() + "LastUserInput", get());
      return lastUserInput;
   }

   @Override
   public YoEnum<E> getYoVariable()
   {
      return yoEnum;
   }

   @Override
   public Object getBean()
   {
      return bean;
   }

   @Override
   public String getName()
   {
      return yoEnum.getName();
   }
}
