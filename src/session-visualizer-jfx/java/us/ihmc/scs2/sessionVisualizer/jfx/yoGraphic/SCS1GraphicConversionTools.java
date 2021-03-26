package us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.appearance.YoAppearanceMaterial;
import us.ihmc.graphicsDescription.appearance.YoAppearanceRGBColor;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphic;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicCoordinateSystem;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicCylinder;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicEllipsoid;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicLineSegment;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPolygon;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPolygon3D;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPolynomial3D;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicPosition;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicReferenceFrame;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicShape;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicText;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicText3D;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicTriangle;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicVRML;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicVector;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsList;
import us.ihmc.graphicsDescription.yoGraphics.YoGraphicsListRegistry;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.yoComposite.YoQuaternionDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple2DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicArrow3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCoordinateSystem3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCylinder3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicGroupDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPoint3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolygonExtruded3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolynomial3DDefinition;
import us.ihmc.yoVariables.variable.YoVariable;

public class SCS1GraphicConversionTools
{
   public static List<YoGraphicDefinition> toYoGraphicDefinitions(YoGraphicsListRegistry registry)
   {
      List<YoGraphicsList> yoGraphicsLists = new ArrayList<>();
      registry.getRegisteredYoGraphicsLists(yoGraphicsLists);

      List<YoGraphicDefinition> definitions = new ArrayList<>();
      yoGraphicsLists.forEach(yoGraphicsList -> definitions.add(toYoGraphicGroupDefinition(yoGraphicsList)));

      return definitions;
   }

   public static YoGraphicGroupDefinition toYoGraphicGroupDefinition(YoGraphicsList yoGraphicsList)
   {
      YoGraphicGroupDefinition groupDefinition = new YoGraphicGroupDefinition();
      groupDefinition.setName(yoGraphicsList.getLabel());
      groupDefinition.setChildren(toYoGraphicDefinitions(yoGraphicsList.getYoGraphics()));
      return groupDefinition;
   }

