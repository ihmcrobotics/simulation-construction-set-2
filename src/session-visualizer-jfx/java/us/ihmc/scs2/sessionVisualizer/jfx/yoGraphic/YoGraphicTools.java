package us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.yoGraphic.*;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicFXControllerTools;
import us.ihmc.scs2.sessionVisualizer.jfx.definition.JavaFXVisualTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools.YoVariableDatabase;

public class YoGraphicTools
{
   public static final String ROOT_NAME = "root";
   public static final String SEPARATOR = ":";

   public static List<String> collectAllExistingNamespaces(YoGroupFX group)
   {
      List<String> namespaces = new ArrayList<>();
      if (!group.isRoot())
         namespaces.add(group.getFullname());
      for (YoGroupFX child : group.getChildren())
         namespaces.addAll(collectAllExistingNamespaces(child));
      return namespaces;
   }

   public static boolean yoGraphicFXGroupContainsItem(String itemName, Class<? extends YoGraphicFXItem> itemType, YoGroupFX group)
   {
      if (YoGraphicFX2D.class.isAssignableFrom(itemType))
         return group.containsYoGraphicFX2D(itemName);
      else if (YoGraphicFX3D.class.isAssignableFrom(itemType))
         return group.containsYoGraphicFX3D(itemName);
      else if (YoGroupFX.class.isAssignableFrom(itemType))
         return group.containsChild(itemName);
      else
         throw new RuntimeException("Unexpected item type: " + itemType.getSimpleName());
   }

   public static <I extends YoGraphicFXItem> I findYoGraphicFXItem(YoGroupFX root, String namespace, String itemName, Class<I> itemType)
   {
      YoGroupFX graphicGroup = findYoGraphicFXGroup(root, namespace);
      if (graphicGroup == null)
         return null;
      else
      {
         if (YoGraphicFX2D.class.isAssignableFrom(itemType))
            return itemType.cast(graphicGroup.getYoGraphicFX2D(itemName));
         else if (YoGraphicFX3D.class.isAssignableFrom(itemType))
            return itemType.cast(graphicGroup.getYoGraphicFX3D(itemName));
         else if (YoGroupFX.class.isAssignableFrom(itemType))
            return itemType.cast(graphicGroup.getChild(itemName));
         else
            throw new RuntimeException("Unexpected item type: " + itemType.getSimpleName());
      }
   }

   public static YoGraphicFX2D findYoGraphicFX2D(YoGroupFX root, String namespace, String graphicName)
   {
      YoGroupFX graphicGroup = findYoGraphicFXGroup(root, namespace);
      if (graphicGroup == null)
         return null;
      else
         return graphicGroup.getYoGraphicFX2D(graphicName);
   }

   public static YoGraphicFX3D findYoGraphicFX3D(YoGroupFX root, String namespace, String graphicName)
   {
      YoGroupFX graphicGroup = findYoGraphicFXGroup(root, namespace);
      if (graphicGroup == null)
         return null;
      else
         return graphicGroup.getYoGraphicFX3D(graphicName);
   }

   public static YoGroupFX findYoGraphicFXGroup(YoGroupFX root, String namespace)
   {
      String[] groupNames;
      if (namespace.contains(YoGraphicTools.SEPARATOR))
         groupNames = namespace.split(YoGraphicTools.SEPARATOR);
      else
         groupNames = new String[] {namespace};

      if (groupNames == null || groupNames.length == 0)
         return null;

      int startIndex = 1;

      if (!groupNames[0].equals(root.getName()))
         startIndex = 0;

      YoGroupFX parent = root;

      for (int i = startIndex; i < groupNames.length; i++)
      {
         String groupName = groupNames[i];
         YoGroupFX child = parent.getChild(groupName);

         if (child == null)
            return null;

         parent = child;
      }

      return parent;
   }

