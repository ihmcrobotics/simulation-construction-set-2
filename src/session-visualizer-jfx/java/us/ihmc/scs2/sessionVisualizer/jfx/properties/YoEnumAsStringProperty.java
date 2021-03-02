package us.ihmc.scs2.sessionVisualizer.jfx.properties;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumAsStringProperty<E extends Enum<E>> extends StringPropertyBase implements YoVariableProperty<YoEnum<E>, String>
{
   private final YoEnum<E> yoEnum;
   private final Object bean;
   private final YoVariableChangedListener propertyUpdater = v -> pullYoEnumValue();
   private final List<String> enumConstants;

   private SimpleStringProperty lastUserInput;

   public YoEnumAsStringProperty(YoEnum<E> yoEnum)
   {
      this(yoEnum, null);
   }

   public YoEnumAsStringProperty(YoEnum<E> yoEnum, Object bean)
   {
      this.yoEnum = yoEnum;
      this.bean = bean;

      enumConstants = Arrays.asList(yoEnum.getEnumValuesAsString());
      pullYoEnumValue();
      yoEnum.addListener(propertyUpdater);
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         yoEnum.removeListener(propertyUpdater);
      }
      finally
      {
         super.finalize();
      }
   }

   @Override
   public void set(String newValue)
   {
      if (lastUserInput != null)
         lastUserInput.set(newValue);
      super.set(newValue);
      yoEnum.set(toEnumOrdinal(newValue));
   }

   private void pullYoEnumValue()
   {
      super.set(toEnumString(yoEnum.getOrdinal()));
   }

   public void bindStringProperty(Property<String> property)
   {
      bindStringProperty(property, null);
   }

   public void bindStringProperty(Property<String> property, Runnable pushValueAction)
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

   public int toEnumOrdinal(String newValue)
   {
      if (newValue == null)
         return YoEnum.NULL_VALUE;

      int indexOf = enumConstants.indexOf(newValue);
      if (indexOf == -1)
         throw new IllegalArgumentException("The value " + newValue + " is not part of the constants of the YoEnum: " + yoEnum.getName());
      return indexOf;
   }

   public String toEnumString(int ordinal)
   {
      if (ordinal == -1)
         return null;
      else
         return enumConstants.get(yoEnum.getOrdinal());
   }

   @Override
   public StringProperty userInputProperty()
   {
      if (lastUserInput == null)
         lastUserInput = new SimpleStringProperty(this, getName() + "LastUserInput", get());
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