   public static List<YoGraphicDefinition> toYoGraphicDefinitions(Collection<? extends YoGraphic> yoGraphics)
   {
      return yoGraphics.stream().map(SCS1GraphicConversionTools::toYoGraphicDefinition).filter(definition -> definition != null).collect(Collectors.toList());
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphic yoGraphic)
   {
      if (yoGraphic == null)
         return null;

      YoGraphicDefinition definition = null;
      if (yoGraphic instanceof YoGraphicPolygon)
         definition = toYoGraphicDefinition((YoGraphicPolygon) yoGraphic);
      if (yoGraphic instanceof YoGraphicShape)
         definition = toYoGraphicDefinition((YoGraphicShape) yoGraphic);
      if (yoGraphic instanceof YoGraphicText)
         definition = toYoGraphicDefinition((YoGraphicText) yoGraphic);
      if (yoGraphic instanceof YoGraphicText3D)
         definition = toYoGraphicDefinition((YoGraphicText3D) yoGraphic);
      if (yoGraphic instanceof YoGraphicCoordinateSystem)
         definition = toYoGraphicDefinition((YoGraphicCoordinateSystem) yoGraphic);
      if (yoGraphic instanceof YoGraphicCylinder)
         definition = toYoGraphicDefinition((YoGraphicCylinder) yoGraphic);
      if (yoGraphic instanceof YoGraphicPolygon3D)
         definition = toYoGraphicDefinition((YoGraphicPolygon3D) yoGraphic);
      if (yoGraphic instanceof YoGraphicPolynomial3D)
         definition = toYoGraphicDefinition((YoGraphicPolynomial3D) yoGraphic);
      if (yoGraphic instanceof YoGraphicPosition)
         definition = toYoGraphicDefinition((YoGraphicPosition) yoGraphic);
      if (yoGraphic instanceof YoGraphicTriangle)
         definition = toYoGraphicDefinition((YoGraphicTriangle) yoGraphic);
      if (yoGraphic instanceof YoGraphicVector)
         definition = toYoGraphicDefinition((YoGraphicVector) yoGraphic);

      if (definition == null)
      {
         LogTools.error("Unsupported YoGraphic type: " + yoGraphic);
      }
      else
      {
         definition.setName(yoGraphic.getName());
         definition.setVisible(yoGraphic.isGraphicObjectShowing());
      }

      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicPolygon yoGraphicPolygon)
   {
      //      if (yoGraphicPolygon.isUsingYawPitchRoll())
      //      {
      //         LogTools.error("Yaw-pitch-roll is not yet supported: " + yoGraphicPolygon.getName());
      //         return null;
      //      }

      YoGraphicPolygonExtruded3DDefinition definition = new YoGraphicPolygonExtruded3DDefinition();
      definition.setName(yoGraphicPolygon.getName());
      YoVariable[] yoVariables = yoGraphicPolygon.getVariables();
      int yoVariableIndex = 0;
      double[] constants = yoGraphicPolygon.getConstants();
      int constantIndex = 1; // 0 corresponds to the scale factor

      int vertexBufferSize = (int) constants[constantIndex++];
      definition.setThickness(constants.length == 3 ? constants[constantIndex++] : 0.01);

      definition.setNumberOfVertices(yoVariables[yoVariableIndex++].getFullNameString());

      List<YoTuple2DDefinition> vertices = new ArrayList<>();

      for (int i = 0; i < vertexBufferSize; i++)
      {
         vertices.add(toYoTuple2DDefinition(yoVariables, yoVariableIndex));
         yoVariableIndex += 2;
      }

      definition.setPosition(toYoTuple3DDefinition(yoVariables, yoVariableIndex));
      yoVariableIndex += 3;

      if (yoGraphicPolygon.isUsingYawPitchRoll())
      {
         definition.setOrientation(toYoYawPitchRollDefinition(yoVariables, yoVariableIndex));
         yoVariableIndex += 3;
      }
      else
      {
         definition.setOrientation(toYoQuaternionDefinition(yoVariables, yoVariableIndex));
         yoVariableIndex += 4;
      }

      definition.setColor(toColorDefinition(yoGraphicPolygon.getAppearance()));

      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicShape yoGraphicShape)
   {
      // TODO Unsupported for now, need to convert Graphics3DObject
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicText yoGraphicText)
   {
      // TODO Unsupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicText3D yoGraphicText3D)
   {
      // TODO Unsupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicCoordinateSystem yoGraphicCoordinateSystem)
   {
      if (yoGraphicCoordinateSystem instanceof YoGraphicEllipsoid)
         return toYoGraphicDefinition((YoGraphicEllipsoid) yoGraphicCoordinateSystem);
      // TODO This is loaded the same way as a YoGraphicCoordinateSystem
      //      if (yoGraphicCoordinateSystem instanceof YoGraphicReferenceFrame)
      //         return toYoGraphicDefinition((YoGraphicReferenceFrame) yoGraphicCoordinateSystem);
      if (yoGraphicCoordinateSystem instanceof YoGraphicVRML)
         return toYoGraphicDefinition((YoGraphicVRML) yoGraphicCoordinateSystem);

      YoGraphicCoordinateSystem3DDefinition definition = new YoGraphicCoordinateSystem3DDefinition();
      definition.setName(yoGraphicCoordinateSystem.getName());

      YoVariable[] yoVariables = yoGraphicCoordinateSystem.getVariables();
      int yoVariableIndex = 0;
      double[] constants = yoGraphicCoordinateSystem.getConstants();

      definition.setPosition(toYoTuple3DDefinition(yoVariables, yoVariableIndex));
      yoVariableIndex += 3;

      if (yoVariables.length == 6)
      {
         definition.setOrientation(toYoYawPitchRollDefinition(yoVariables, yoVariableIndex));
         yoVariableIndex += 3;
      }
      else
      {
         definition.setOrientation(toYoQuaternionDefinition(yoVariables, yoVariableIndex));
         yoVariableIndex += 4;
      }

      double scale = constants[0];
      definition.setBodyLength(0.90 * scale);
      definition.setHeadLength(0.10 * scale);
      definition.setBodyRadius(0.02 * scale);
      definition.setHeadRadius(0.04 * scale);

      if (constants.length == 3)
      {
         definition.setColor(new ColorDefinition((int) constants[1]));
         definition.getColor().setAlpha(1.0 - constants[2]);
      }

      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicEllipsoid yoGraphicEllipsoid)
   {
      // TODO Unsupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicReferenceFrame yoGraphicReferenceFrame)
   {
      return toYoGraphicDefinition((YoGraphicCoordinateSystem) yoGraphicReferenceFrame);
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicVRML yoGraphicVRML)
   {
      // TODO Unsupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicCylinder yoGraphicCylinder)
   {
      YoGraphicCylinder3DDefinition definition = new YoGraphicCylinder3DDefinition();
      definition.setName(yoGraphicCylinder.getName());

      YoVariable[] yoVariables = yoGraphicCylinder.getVariables();
      int yoVariableIndex = 0;
      double[] constants = yoGraphicCylinder.getConstants();

      // FIXME the YoGraphicCylinder only gives the position of the base and not the center.
      definition.setCenter(toYoTuple3DDefinition(yoVariables, yoVariableIndex));
      yoVariableIndex += 3;
      definition.setAxis(toYoTuple3DDefinition(yoVariables, yoVariableIndex));
      yoVariableIndex += 3;

      definition.setRadius(constants[0]);
      definition.setColor(toColorDefinition(yoGraphicCylinder.getAppearance()));

      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicPolygon3D yoGraphicPolygon3D)
   {
      // TODO Unsupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicPolynomial3D yoGraphicPolynomial3D)
   {
      YoVariable[] yoVariables = yoGraphicPolynomial3D.getVariables();
      int yoVariableIndex = 0;
      double[] constants = yoGraphicPolynomial3D.getConstants();

      boolean hasPoseDefined = (int) constants[3] == 1;

      if (hasPoseDefined)
      {
         LogTools.error("Unsupported feature [hasPoseDefined] for " + yoGraphicPolynomial3D);
         return null;
      }

      int numberOfPolynomials = (int) constants[4];
      double[] polynomialSizes = Arrays.copyOfRange(constants, 5, 5 + 3 * numberOfPolynomials);

      if (numberOfPolynomials == 1)
      {
         YoGraphicPolynomial3DDefinition definition = new YoGraphicPolynomial3DDefinition();
         definition.setName(yoGraphicPolynomial3D.getName());
         definition.setSize(constants[0]);

         int xSize = (int) polynomialSizes[0];
         String xNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
         List<String> xCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + xSize - 1))
                                      .map(YoVariable::getFullNameString).collect(Collectors.toList());
         yoVariableIndex += xSize;

         int ySize = (int) polynomialSizes[1];
         String yNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
         List<String> yCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + ySize - 1))
                                      .map(YoVariable::getFullNameString).collect(Collectors.toList());
         yoVariableIndex += ySize;

         int zSize = (int) polynomialSizes[2];
         String zNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
         List<String> zCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + zSize - 1))
                                      .map(YoVariable::getFullNameString).collect(Collectors.toList());
         yoVariableIndex += ySize;

         definition.setCoefficientsX(xCoeffs);
         definition.setCoefficientsY(yCoeffs);
         definition.setCoefficientsZ(zCoeffs);
         definition.setNumberOfCoefficientsX(xNumberOfCoeffs);
         definition.setNumberOfCoefficientsY(yNumberOfCoeffs);
         definition.setNumberOfCoefficientsZ(zNumberOfCoeffs);
         definition.setStartTime(0.0);
         definition.setEndTime(yoVariables[yoVariableIndex].getFullNameString());
         return definition;
      }
      else
      {
         YoGraphicGroupDefinition groupDefinition = new YoGraphicGroupDefinition();
         groupDefinition.setName(yoGraphicPolynomial3D.getName());
         groupDefinition.setChildren(new ArrayList<>());

         YoVariable[] waypointTimes = Arrays.copyOfRange(yoVariables, yoVariables.length - 3 - numberOfPolynomials, yoVariables.length - 3 + 1);

         for (int i = 0; i < numberOfPolynomials; i++)
         {
            YoGraphicPolynomial3DDefinition definition = new YoGraphicPolynomial3DDefinition();
            definition.setName(yoGraphicPolynomial3D.getName() + Integer.toString(i));
            definition.setSize(constants[0]);

            int xSize = (int) polynomialSizes[0];
            String xNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
            List<String> xCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + xSize - 1))
                                         .map(YoVariable::getFullNameString).collect(Collectors.toList());
            yoVariableIndex += xSize;