   public static YoGroupFX findOrCreateYoGraphicFXGroup(YoGroupFX root, String namespace)
   {
      String[] groupNames;
      if (namespace.contains(YoGraphicTools.SEPARATOR))
         groupNames = namespace.split(YoGraphicTools.SEPARATOR);
      else
         groupNames = new String[] {namespace};

      if (groupNames == null || groupNames.length == 0)
         return null;

      int startIndex = groupNames[0].equals(root.getName()) ? 1 : 0;

      YoGroupFX parent = root;

      for (int i = startIndex; i < groupNames.length; i++)
      {
         String groupName = groupNames[i];
         YoGroupFX child = parent.getChild(groupName);

         if (child == null)
         {
            child = new YoGroupFX(groupName);
            parent.addChild(child);
         }

         parent = child;
      }

      return parent;
   }

   public static List<YoGraphicFXItem> createYoGraphicFXs(YoVariableDatabase yoVariableDatabase, YoGroupFX parentGroup,
                                                          YoGraphicFXResourceManager resourceManager, ReferenceFrameManager referenceFrameManager,
                                                          YoGraphicListDefinition yoGraphicListDefinition)
   {
      if (yoGraphicListDefinition.getYoGraphics() == null)
         return null;

      List<YoGraphicFXItem> items = new ArrayList<>();

      for (YoGraphicDefinition definition : yoGraphicListDefinition.getYoGraphics())
      {
         YoGraphicFXItem item = createYoGraphicFX(yoVariableDatabase, parentGroup, resourceManager, referenceFrameManager, definition);
         if (item != null)
            items.add(item);
      }

      return items;
   }

   public static YoGraphicFXItem createYoGraphicFX(YoVariableDatabase yoVariableDatabase, YoGroupFX parentGroup, YoGraphicFXResourceManager resourceManager,
                                                   ReferenceFrameManager referenceFrameManager, YoGraphicDefinition yoGraphicDefinition)
   {
      YoGraphicFXItem yoGraphicFX = toYoGraphicFX(yoVariableDatabase, resourceManager, referenceFrameManager, yoGraphicDefinition);

      if (yoGraphicFX == null)
      {
         LogTools.error("Failed to load YoGraphicDefinition: " + yoGraphicDefinition);
         return null;
      }

      boolean isValidName;
      if (yoGraphicFX instanceof YoGroupFX)
         isValidName = !parentGroup.containsChild(yoGraphicFX.getName());
      else if (yoGraphicFX instanceof YoGraphicFX2D)
         isValidName = !parentGroup.containsYoGraphicFX2D(yoGraphicFX.getName());
      else if (yoGraphicFX instanceof YoGraphicFX3D)
         isValidName = !parentGroup.containsYoGraphicFX3D(yoGraphicFX.getName());
      else
         throw new RuntimeException("The following " + YoGraphicFX.class.getSimpleName() + " has unexpected hierarchy "
               + yoGraphicFX.getClass().getSimpleName());

      if (!isValidName)
         yoGraphicFX.setName(YoGraphicFXControllerTools.createAvailableYoGraphicFXItemName(parentGroup, yoGraphicFX.getName(), yoGraphicFX.getClass()));

      return yoGraphicFX;
   }

