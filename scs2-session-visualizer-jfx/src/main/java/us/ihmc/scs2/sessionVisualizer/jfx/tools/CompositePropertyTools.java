package us.ihmc.scs2.sessionVisualizer.jfx.tools;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoComposite.YoCompositeDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoQuaternionDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple2DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameWrapper;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.CompositeProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Orientation3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.QuaternionProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple3DProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YawPitchRollProperty;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositePropertyTools
{
   public static List<CompositeProperty> toCompositePropertyList(YoVariableDatabase yoVariableDatabase,
                                                                 ReferenceFrameManager referenceFrameManager,
                                                                 List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static List<Tuple2DProperty> toTuple2DPropertyList(YoVariableDatabase yoVariableDatabase,
                                                             ReferenceFrameManager referenceFrameManager,
                                                             List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static List<Tuple3DProperty> toTuple3DPropertyList(YoVariableDatabase yoVariableDatabase,
                                                             ReferenceFrameManager referenceFrameManager,
                                                             List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static CompositeProperty toCompositeProperty(YoVariableDatabase yoVariableDatabase,
                                                       ReferenceFrameManager referenceFrameManager,
                                                       YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;

      return new CompositeProperty(definition.getType(),
                                   definition.getComponentIdentifiers(),
                                   toReferenceFrameProperty(yoVariableDatabase, referenceFrameManager, definition.getReferenceFrame()),
                                   toDoublePropertyArray(yoVariableDatabase, definition.getComponentValues()));
   }

   public static Tuple2DProperty toTuple2DProperty(YoVariableDatabase yoVariableDatabase,
                                                   ReferenceFrameManager referenceFrameManager,
                                                   YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new Tuple2DProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static Tuple3DProperty toTuple3DProperty(YoVariableDatabase yoVariableDatabase,
                                                   ReferenceFrameManager referenceFrameManager,
                                                   YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new Tuple3DProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static Orientation3DProperty toOrientation3DProperty(YoVariableDatabase yoVariableDatabase,
                                                               ReferenceFrameManager referenceFrameManager,
                                                               YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      else if (definition.getType().equals(YoQuaternionDefinition.YoQuaternion))
         return toQuaternionProperty(yoVariableDatabase, referenceFrameManager, definition);
      else if (definition.getType().equals(YoYawPitchRollDefinition.YoYawPitchRoll))
         return toYawPitchRollProperty(yoVariableDatabase, referenceFrameManager, definition);
      else
         throw new UnsupportedOperationException("Unsupported orientation definition: " + definition.getType());
   }

   public static QuaternionProperty toQuaternionProperty(YoVariableDatabase yoVariableDatabase,
                                                         ReferenceFrameManager referenceFrameManager,
                                                         YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new QuaternionProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static YawPitchRollProperty toYawPitchRollProperty(YoVariableDatabase yoVariableDatabase,
                                                             ReferenceFrameManager referenceFrameManager,
                                                             YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new YawPitchRollProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static DoubleProperty[] toDoublePropertyArray(YoVariableDatabase yoVariableDatabase, String[] definitionArray)
   {
      if (definitionArray == null)
         return null;
      return Stream.of(definitionArray).map(name -> toDoubleProperty(yoVariableDatabase, name)).toArray(DoubleProperty[]::new);
   }

   public static List<DoubleProperty> toDoublePropertyList(YoVariableDatabase yoVariableDatabase, List<String> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toDoubleProperty(yoVariableDatabase, definition)).collect(Collectors.toList());
   }

   public static DoubleProperty toDoubleProperty(YoVariableDatabase yoVariableDatabase, String field)
   {
      return toDoubleProperty(yoVariableDatabase, yoVariableDatabase::linkYoVariable, field);
   }

   public static DoubleProperty toDoubleProperty(YoVariableDatabase yoVariableDatabase,
                                                 BiFunction<YoDouble, Object, LinkedYoDouble> linkedYoVariableFactory,
                                                 String field)
   {
      if (field == null)
      {
         return null;
      }
      else if (isParsableAsDouble(field))
      {
         return new SimpleDoubleProperty(Double.parseDouble(field));
      }
      else
      {
         YoDouble yoDouble = (YoDouble) yoVariableDatabase.searchExact(field);
         if (yoDouble == null)
         {
            LogTools.warn("Incompatible variable name, searching similar variables");
            yoDouble = yoVariableDatabase.searchSimilar(field, 0.90, YoDouble.class);
         }
         if (yoDouble == null)
         {
            LogTools.error("Could not find the YoVariable: {}", field);
            return new SimpleDoubleProperty(Double.NaN);
         }
         YoDoubleProperty yoDoubleProperty = new YoDoubleProperty(yoDouble);
         if (linkedYoVariableFactory != null)
            yoDoubleProperty.setLinkedBuffer(linkedYoVariableFactory.apply(yoDouble, yoDoubleProperty));
         return yoDoubleProperty;
      }
   }

   public static IntegerProperty toIntegerProperty(YoVariableDatabase yoVariableDatabase, String field)
   {
      if (field == null)
      {
         return null;
      }
      else if (isParsableAsInteger(field))
      {
         return new SimpleIntegerProperty(Integer.parseInt(field));
      }
      else
      {
         YoInteger yoInteger = (YoInteger) yoVariableDatabase.searchExact(field);
         if (yoInteger == null)
         {
            LogTools.warn("Incompatible variable name, searching similar variables");
            yoInteger = yoVariableDatabase.searchSimilar(field, 0.90, YoInteger.class);
         }
         if (yoInteger == null)
         {
            LogTools.error("Could not find the YoVariable: {}", field);
            return new SimpleIntegerProperty(-1);
         }
         YoIntegerProperty yoIntegerProperty = new YoIntegerProperty(yoInteger);
         yoIntegerProperty.setLinkedBuffer(yoVariableDatabase.linkYoVariable(yoInteger, yoIntegerProperty));
         return yoIntegerProperty;
      }
   }

   public static Property<ReferenceFrameWrapper> toReferenceFrameProperty(YoVariableDatabase yoVariableDatabase,
                                                                          ReferenceFrameManager referenceFrameManager,
                                                                          String field)
   {
      if (field == null)
         return null;

      ReferenceFrameWrapper referenceFrame = referenceFrameManager.getReferenceFrameFromFullname(field);

      if (referenceFrame != null)
         return new SimpleObjectProperty<>(referenceFrame);

      referenceFrame = referenceFrameManager.getReferenceFrameFromUniqueName(field);

      if (referenceFrame != null)
         return new SimpleObjectProperty<>(referenceFrame);

      if (!field.contains(ReferenceFrame.SEPARATOR))
         return new SimpleObjectProperty<>(referenceFrameManager.getWorldFrame()); // We're looking for a root frame, we can use world.

      String fieldShort = field.substring(field.lastIndexOf(ReferenceFrame.SEPARATOR) + 1);

      referenceFrame = referenceFrameManager.getReferenceFrameFromUniqueName(fieldShort);

      if (referenceFrame != null)
         return new SimpleObjectProperty<>(referenceFrame);

      LogTools.warn("Could not retrieve the frame {}. Could be a robot frame that is yet to be loaded (fullname: {}).", fieldShort, field);
      referenceFrame = referenceFrameManager.getReferenceFrameFromFullname(field, true);
      return new SimpleObjectProperty<>(referenceFrame);
   }

   public static String toDoublePropertyName(DoubleProperty doubleProperty)
   {
      if (doubleProperty == null)
         return null;
      else if (doubleProperty instanceof YoDoubleProperty)
         return ((YoDoubleProperty) doubleProperty).getYoVariable().getFullNameString();
      else
         return Double.toString(doubleProperty.get());
   }

   public static String toIntegerPropertyName(IntegerProperty integerProperty)
   {
      if (integerProperty == null)
         return null;
      else if (integerProperty instanceof YoIntegerProperty)
         return ((YoIntegerProperty) integerProperty).getYoVariable().getFullNameString();
      else
         return Integer.toString(integerProperty.get());
   }

   public static String toReferenceFramePropertyName(Property<ReferenceFrameWrapper> referenceFrameProperty)
   {
      if (referenceFrameProperty == null || referenceFrameProperty.getValue() == null)
         return null;
      else if (referenceFrameProperty instanceof SimpleObjectProperty)
         return referenceFrameProperty.getValue().getFullName();
      else
         throw new UnsupportedOperationException("Unhandled property: " + referenceFrameProperty.getClass().getSimpleName());
   }

   public static YoCompositeDefinition toYoCompositeDefinition(CompositeProperty property)
   {
      if (property.getType().equals(YoTuple2DDefinition.YoTuple2D))
         return toYoTuple2DDefinition(property);
      else if (property.getType().equals(YoTuple3DDefinition.YoTuple3D))
         return toYoTuple3DDefinition(property);
      else if (property.getType().equals(YoQuaternionDefinition.YoQuaternion))
         return toYoQuaternionDefinition(property);
      else if (property.getType().equals(YoYawPitchRollDefinition.YoYawPitchRoll))
         return toYoYawPitchRollDefinition(property);
      else
         throw new UnsupportedOperationException("Unhandled property type: " + property.getType());
   }

   public static YoTuple2DDefinition toYoTuple2DDefinition(CompositeProperty property)
   {
      if (property == null)
         return null;
      if (!property.getType().equals(YoTuple2DDefinition.YoTuple2D))
         throw new IllegalArgumentException("Cannot convert a " + property.getType() + " to a " + YoTuple2DDefinition.class.getSimpleName());

      YoTuple2DDefinition definition = new YoTuple2DDefinition();
      definition.setX(toDoublePropertyName(property.componentValueProperties()[0]));
      definition.setY(toDoublePropertyName(property.componentValueProperties()[1]));
      definition.setReferenceFrame(toReferenceFramePropertyName(property.referenceFrameProperty()));
      return definition;
   }

   public static YoTuple3DDefinition toYoTuple3DDefinition(CompositeProperty property)
   {
      if (property == null)
         return null;
      if (!property.getType().equals(YoTuple3DDefinition.YoTuple3D))
         throw new IllegalArgumentException("Cannot convert a " + property.getType() + " to a " + YoTuple3DDefinition.class.getSimpleName());
      YoTuple3DDefinition definition = new YoTuple3DDefinition();
      definition.setX(toDoublePropertyName(property.componentValueProperties()[0]));
      definition.setY(toDoublePropertyName(property.componentValueProperties()[1]));
      definition.setZ(toDoublePropertyName(property.componentValueProperties()[2]));
      definition.setReferenceFrame(toReferenceFramePropertyName(property.referenceFrameProperty()));
      return definition;
   }

   public static YoTuple3DDefinition toYoTuple3DDefinition(DoubleProperty[] properties)
   {
      if (properties == null || properties.length != 3)
         return null;
      YoTuple3DDefinition definition = new YoTuple3DDefinition();
      definition.setX(toDoublePropertyName(properties[0]));
      definition.setY(toDoublePropertyName(properties[1]));
      definition.setZ(toDoublePropertyName(properties[2]));
      return definition;
   }

   public static YoOrientation3DDefinition toYoOrientation3DDefinition(CompositeProperty property)
   {
      if (property == null)
         return null;
      else if (property.getType().equals(YoQuaternionDefinition.YoQuaternion))
         return toYoQuaternionDefinition(property);
      else if (property.getType().equals(YoYawPitchRollDefinition.YoYawPitchRoll))
         return toYoYawPitchRollDefinition(property);
      else
         throw new UnsupportedOperationException("Unsupported orientation property: " + property.getType());
   }

   public static YoQuaternionDefinition toYoQuaternionDefinition(CompositeProperty property)
   {
      YoQuaternionDefinition definition = new YoQuaternionDefinition();
      definition.setX(toDoublePropertyName(property.componentValueProperties()[0]));
      definition.setY(toDoublePropertyName(property.componentValueProperties()[1]));
      definition.setZ(toDoublePropertyName(property.componentValueProperties()[2]));
      definition.setS(toDoublePropertyName(property.componentValueProperties()[3]));
      definition.setReferenceFrame(toReferenceFramePropertyName(property.referenceFrameProperty()));
      return definition;
   }

   public static YoQuaternionDefinition toYoQuaternionDefinition(DoubleProperty[] properties)
   {
      if (properties == null || properties.length != 4)
         return null;
      YoQuaternionDefinition definition = new YoQuaternionDefinition();
      definition.setX(toDoublePropertyName(properties[0]));
      definition.setY(toDoublePropertyName(properties[1]));
      definition.setZ(toDoublePropertyName(properties[2]));
      definition.setS(toDoublePropertyName(properties[3]));
      return definition;
   }

   {

   }

   public static YoYawPitchRollDefinition toYoYawPitchRollDefinition(CompositeProperty property)
   {
      YoYawPitchRollDefinition definition = new YoYawPitchRollDefinition();
      definition.setYaw(toDoublePropertyName(property.componentValueProperties()[0]));
      definition.setPitch(toDoublePropertyName(property.componentValueProperties()[1]));
      definition.setRoll(toDoublePropertyName(property.componentValueProperties()[2]));
      definition.setReferenceFrame(toReferenceFramePropertyName(property.referenceFrameProperty()));
      return definition;
   }

   public static List<String> toDoublePropertyNames(List<? extends DoubleProperty> doubleProperties)
   {
      if (doubleProperties == null)
         return null;
      return doubleProperties.stream().map(CompositePropertyTools::toDoublePropertyName).collect(Collectors.toList());
   }

   public static boolean isParsableAsNumber(String string)
   { // TODO: Probably need to add more to this like long, etc.
      return isParsableAsDouble(string) || isParsableAsInteger(string);
   }

   public static boolean areParsableAsDoubles(String... strings)
   {
      if (strings == null || strings.length == 0)
         return false;
      for (String string : strings)
      {
         if (!isParsableAsDouble(string))
            return false;
      }
      return true;
   }

   public static boolean isParsableAsDouble(String string)
   {
      if (string == null)
         return false;

      try
      {
         Double.parseDouble(string);
         return true;
      }
      catch (NumberFormatException e)
      {
         return false;
      }
   }

   public static boolean areParsableAsIntegers(String... strings)
   {
      if (strings == null || strings.length == 0)
         return false;
      for (String string : strings)
      {
         if (!isParsableAsInteger(string))
            return false;
      }
      return true;
   }

   public static boolean isParsableAsInteger(String string)
   {
      if (string == null)
         return false;

      try
      {
         Integer.parseInt(string);
         return true;
      }
      catch (NumberFormatException e)
      {
         return false;
      }
   }

   public static boolean areAllInstanceOf(Class<?> type, Object... objects)
   {
      if (objects == null || objects.length == 0)
         return false;

      for (Object object : objects)
      {
         if (!type.isInstance(object))
            return false;
      }
      return true;
   }
}