            int ySize = (int) polynomialSizes[1];
            String yNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
            List<String> yCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + ySize - 1))
                                         .map(YoVariable::getFullNameString).collect(Collectors.toList());
            yoVariableIndex += ySize;

            int zSize = (int) polynomialSizes[2];
            String zNumberOfCoeffs = yoVariables[yoVariableIndex].getFullNameString();
            List<String> zCoeffs = Stream.of(Arrays.copyOfRange(yoVariables, yoVariableIndex + 1, yoVariableIndex + 1 + zSize - 1))
                                         .map(YoVariable::getFullNameString).collect(Collectors.toList());
            yoVariableIndex += ySize;

            definition.setCoefficientsX(xCoeffs);
            definition.setCoefficientsY(yCoeffs);
            definition.setCoefficientsZ(zCoeffs);
            definition.setNumberOfCoefficientsX(xNumberOfCoeffs);
            definition.setNumberOfCoefficientsY(yNumberOfCoeffs);
            definition.setNumberOfCoefficientsZ(zNumberOfCoeffs);
            definition.setStartTime(i == 0 ? Double.toString(0.0) : waypointTimes[i - 1].getFullNameString());
            definition.setEndTime(waypointTimes[i].getFullNameString());
            groupDefinition.getChildren().add(definition);
         }

         return groupDefinition;
      }
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicPosition yoGraphicPosition)
   {
      YoGraphicPoint3DDefinition definition = new YoGraphicPoint3DDefinition();
      definition.setName(yoGraphicPosition.getName());

      YoVariable[] yoVariables = yoGraphicPosition.getVariables();
      double[] constants = yoGraphicPosition.getConstants();

      YoTuple3DDefinition position = new YoTuple3DDefinition();
      position.setX(yoVariables[0].getFullNameString());
      position.setY(yoVariables[1].getFullNameString());
      position.setZ(yoVariables.length == 3 ? yoVariables[2].getFullNameString() : Double.toString(0.0));
      definition.setPosition(position);
      definition.setSize(constants[0]);
      definition.setColor(toColorDefinition(yoGraphicPosition.getAppearance()));
      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicTriangle yoGraphicTriangle)
   {
      // TODO Unupported for now
      return null;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicVector yoGraphicVector)
   {
      if (yoGraphicVector instanceof YoGraphicLineSegment)
         return toYoGraphicDefinition((YoGraphicLineSegment) yoGraphicVector);

      YoGraphicArrow3DDefinition definition = new YoGraphicArrow3DDefinition();
      definition.setName(yoGraphicVector.getName());

      YoVariable[] yoVariables = yoGraphicVector.getVariables();
      double[] constants = yoGraphicVector.getConstants();

      definition.setOrigin(toYoTuple3DDefinition(yoVariables, 0));
      definition.setDirection(toYoTuple3DDefinition(yoVariables, 3));

      double scale = constants[0];
      definition.setBodyLength(0.9000 * scale);
      definition.setHeadLength(0.1000 * scale);
      definition.setBodyRadius(0.0150 * scale);
      definition.setHeadRadius(0.0375 * scale);
      definition.setScaleLength(true);
      definition.setScaleRadius(true);
      definition.setColor(toColorDefinition(yoGraphicVector.getAppearance()));
      return definition;
   }

   public static YoGraphicDefinition toYoGraphicDefinition(YoGraphicLineSegment yoGraphicLineSegment)
   {
      // TODO Unsupported for now
      return null;
   }

   public static ColorDefinition toColorDefinition(AppearanceDefinition appearanceDefinition)
   {
      ColorDefinition definition = new ColorDefinition();
      definition.setAlpha(1.0 - appearanceDefinition.getTransparency());

      if (appearanceDefinition instanceof YoAppearanceMaterial)
      {
         YoAppearanceMaterial yoAppearanceMaterial = (YoAppearanceMaterial) appearanceDefinition;
         definition.setRed(yoAppearanceMaterial.getDiffuseColor().getX());
         definition.setGreen(yoAppearanceMaterial.getDiffuseColor().getY());
         definition.setBlue(yoAppearanceMaterial.getDiffuseColor().getZ());
      }
      else if (appearanceDefinition instanceof YoAppearanceRGBColor)
      {
         YoAppearanceRGBColor yoAppearanceRGBColor = (YoAppearanceRGBColor) appearanceDefinition;
         definition.setRed(yoAppearanceRGBColor.getRed());
         definition.setGreen(yoAppearanceRGBColor.getGreen());
         definition.setBlue(yoAppearanceRGBColor.getBlue());
      }
      else
      {
         LogTools.error("Unsupported appearance: " + appearanceDefinition);
      }

      return definition;
   }

   public static YoTuple3DDefinition toYoTuple3DDefinition(YoVariable[] variables, int startIndex)
   {
      return toYoTuple3DDefinition(variables[startIndex++], variables[startIndex++], variables[startIndex]);
   }

   public static YoTuple3DDefinition toYoTuple3DDefinition(YoVariable x, YoVariable y, YoVariable z)
   {
      YoTuple3DDefinition position = new YoTuple3DDefinition();
      position.setX(x.getFullNameString());
      position.setY(y.getFullNameString());
      position.setZ(z.getFullNameString());
      return position;
   }

   public static YoTuple2DDefinition toYoTuple2DDefinition(YoVariable[] variables, int startIndex)
   {
      return toYoTuple2DDefinition(variables[startIndex++], variables[startIndex]);
   }

   public static YoTuple2DDefinition toYoTuple2DDefinition(YoVariable x, YoVariable y)
   {
      YoTuple2DDefinition position = new YoTuple2DDefinition();
      position.setX(x.getFullNameString());
      position.setY(y.getFullNameString());
      return position;
   }

   public static YoYawPitchRollDefinition toYoYawPitchRollDefinition(YoVariable[] variables, int startIndex)
   {
      return toYoYawPitchRollDefinition(variables[startIndex++], variables[startIndex++], variables[startIndex]);
   }

   public static YoYawPitchRollDefinition toYoYawPitchRollDefinition(YoVariable yaw, YoVariable pitch, YoVariable roll)
   {
      YoYawPitchRollDefinition position = new YoYawPitchRollDefinition();
      position.setYaw(yaw.getFullNameString());
      position.setPitch(pitch.getFullNameString());
      position.setRoll(roll.getFullNameString());
      return position;
   }

   public static YoQuaternionDefinition toYoQuaternionDefinition(YoVariable[] variables, int startIndex)
   {
      return toYoQuaternionDefinition(variables[startIndex++], variables[startIndex++], variables[startIndex++], variables[startIndex]);
   }

   public static YoQuaternionDefinition toYoQuaternionDefinition(YoVariable qx, YoVariable qy, YoVariable qz, YoVariable qs)
   {
      YoQuaternionDefinition position = new YoQuaternionDefinition();
      position.setX(qx.getFullNameString());
      position.setY(qy.getFullNameString());
      position.setZ(qz.getFullNameString());
      position.setS(qs.getFullNameString());
      return position;
   }
}
