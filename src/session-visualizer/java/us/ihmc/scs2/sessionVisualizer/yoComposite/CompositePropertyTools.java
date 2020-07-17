package us.ihmc.scs2.sessionVisualizer.yoComposite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.commons.text.similarity.SimilarityScore;

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
import us.ihmc.scs2.sessionVisualizer.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.tools.YoVariableTools;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.tools.YoFrameVariableNameTools;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoVariable;

public class CompositePropertyTools
{
   public static List<CompositeProperty> toCompositePropertyList(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                                 List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static List<Tuple2DProperty> toTuple2DPropertyList(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                             List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static List<Tuple3DProperty> toTuple3DPropertyList(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                             List<? extends YoCompositeDefinition> definitionList)
   {
      if (definitionList == null)
         return null;
      return definitionList.stream().map(definition -> toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition)).collect(Collectors.toList());
   }

   public static CompositeProperty toCompositeProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                       YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;

      CompositeProperty property = new CompositeProperty(definition.getType(), definition.getComponentIdentifiers());
      property.setComponentValueProperties(toDoublePropertyArray(yoVariableDatabase, definition.getComponentValues()));
      property.setReferenceFrameProperty(toReferenceFrameProperty(yoVariableDatabase, referenceFrameManager, definition.getReferenceFrame()));
      return property;
   }

   public static Tuple2DProperty toTuple2DProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                   YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new Tuple2DProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static Tuple3DProperty toTuple3DProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                   YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new Tuple3DProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static Orientation3DProperty toOrientation3DProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
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

   public static QuaternionProperty toQuaternionProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                         YoCompositeDefinition definition)
   {
      if (definition == null)
         return null;
      return new QuaternionProperty(toCompositeProperty(yoVariableDatabase, referenceFrameManager, definition));
   }

   public static YawPitchRollProperty toYawPitchRollProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
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
            yoDouble = (YoDouble) yoVariableDatabase.searchSimilar(field, 0.90, YoDouble.class);
         }
         Objects.requireNonNull(yoDouble, "Could not find the YoVariable: " + field);
         return new YoDoubleProperty(yoDouble);
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
         Objects.requireNonNull(yoInteger, "Could not find the YoVariable: " + field);
         return new YoIntegerProperty(yoInteger);
      }
   }

   public static Property<ReferenceFrame> toReferenceFrameProperty(YoVariableDatabase yoVariableDatabase, ReferenceFrameManager referenceFrameManager,
                                                                   String field)
   {
      if (field == null)
         return null;
      ReferenceFrame referenceFrame = referenceFrameManager.getReferenceFrameFromFullname(field);
      if (referenceFrame != null)
         return new SimpleObjectProperty<>(referenceFrame);
      return null;
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

   public static String toReferenceFramePropertyName(Property<ReferenceFrame> referenceFrameProperty)
   {
      if (referenceFrameProperty == null || referenceFrameProperty.getValue() == null)
         return null;
      else if (referenceFrameProperty instanceof SimpleObjectProperty)
         return ReferenceFrameManager.getFullname(referenceFrameProperty.getValue());
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

   public static boolean isParsableAsInteger(String string)
   {
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

   public static class YoVariableDatabase
   {
      private final YoRegistry rootRegistry;
      private List<YoVariable> allYoVariables;

      private final Map<String, YoVariable> previousSearchResults = new HashMap<>();
      private final Map<String, String> fromSearchToBestSubname = new HashMap<>();

      private final SimilarityScore<Double> seachEngine;

      public YoVariableDatabase(YoRegistry rootRegistry)
      {
         this.rootRegistry = rootRegistry;
         seachEngine = new SimilarityScore<Double>()
         {
            SimilarityScore<Integer> caseSensitiveSearchEngine = new LongestCommonSubsequence();

            @Override
            public Double apply(CharSequence left, CharSequence right)
            {
               return caseSensitiveSearchEngine.apply(left.toString().toLowerCase(), right.toString().toLowerCase()).doubleValue()
                     / (double) Math.max(left.length(), right.length());
            }
         };
      }

      public YoVariable searchExact(String fullnameToSearch)
      {
         return rootRegistry.findVariable(fullnameToSearch);
      }

      public YoVariable searchSimilar(String fullnameToSearch, double minScore)
      {
         return searchSimilar(fullnameToSearch, minScore, YoVariable.class);
      }

      @SuppressWarnings("unchecked")
      public <T extends YoVariable> T searchSimilar(String fullnameToSearch, double minScore, Class<? extends T> type)
      {
         if (previousSearchResults.containsKey(fullnameToSearch))
            return type.cast(previousSearchResults.get(fullnameToSearch));

         for (Entry<String, String> entry : fromSearchToBestSubname.entrySet())
         {
            String bestSubname = entry.getKey();
            String searchSubname = entry.getValue();
            if (fullnameToSearch.contains(searchSubname))
            {
               YoVariable candidate = rootRegistry.findVariable(fullnameToSearch.replace(searchSubname, bestSubname));

               if (candidate != null && type.isInstance(candidate))
               {
                  LogTools.info("Search for: " + fullnameToSearch + ", found: " + candidate.getFullNameString());
                  previousSearchResults.put(fullnameToSearch, candidate);
                  return type.cast(candidate);
               }
            }
         }

         if (allYoVariables == null)
            allYoVariables = rootRegistry.subtreeVariables();

         List<T> correctTypeVariables;
         if (!YoVariable.class.equals(type))
            correctTypeVariables = allYoVariables.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
         else
            correctTypeVariables = (List<T>) allYoVariables;

         List<Number> score = new ArrayList<>();
         List<T> searchResult = YoVariableTools.search(correctTypeVariables, YoVariable::getFullNameString, fullnameToSearch, seachEngine, 1, score);

         if (searchResult != null)
         {
            T bestYoVariable = searchResult.get(0);
            String bestFullname = bestYoVariable.getFullNameString();
            String commonPrefix = YoFrameVariableNameTools.getCommonPrefix(fullnameToSearch, bestFullname);
            String commonSuffix = YoFrameVariableNameTools.getCommonSuffix(fullnameToSearch, bestFullname);
            String searchSubname = fullnameToSearch.substring(commonPrefix.length(), fullnameToSearch.length() - commonSuffix.length());
            String bestSubname = bestFullname.substring(commonPrefix.length(), bestFullname.length() - commonSuffix.length());

            LogTools.info("Score: " + score.get(0) + ", difference: [" + searchSubname + ", " + bestSubname + "], field: " + fullnameToSearch + ", result: "
                  + bestFullname);

            if (score.get(0).doubleValue() >= minScore)
            {
               fromSearchToBestSubname.put(bestSubname, searchSubname);
               previousSearchResults.put(fullnameToSearch, bestYoVariable);
               return bestYoVariable;
            }
         }

         return null;
      }
   }
}
