package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.group;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.DoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;

public class YoGroupFXEditorTools
{

   public static <T, F> void setField(Collection<T> collection, BiConsumer<T, F> fieldSetter, F fieldValue)
   {
      for (T item : collection)
      {
         fieldSetter.accept(item, fieldValue);
      }
   }

   public static <T, F> Collection<F> getField(Collection<T> collection, Function<T, F> getter)
   {
      return collection.stream().map(getter).collect(Collectors.toList());
   }

   public static String getCommonString(YoCompositeSearchManager yoCompositeSearchManager, Collection<DoubleProperty> doubleProperties)
   {
      if (doubleProperties == null || doubleProperties.isEmpty())
         return null;

      String ret = null;

      for (DoubleProperty doubleProperty : doubleProperties)
      {
         String asString = toString(yoCompositeSearchManager, doubleProperty);

         if (ret == null)
            ret = asString;
         else if (!asString.equals(ret))
            return null;
      }

      return ret;
   }

   public static String toString(YoCompositeSearchManager yoCompositeSearchManager, DoubleProperty doubleProperty)
   {
      if (doubleProperty instanceof YoDoubleProperty)
      {
         YoDoubleProperty yoDoubleProperty = (YoDoubleProperty) doubleProperty;
         return yoCompositeSearchManager.getYoDoubleCollection().getYoVariableUniqueName(yoDoubleProperty.getYoVariable());
      }
      else
      {
         return Double.toString(doubleProperty.get());
      }
   }

   public static <T> T getCommonValue(Collection<T> values)
   {
      if (values == null || values.isEmpty())
         return null;

      T commonValue = null;

      for (T value : values)
      {
         if (commonValue == null)
            commonValue = value;
         else if (!commonValue.equals(value))
            return null;
      }

      return commonValue;
   }
}
