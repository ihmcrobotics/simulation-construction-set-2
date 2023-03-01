package us.ihmc.scs2.definition.yoGraphic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.visual.PaintDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple2DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;

public abstract class YoGraphicDefinition
{
   private static final boolean DEBUG_PARSING;

   static
   {
      boolean debugParsingValue = false;
      String debugParsingProp = System.getProperty("scs2.definition.debugParsing");
      if (debugParsingProp != null)
         debugParsingValue = Boolean.parseBoolean(debugParsingProp);
      DEBUG_PARSING = debugParsingValue;
   }

   protected String name;
   protected boolean visible = true;

   public YoGraphicDefinition()
   {
      registerField("name", this::getName, this::setName);
      registerField("visible", this::isVisible, this::setVisible);
   }

   @XmlAttribute
   public final void setName(String name)
   {
      this.name = name;
   }

   @XmlAttribute
   public final void setVisible(boolean visible)
   {
      this.visible = visible;
   }

   public final String getName()
   {
      return name;
   }

   public final boolean isVisible()
   {
      return visible;
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof YoGraphicDefinition other)
      {
         if (!Objects.equals(name, other.name))
            return false;
         if (visible != other.visible)
            return false;
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * Creates a map from field name to field value as {@code String} for every field representing this
    * {@code YoGraphicDefinition}.
    * 
    * @return the map of field names to respective values.
    */
   public Map<String, String> createFieldValueStringMap()
   {
      LinkedHashMap<String, String> map = new LinkedHashMap<>();

      for (FieldStringParsingInfo definitionField : definitionFields.values())
      {
         String value = definitionField.fieldStringValueSupplier.get();
         if (value != null)
            map.put(definitionField.fieldName, value);
      }
      return map;
   }

   public void parseFieldValueStringMap(Map<String, String> map)
   {
      for (Entry<String, String> entry : map.entrySet())
      {
         FieldStringParsingInfo field = definitionFields.get(entry.getKey());
         if (field == null)
         {
            if (DEBUG_PARSING)
               LogTools.error("Could not find field: {} for type: {}", entry.getKey(), getClass().getSimpleName());
            continue;
         }

         try
         {
            field.fieldStringValueParser.accept(entry.getValue());
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error for definition: %s, field: %s, value: %s".formatted(getClass().getSimpleName(),
                                                                                                  field.fieldName,
                                                                                                  entry.getValue()),
                                       e);
         }
      }
   }

   private static class FieldStringParsingInfo
   {
      private final String fieldName;
      private final Supplier<String> fieldStringValueSupplier;
      private final Consumer<String> fieldStringValueParser;

      public FieldStringParsingInfo(String fieldName, Supplier<String> fieldStringValueSupplier, Consumer<String> fieldStringValueParser)
      {
         this.fieldName = fieldName;
         this.fieldStringValueSupplier = fieldStringValueSupplier;
         this.fieldStringValueParser = fieldStringValueParser;
      }
   }

   @XmlTransient
   private final Map<String, FieldStringParsingInfo> definitionFields = new LinkedHashMap<>();

   protected final <T> void registerListField(String fieldName, Supplier<List<T>> fieldListValueGetter, Consumer<List<T>> fieldListValueSetter)
   {
      // FIXME
   }

   static <T> String listToString(List<T> list, Function<T, String> elementToString)
   {
      StringBuilder sb = new StringBuilder("List(");
      for (int i = 0; i < list.size(); i++)
      {
         if (i > 0)
            sb.append(", ");
         sb.append("e").append(i).append("=").append(elementToString.apply(list.get(i)));
      }
      sb.append(")");
      return sb.toString();
   }

   static <T> List<T> parseList(String value, Function<String, T> elementParser)
   {
      value = value.trim();

      if (value.startsWith("List"))
      {
         String elementsSustring = value.substring(5, value.length() - 1).trim();
         ArrayList<T> list = new ArrayList<>();
         if (elementsSustring.isEmpty())
            return list;

         elementsSustring = elementsSustring.substring(3).trim();

         int nextElementIndex = 1;
         while (true)
         {
            String nextElementLabel = ", e%d=".formatted(nextElementIndex);
            int indexOfLabel = elementsSustring.indexOf(nextElementLabel);

            String element;
            if (indexOfLabel != -1)
            {
               element = elementsSustring.substring(0, indexOfLabel);
               list.add(elementParser.apply(element));
               elementsSustring = elementsSustring.substring(indexOfLabel + nextElementLabel.length());
               nextElementIndex++;
            }
            else
            {
               list.add(elementParser.apply(elementsSustring));
               break;
            }
         }
         return list;
      }
      else
      {
         throw new IllegalArgumentException("Unknown list format: " + value);
      }
   }

   protected final void registerYoListField(String fieldName, Supplier<YoListDefinition> fieldListValueGetter, Consumer<YoListDefinition> fieldListValueSetter)
   {
      registerField(fieldName,
                    () -> Objects.toString(fieldListValueGetter.get()),
                    value -> fieldListValueSetter.accept(value == null ? null : YoListDefinition.parse(value)));
   }

   protected final void registerTuple2DField(String fieldName,
                                             Supplier<YoTuple2DDefinition> fieldTuple2DValueGetter,
                                             Consumer<YoTuple2DDefinition> fieldTuple2DValueSetter)
   {
      registerField(fieldName,
                    () -> Objects.toString(fieldTuple2DValueGetter.get()),
                    value -> fieldTuple2DValueSetter.accept(value == null ? null : YoTuple2DDefinition.parse(value)));
   }

   protected final void registerTuple3DField(String fieldName,
                                             Supplier<YoTuple3DDefinition> fieldTuple3DValueGetter,
                                             Consumer<YoTuple3DDefinition> fieldTuple3DValueSetter)
   {
      registerField(fieldName,
                    () -> Objects.toString(fieldTuple3DValueGetter.get()),
                    value -> fieldTuple3DValueSetter.accept(value == null ? null : YoTuple3DDefinition.parse(value)));
   }

   protected final void registerOrientation3DField(String fieldName,
                                                   Supplier<YoOrientation3DDefinition> fieldOrientation3DValueGetter,
                                                   Consumer<YoOrientation3DDefinition> fieldOrientation3DValueSetter)
   {
      registerField(fieldName,
                    () -> Objects.toString(fieldOrientation3DValueGetter.get()),
                    value -> fieldOrientation3DValueSetter.accept(value == null ? null : YoOrientation3DDefinition.parse(value)));
   }

   protected final void registerPaintField(String fieldName, Supplier<PaintDefinition> fieldPaintValueGetter, Consumer<PaintDefinition> fieldPaintValueSetter)
   {
      registerField(fieldName,
                    () -> Objects.toString(fieldPaintValueGetter.get()),
                    value -> fieldPaintValueSetter.accept(value == null ? null : PaintDefinition.parse(value)));
   }

   protected final void registerField(String fieldName, DoubleSupplier fieldDoubleValueGetter, DoubleConsumer fieldDoubleValueSetter)
   {
      registerField(fieldName,
                    () -> Double.toString(fieldDoubleValueGetter.getAsDouble()),
                    string -> fieldDoubleValueSetter.accept(Double.parseDouble(string)));
   }

   protected final void registerField(String fieldName, BooleanSupplier fieldBooleanValueGetter, Consumer<Boolean> fieldBooleanValueSetter)
   {
      registerField(fieldName,
                    () -> Boolean.toString(fieldBooleanValueGetter.getAsBoolean()),
                    string -> fieldBooleanValueSetter.accept(Boolean.valueOf(string)));
   }

   protected final void registerField(String fieldName, Supplier<String> fieldStringValueSupplier, Consumer<String> fieldStringValueParser)
   {
      definitionFields.put(fieldName, new FieldStringParsingInfo(fieldName, fieldStringValueSupplier, fieldStringValueParser));
   }
}
