package us.ihmc.scs2.sessionVisualizer.properties;

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import us.ihmc.yoVariables.listener.VariableChangedListener;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumAsStringProperty<E extends Enum<E>> extends StringPropertyBase implements YoVariableProperty<YoEnum<E>, String>
{
   private final YoEnum<E> yoEnum;
   private final Object bean;
   private final VariableChangedListener propertyUpdater = v -> pullYoEnumValue();
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