   public static YoGraphicFXItem toYoGraphicFX(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                               ReferenceFrameManager referenceFrameManager, YoGraphicDefinition definition)
   {
      try
      {
         if (definition instanceof YoGraphicGroupDefinition)
            return toYoGroupFX(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicGroupDefinition) definition);
         else if (definition instanceof YoGraphicPoint2DDefinition)
            return toYoPointFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPoint2DDefinition) definition);
         else if (definition instanceof YoGraphicPolygon2DDefinition)
            return toYoGraphicPolygonFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPolygon2DDefinition) definition);
         else if (definition instanceof YoGraphicPointcloud2DDefinition)
            return toYoPointcloudFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPointcloud2DDefinition) definition);
         else if (definition instanceof YoGraphicLine2DDefinition)
            return toYoLineFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicLine2DDefinition) definition);
         else if (definition instanceof YoGraphicPoint3DDefinition)
            return toYoPointFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPoint3DDefinition) definition);
         else if (definition instanceof YoGraphicArrow3DDefinition)
            return toYoArrowFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicArrow3DDefinition) definition);
         else if (definition instanceof YoGraphicCapsule3DDefinition)
            return toYoCapsuleFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicCapsule3DDefinition) definition);
         else if (definition instanceof YoGraphicPointcloud3DDefinition)
            return toYoPointcloudFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPointcloud3DDefinition) definition);
         else if (definition instanceof YoGraphicPolynomial3DDefinition)
            return toYoPolynomialFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPolynomial3DDefinition) definition);
         else if (definition instanceof YoGraphicCoordinateSystem3DDefinition)
            return toYoCoordinateSystemFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicCoordinateSystem3DDefinition) definition);
         else if (definition instanceof YoGraphicPolygonExtruded3DDefinition)
            return toYoPolygonExtrudedFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicPolygonExtruded3DDefinition) definition);
         else if (definition instanceof YoGraphicBox3DDefinition)
            return toYoBoxFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, (YoGraphicBox3DDefinition) definition);
         else
            throw new UnsupportedOperationException("Unhandled graphic type: " + definition.getClass().getSimpleName());
      }
      catch (NullPointerException e)
      {
         LogTools.error("Could not load: " + definition.getClass().getSimpleName() + ", " + definition.getName() + ", reason: " + e.getMessage());
         return null;
      }
   }

   private static void toYoGraphicFX(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                     ReferenceFrameManager referenceFrameManager, YoGraphicDefinition definition, YoGraphicFX yoGraphicFXToPack)
   {
      yoGraphicFXToPack.setName(definition.getName());
      yoGraphicFXToPack.setVisible(definition.isVisible());
   }

   private static void toYoGraphicFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                       ReferenceFrameManager referenceFrameManager, YoGraphic2DDefinition definition, YoGraphicFX2D yoGraphicFXToPack)
   {
      toYoGraphicFX(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      Color fillColor = JavaFXVisualTools.toColor(definition.getFillColor(), null);
      if (fillColor != null)
         yoGraphicFXToPack.setFillColor(fillColor);
      else
         yoGraphicFXToPack.setStrokeColor(JavaFXVisualTools.toColor(definition.getStrokeColor()));
      yoGraphicFXToPack.setStrokeWidth(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getStrokeWidth()));
   }

   public static YoGroupFX toYoGroupFX(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                       ReferenceFrameManager referenceFrameManager, YoGraphicGroupDefinition definition)
   {
      YoGroupFX yoGroupFX = new YoGroupFX(definition.getName());
      yoGroupFX.setVisible(definition.isVisible());
      for (YoGraphicDefinition child : definition.getChildren())
      {
         YoGraphicFXItem yoGraphicFX = toYoGraphicFX(yoVariableDatabase, resourceManager, referenceFrameManager, child);
         if (yoGraphicFX != null)
            yoGroupFX.addYoGraphicFXItem(yoGraphicFX);
      }
      return yoGroupFX;
   }

   public static YoPointFX2D toYoPointFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                           ReferenceFrameManager referenceFrameManager, YoGraphicPoint2DDefinition definition)
   {
      YoPointFX2D yoGraphicFX = new YoPointFX2D();
      toYoPointFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPointFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                    ReferenceFrameManager referenceFrameManager, YoGraphicPoint2DDefinition definition, YoPointFX2D yoGraphicFXToPack)
   {
      toYoGraphicFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPosition(CompositePropertyTools.toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition.getPosition()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getSize()));
      yoGraphicFXToPack.setGraphicResource(resourceManager.loadGraphic2DResource(definition.getGraphicName()));
   }

   public static YoPolygonFX2D toYoGraphicPolygonFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                      ReferenceFrameManager referenceFrameManager, YoGraphicPolygon2DDefinition definition)
   {
      YoPolygonFX2D yoGraphicFX = new YoPolygonFX2D();
      toYoGraphicPolygonFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoGraphicPolygonFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                             ReferenceFrameManager referenceFrameManager, YoGraphicPolygon2DDefinition definition,
                                             YoPolygonFX2D yoGraphicFXToPack)
   {
      toYoGraphicFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setVertices(CompositePropertyTools.toTuple2DPropertyList(yoVariableDatabase, referenceFrameManager, definition.getVertices()));
      yoGraphicFXToPack.setNumberOfVertices(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfVertices()));
   }

   public static YoPointcloudFX2D toYoPointcloudFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                     ReferenceFrameManager referenceFrameManager, YoGraphicPointcloud2DDefinition definition)
   {
      YoPointcloudFX2D yoGraphicFX = new YoPointcloudFX2D();
      toYoPointcloudFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPointcloudFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                         ReferenceFrameManager referenceFrameManager, YoGraphicPointcloud2DDefinition definition,
                                         YoPointcloudFX2D yoGraphicFXToPack)
   {
      toYoGraphicFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPoints(CompositePropertyTools.toTuple2DPropertyList(yoVariableDatabase, referenceFrameManager, definition.getPoints()));
      yoGraphicFXToPack.setNumberOfPoints(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfPoints()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getSize()));
      yoGraphicFXToPack.setGraphicResource(resourceManager.loadGraphic2DResource(definition.getGraphicName()));
   }

   public static YoLineFX2D toYoLineFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                         ReferenceFrameManager referenceFrameManager, YoGraphicLine2DDefinition definition)
   {
      YoLineFX2D yoGraphicFX = new YoLineFX2D();
      toYoLineFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoLineFX2D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                   ReferenceFrameManager referenceFrameManager, YoGraphicLine2DDefinition definition, YoLineFX2D yoGraphicFXToPack)
   {
      toYoGraphicFX2D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setOrigin(CompositePropertyTools.toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition.getOrigin()));
      yoGraphicFXToPack.setDirection(CompositePropertyTools.toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition.getDirection()));
      yoGraphicFXToPack.setDestination(CompositePropertyTools.toTuple2DProperty(yoVariableDatabase, referenceFrameManager, definition.getDestination()));
   }

   private static void toYoGraphicFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                       ReferenceFrameManager referenceFrameManager, YoGraphic3DDefinition definition, YoGraphicFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setColor(JavaFXVisualTools.toColor(definition.getColor()));
   }

   public static YoPointFX3D toYoPointFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                           ReferenceFrameManager referenceFrameManager, YoGraphicPoint3DDefinition definition)
   {
      YoPointFX3D yoGraphicFX = new YoPointFX3D();
      toYoPointFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPointFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                    ReferenceFrameManager referenceFrameManager, YoGraphicPoint3DDefinition definition, YoPointFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPosition(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getPosition()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getSize()));
      yoGraphicFXToPack.setGraphicResource(resourceManager.loadGraphic3DResource(definition.getGraphicName()));
   }

   public static YoArrowFX3D toYoArrowFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                           ReferenceFrameManager referenceFrameManager, YoGraphicArrow3DDefinition definition)
   {
      YoArrowFX3D yoGraphicFX = new YoArrowFX3D();
      toYoArrowFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoArrowFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                    ReferenceFrameManager referenceFrameManager, YoGraphicArrow3DDefinition definition, YoArrowFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setOrigin(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getOrigin()));
      yoGraphicFXToPack.setDirection(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getDirection()));
      yoGraphicFXToPack.setScaleLength(definition.isScaleLength());
      yoGraphicFXToPack.setBodyLength(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getBodyLength()));
      yoGraphicFXToPack.setHeadLength(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getHeadLength()));
      yoGraphicFXToPack.setScaleRadius(definition.isScaleRadius());
      yoGraphicFXToPack.setBodyRadius(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getBodyRadius()));
      yoGraphicFXToPack.setHeadRadius(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getHeadRadius()));
   }

   public static YoCapsuleFX3D toYoCapsuleFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                               ReferenceFrameManager referenceFrameManager, YoGraphicCapsule3DDefinition definition)
   {
      YoCapsuleFX3D yoGraphicFX = new YoCapsuleFX3D();
      toYoCapsuleFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoCapsuleFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                      ReferenceFrameManager referenceFrameManager, YoGraphicCapsule3DDefinition definition, YoCapsuleFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setCenter(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getCenter()));
      yoGraphicFXToPack.setAxis(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getAxis()));
      yoGraphicFXToPack.setLength(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getLength()));
      yoGraphicFXToPack.setRadius(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getRadius()));
   }

   public static YoPointcloudFX3D toYoPointcloudFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                     ReferenceFrameManager referenceFrameManager, YoGraphicPointcloud3DDefinition definition)
   {
      YoPointcloudFX3D yoGraphicFX = new YoPointcloudFX3D();
      toYoPointcloudFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPointcloudFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                         ReferenceFrameManager referenceFrameManager, YoGraphicPointcloud3DDefinition definition,
                                         YoPointcloudFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPoints(CompositePropertyTools.toTuple3DPropertyList(yoVariableDatabase, referenceFrameManager, definition.getPoints()));
      yoGraphicFXToPack.setNumberOfPoints(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfPoints()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getSize()));
      yoGraphicFXToPack.setGraphicResource(resourceManager.loadGraphic3DResource(definition.getGraphicName()));
   }

   public static YoPolynomialFX3D toYoPolynomialFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                     ReferenceFrameManager referenceFrameManager, YoGraphicPolynomial3DDefinition definition)
   {
      YoPolynomialFX3D yoGraphicFX = new YoPolynomialFX3D();
      toYoPolynomialFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPolynomialFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                         ReferenceFrameManager referenceFrameManager, YoGraphicPolynomial3DDefinition definition,
                                         YoPolynomialFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setCoefficientsX(CompositePropertyTools.toDoublePropertyList(yoVariableDatabase, definition.getCoefficientsX()));
      yoGraphicFXToPack.setCoefficientsY(CompositePropertyTools.toDoublePropertyList(yoVariableDatabase, definition.getCoefficientsY()));
      yoGraphicFXToPack.setCoefficientsZ(CompositePropertyTools.toDoublePropertyList(yoVariableDatabase, definition.getCoefficientsZ()));
      yoGraphicFXToPack.setNumberOfCoefficientsX(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfCoefficientsX()));
      yoGraphicFXToPack.setNumberOfCoefficientsY(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfCoefficientsY()));
      yoGraphicFXToPack.setNumberOfCoefficientsZ(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfCoefficientsZ()));
      yoGraphicFXToPack.setStartTime(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getStartTime()));
      yoGraphicFXToPack.setEndTime(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getEndTime()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getSize()));
   }

   public static YoCoordinateSystemFX3D toYoCoordinateSystemFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                                 ReferenceFrameManager referenceFrameManager, YoGraphicCoordinateSystem3DDefinition definition)
   {
      YoCoordinateSystemFX3D yoGraphicFX = new YoCoordinateSystemFX3D();
      toYoCoordinateSystemFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoCoordinateSystemFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                               ReferenceFrameManager referenceFrameManager, YoGraphicCoordinateSystem3DDefinition definition,
                                               YoCoordinateSystemFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPosition(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getPosition()));
      yoGraphicFXToPack.setOrientation(CompositePropertyTools.toOrientation3DProperty(yoVariableDatabase, referenceFrameManager, definition.getOrientation()));
      yoGraphicFXToPack.setBodyLength(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getBodyLength()));
      yoGraphicFXToPack.setHeadLength(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getHeadLength()));
      yoGraphicFXToPack.setBodyRadius(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getBodyRadius()));
      yoGraphicFXToPack.setHeadRadius(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getHeadRadius()));
   }

   public static YoPolygonExtrudedFX3D toYoPolygonExtrudedFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                                               ReferenceFrameManager referenceFrameManager, YoGraphicPolygonExtruded3DDefinition definition)
   {
      YoPolygonExtrudedFX3D yoGraphicFX = new YoPolygonExtrudedFX3D();
      toYoPolygonExtrudedFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoPolygonExtrudedFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                              ReferenceFrameManager referenceFrameManager, YoGraphicPolygonExtruded3DDefinition definition,
                                              YoPolygonExtrudedFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPosition(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getPosition()));
      yoGraphicFXToPack.setOrientation(CompositePropertyTools.toOrientation3DProperty(yoVariableDatabase, referenceFrameManager, definition.getOrientation()));
      yoGraphicFXToPack.setVertices(CompositePropertyTools.toTuple2DPropertyList(yoVariableDatabase, referenceFrameManager, definition.getVertices()));
      yoGraphicFXToPack.setNumberOfVertices(CompositePropertyTools.toIntegerProperty(yoVariableDatabase, definition.getNumberOfVertices()));
      yoGraphicFXToPack.setThickness(CompositePropertyTools.toDoubleProperty(yoVariableDatabase, definition.getThickness()));
   }

   public static YoBoxFX3D toYoBoxFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                       ReferenceFrameManager referenceFrameManager, YoGraphicBox3DDefinition definition)
   {
      YoBoxFX3D yoGraphicFX = new YoBoxFX3D();
      toYoBoxFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFX);
      return yoGraphicFX;
   }

   public static void toYoBoxFX3D(YoVariableDatabase yoVariableDatabase, YoGraphicFXResourceManager resourceManager,
                                  ReferenceFrameManager referenceFrameManager, YoGraphicBox3DDefinition definition, YoBoxFX3D yoGraphicFXToPack)
   {
      toYoGraphicFX3D(yoVariableDatabase, resourceManager, referenceFrameManager, definition, yoGraphicFXToPack);
      yoGraphicFXToPack.setPosition(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getPosition()));
      yoGraphicFXToPack.setOrientation(CompositePropertyTools.toOrientation3DProperty(yoVariableDatabase, referenceFrameManager, definition.getOrientation()));
      yoGraphicFXToPack.setSize(CompositePropertyTools.toTuple3DProperty(yoVariableDatabase, referenceFrameManager, definition.getSize()));
   }

   public static YoGraphicListDefinition toYoGraphicListDefinition(Collection<? extends YoGraphicFXItem> yoGraphicFXs)
   {
      return new YoGraphicListDefinition(yoGraphicFXs.stream().map(YoGraphicTools::toYoGraphicDefinition).collect(Collectors.toList()));
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicFXItem yoGraphicFX)
   {
      if (yoGraphicFX instanceof YoGroupFX)
         return toYoGraphicGroupDefinition((YoGroupFX) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPointFX2D)
         return toYoGraphicPoint2DDefinition((YoPointFX2D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPolygonFX2D)
         return toYoGraphicPolygon2DDefinition((YoPolygonFX2D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPointcloudFX2D)
         return toYoGraphicPointcloud2DDefinition((YoPointcloudFX2D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoLineFX2D)
         return toYoGraphicLine2DDefinition((YoLineFX2D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoArrowFX3D)
         return toYoGraphicArrow3DDefinition((YoArrowFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoCapsuleFX3D)
         return toYoGraphicCapsule3DDefinition((YoCapsuleFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPointFX3D)
         return toYoGraphicPoint3DDefinition((YoPointFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPointcloudFX3D)
         return toYoGraphicPointcloud3DDefinition((YoPointcloudFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPolynomialFX3D)
         return toYoGraphicPolynomial3DDefinition((YoPolynomialFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoCoordinateSystemFX3D)
         return toYoGraphicCoordinateSystem3DDefinition((YoCoordinateSystemFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoPolygonExtrudedFX3D)
         return toYoGraphicPolygonExtruded3DDefinition((YoPolygonExtrudedFX3D) yoGraphicFX);
      else if (yoGraphicFX instanceof YoBoxFX3D)
         return toYoGraphicBox3DDefinition((YoBoxFX3D) yoGraphicFX);
      else
         throw new UnsupportedOperationException("Unsupported " + YoGraphicFX.class.getSimpleName() + ": " + yoGraphicFX.getClass().getSimpleName());
   }

   public static YoGraphicGroupDefinition toYoGraphicGroupDefinition(YoGroupFX yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicGroupDefinition definition = new YoGraphicGroupDefinition();
      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setChildren(yoGraphicFX.getItemChildren().stream().map(YoGraphicTools::toYoGraphicDefinition).collect(Collectors.toList()));
      return definition;
   }

   public static YoGraphicPoint2DDefinition toYoGraphicPoint2DDefinition(YoPointFX2D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPoint2DDefinition definition = new YoGraphicPoint2DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPosition(CompositePropertyTools.toYoTuple2DDefinition(yoGraphicFX.getPosition()));
      definition.setSize(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getSize()));
      definition.setFillColor(toColorDefinition(yoGraphicFX.getFillColor()));
      definition.setStrokeColor(toColorDefinition(yoGraphicFX.getStrokeColor()));
      definition.setStrokeWidth(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getStrokeWidth()));
      definition.setGraphicName(yoGraphicFX.getGraphicResource().getResourceName());

      return definition;
   }

   public static YoGraphicPolygon2DDefinition toYoGraphicPolygon2DDefinition(YoPolygonFX2D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPolygon2DDefinition definition = new YoGraphicPolygon2DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setVertices(yoGraphicFX.getVertices().stream().map(CompositePropertyTools::toYoTuple2DDefinition).collect(Collectors.toList()));
      definition.setNumberOfVertices(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfVertices()));
      definition.setFillColor(toColorDefinition(yoGraphicFX.getFillColor()));
      definition.setStrokeColor(toColorDefinition(yoGraphicFX.getStrokeColor()));
      definition.setStrokeWidth(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getStrokeWidth()));

      return definition;
   }

   public static YoGraphicPointcloud2DDefinition toYoGraphicPointcloud2DDefinition(YoPointcloudFX2D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPointcloud2DDefinition definition = new YoGraphicPointcloud2DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPoints(yoGraphicFX.getPoints().stream().map(CompositePropertyTools::toYoTuple2DDefinition).collect(Collectors.toList()));
      definition.setNumberOfPoints(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfPoints()));
      definition.setSize(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getSize()));
      definition.setGraphicName(yoGraphicFX.getGraphicResource().getResourceName());
      definition.setFillColor(toColorDefinition(yoGraphicFX.getFillColor()));
      definition.setStrokeColor(toColorDefinition(yoGraphicFX.getStrokeColor()));
      definition.setStrokeWidth(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getStrokeWidth()));

      return definition;
   }

   public static YoGraphicLine2DDefinition toYoGraphicLine2DDefinition(YoLineFX2D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicLine2DDefinition definition = new YoGraphicLine2DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setOrigin(CompositePropertyTools.toYoTuple2DDefinition(yoGraphicFX.getOrigin()));
      definition.setDirection(CompositePropertyTools.toYoTuple2DDefinition(yoGraphicFX.getDirection()));
      definition.setDestination(CompositePropertyTools.toYoTuple2DDefinition(yoGraphicFX.getDestination()));
      definition.setFillColor(toColorDefinition(yoGraphicFX.getFillColor()));
      definition.setStrokeColor(toColorDefinition(yoGraphicFX.getStrokeColor()));
      definition.setStrokeWidth(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getStrokeWidth()));

      return definition;
   }

   public static YoGraphicArrow3DDefinition toYoGraphicArrow3DDefinition(YoArrowFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicArrow3DDefinition definition = new YoGraphicArrow3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setOrigin(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getOrigin()));
      definition.setDirection(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getDirection()));
      definition.setScaleLength(yoGraphicFX.getScaleLength());
      definition.setScaleRadius(yoGraphicFX.getScaleRadius());
      definition.setBodyLength(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getBodyLength()));
      definition.setHeadLength(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getHeadLength()));
      definition.setBodyRadius(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getBodyRadius()));
      definition.setHeadRadius(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getHeadRadius()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicCapsule3DDefinition toYoGraphicCapsule3DDefinition(YoCapsuleFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicCapsule3DDefinition definition = new YoGraphicCapsule3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setCenter(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getCenter()));
      definition.setAxis(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getAxis()));
      definition.setLength(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getLength()));
      definition.setRadius(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getRadius()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicPoint3DDefinition toYoGraphicPoint3DDefinition(YoPointFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPoint3DDefinition definition = new YoGraphicPoint3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPosition(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getPosition()));
      definition.setSize(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getSize()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));
      definition.setGraphicName(yoGraphicFX.getGraphicResource().getResourceName());

      return definition;
   }

   public static YoGraphicPointcloud3DDefinition toYoGraphicPointcloud3DDefinition(YoPointcloudFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPointcloud3DDefinition definition = new YoGraphicPointcloud3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPoints(yoGraphicFX.getPoints().stream().map(CompositePropertyTools::toYoTuple3DDefinition).collect(Collectors.toList()));
      definition.setNumberOfPoints(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfPoints()));
      definition.setSize(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getSize()));
      definition.setGraphicName(yoGraphicFX.getGraphicResource().getResourceName());
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicPolynomial3DDefinition toYoGraphicPolynomial3DDefinition(YoPolynomialFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPolynomial3DDefinition definition = new YoGraphicPolynomial3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setCoefficientsX(CompositePropertyTools.toDoublePropertyNames(yoGraphicFX.getCoefficientsX()));
      definition.setCoefficientsY(CompositePropertyTools.toDoublePropertyNames(yoGraphicFX.getCoefficientsY()));
      definition.setCoefficientsZ(CompositePropertyTools.toDoublePropertyNames(yoGraphicFX.getCoefficientsZ()));
      definition.setNumberOfCoefficientsX(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfCoefficientsX()));
      definition.setNumberOfCoefficientsY(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfCoefficientsY()));
      definition.setNumberOfCoefficientsZ(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfCoefficientsZ()));
      definition.setStartTime(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getStartTime()));
      definition.setEndTime(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getEndTime()));
      definition.setSize(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getSize()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicCoordinateSystem3DDefinition toYoGraphicCoordinateSystem3DDefinition(YoCoordinateSystemFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicCoordinateSystem3DDefinition definition = new YoGraphicCoordinateSystem3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPosition(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getPosition()));
      definition.setOrientation(CompositePropertyTools.toYoOrientation3DDefinition(yoGraphicFX.getOrientation()));
      definition.setBodyLength(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getBodyLength()));
      definition.setHeadLength(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getHeadLength()));
      definition.setBodyRadius(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getBodyRadius()));
      definition.setHeadRadius(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getHeadRadius()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicPolygonExtruded3DDefinition toYoGraphicPolygonExtruded3DDefinition(YoPolygonExtrudedFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicPolygonExtruded3DDefinition definition = new YoGraphicPolygonExtruded3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPosition(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getPosition()));
      definition.setOrientation(CompositePropertyTools.toYoOrientation3DDefinition(yoGraphicFX.getOrientation()));
      definition.setVertices(yoGraphicFX.getVertices().stream().map(CompositePropertyTools::toYoTuple2DDefinition).collect(Collectors.toList()));
      definition.setNumberOfVertices(CompositePropertyTools.toIntegerPropertyName(yoGraphicFX.getNumberOfVertices()));
      definition.setThickness(CompositePropertyTools.toDoublePropertyName(yoGraphicFX.getThickness()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static YoGraphicBox3DDefinition toYoGraphicBox3DDefinition(YoBoxFX3D yoGraphicFX)
   {
      if (yoGraphicFX == null)
         return null;

      YoGraphicBox3DDefinition definition = new YoGraphicBox3DDefinition();

      definition.setName(yoGraphicFX.getName());
      definition.setVisible(yoGraphicFX.isVisible());
      definition.setPosition(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getPosition()));
      definition.setOrientation(CompositePropertyTools.toYoOrientation3DDefinition(yoGraphicFX.getOrientation()));
      definition.setSize(CompositePropertyTools.toYoTuple3DDefinition(yoGraphicFX.getSize()));
      definition.setColor(toColorDefinition(yoGraphicFX.getColor()));

      return definition;
   }

   public static ColorDefinition toColorDefinition(Supplier<Color> color)
   {
      if (color == null || color.get() == null)
         return null;
      double red = color.get().getRed();
      double green = color.get().getGreen();
      double blue = color.get().getBlue();
      double alpha = color.get().getOpacity();
      return new ColorDefinition(red, green, blue, alpha);
   }

   public static List<Shape> extractShapes(Group group)
   {
      if (group == null || group.getChildren().isEmpty())
         return Collections.emptyList();

      List<Shape> shapes = new ArrayList<>();

      for (Node child : group.getChildren())
      {
         if (child instanceof Shape)
            shapes.add((Shape) child);
         else if (child instanceof Group)
            shapes.addAll(extractShapes((Group) child));
      }

      return shapes;
   }

   public static List<Shape3D> extractShape3Ds(Collection<? extends Node> nodes)
   {
      if (nodes == null || nodes.isEmpty())
         return Collections.emptyList();

      List<Shape3D> shapes = new ArrayList<>();

      for (Node node : nodes)
      {
         if (node instanceof Shape3D)
            shapes.add((Shape3D) node);
         else if (node instanceof Group)
            shapes.addAll(extractShape3Ds(((Group) node).getChildren()));
      }

      return shapes;
   }

   public static boolean isAnyNull(Object... objects)
   {
      for (Object object : objects)
      {
         if (object == null)
            return true;
      }
      return false;
   }
}
