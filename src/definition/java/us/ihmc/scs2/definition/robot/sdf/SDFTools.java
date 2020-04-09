package us.ihmc.scs2.definition.robot.sdf;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import us.ihmc.euclid.Axis3D;
import us.ihmc.euclid.matrix.Matrix3D;
import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.euclid.yawPitchRoll.YawPitchRoll;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.geometry.CylinderGeometryDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.ModelFileGeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.OneDoFJointDefinition;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.robot.sdf.items.SDFGeometry;
import us.ihmc.scs2.definition.robot.sdf.items.SDFInertia;
import us.ihmc.scs2.definition.robot.sdf.items.SDFJoint;
import us.ihmc.scs2.definition.robot.sdf.items.SDFJoint.SDFAxis;
import us.ihmc.scs2.definition.robot.sdf.items.SDFJoint.SDFAxis.SDFLimit;
import us.ihmc.scs2.definition.robot.sdf.items.SDFLink;
import us.ihmc.scs2.definition.robot.sdf.items.SDFLink.SDFInertial;
import us.ihmc.scs2.definition.robot.sdf.items.SDFModel;
import us.ihmc.scs2.definition.robot.sdf.items.SDFRoot;
import us.ihmc.scs2.definition.robot.sdf.items.SDFURIHolder;
import us.ihmc.scs2.definition.robot.sdf.items.SDFVisual;
import us.ihmc.scs2.definition.robot.sdf.items.SDFVisual.SDFMaterial;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition.MaterialDefinition;

public class SDFTools
{
   private static final double DEFAULT_MASS = 0.0;
   private static final double DEFAULT_IXX = 0.0;
   private static final double DEFAULT_IYY = 0.0;
   private static final double DEFAULT_IZZ = 0.0;
   private static final double DEFAULT_IXY = 0.0;
   private static final double DEFAULT_IXZ = 0.0;
   private static final double DEFAULT_IYZ = 0.0;

   private static final Vector3DReadOnly DEFAULT_AXIS = new Vector3D(1.0, 0.0, 0.0);
   private static final double DEFAULT_LOWER_LIMIT = Double.NEGATIVE_INFINITY;
   private static final double DEFAULT_UPPER_LIMIT = Double.POSITIVE_INFINITY;
   private static final double DEFAULT_EFFORT_LIMIT = Double.POSITIVE_INFINITY;
   private static final double DEFAULT_VELOCITY_LIMIT = Double.POSITIVE_INFINITY;

   public static SDFRoot loadSDFRoot(File sdfFile) throws JAXBException
   {
      return loadSDFRoot(sdfFile, Collections.emptyList());
   }

   public static SDFRoot loadSDFRoot(File sdfFile, Collection<String> resourceDirectories) throws JAXBException
   {
      Set<String> allResourceDirectories = new HashSet<>(resourceDirectories);
      File parentFile = sdfFile.getParentFile();

      if (parentFile != null)
      {
         allResourceDirectories.add(parentFile.getAbsolutePath() + File.separator);
         Stream.of(parentFile.listFiles(File::isDirectory)).map(file -> file.getAbsolutePath() + File.separator).forEach(allResourceDirectories::add);
      }

      JAXBContext context = JAXBContext.newInstance(SDFRoot.class);
      Unmarshaller um = context.createUnmarshaller();
      SDFRoot sdfRoot = (SDFRoot) um.unmarshal(sdfFile);

      resolvePaths(sdfRoot, allResourceDirectories);

      return sdfRoot;
   }

   public static SDFRoot loadSDFRoot(InputStream inputStream, Collection<String> resourceDirectories) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(SDFRoot.class);
      Unmarshaller um = context.createUnmarshaller();
      SDFRoot sdfRoot = (SDFRoot) um.unmarshal(inputStream);

      resolvePaths(sdfRoot, resourceDirectories);

      return sdfRoot;
   }

   public static void resolvePaths(SDFRoot sdfRoot, Collection<String> resourceDirectories)
   {
      List<SDFURIHolder> uriHolders = sdfRoot.getURIHolders();

      for (SDFURIHolder sdfURIHolder : uriHolders)
      {
         sdfURIHolder.setUri(tryToConvertToPath(sdfURIHolder.getUri(), resourceDirectories));
      }
   }

   public static String tryToConvertToPath(String filename, Collection<String> resourceDirectories)
   {
      try
      {
         URI uri = new URI(filename);

         String authority = uri.getAuthority() == null ? "" : uri.getAuthority();

         for (String resourceDirectory : resourceDirectories)
         {
            String fullname = resourceDirectory + authority + uri.getPath();
            // Path relative to class root
            if (SDFTools.class.getClassLoader().getResource(fullname) != null)
            {
               return fullname;
            }
            // Absolute path
            if (new File(fullname).exists())
            {
               return fullname;
            }
         }

         // Let's look in the parent directories of the resources if we can find a match to authority
         String resourceContainingAuthority = null;

         for (String resourceDirectory : resourceDirectories)
         {
            if (resourceDirectory.contains(authority))
            {
               resourceContainingAuthority = resourceDirectory;
               break;
            }
         }

         if (resourceContainingAuthority != null)
         {
            int lastIndexOf = resourceContainingAuthority.lastIndexOf(authority, resourceContainingAuthority.length());
            String newResource = resourceContainingAuthority.substring(0, lastIndexOf);

            if (!resourceDirectories.contains(newResource))
            {
               resourceDirectories.add(newResource);
               return tryToConvertToPath(filename, resourceDirectories);
            }
         }
      }
      catch (URISyntaxException e)
      {
         System.err.println("Malformed resource path in SDF file for path: " + filename);
      }

      return null;
   }

   public static RobotDefinition toRobotDefinition(SDFModel sdfModel)
   {
      List<SDFLink> sdfLinks = sdfModel.getLinks();
      List<SDFJoint> sdfJoints = sdfModel.getJoints();

      List<RigidBodyDefinition> rigidBodyDefinitions = sdfLinks.stream().map(SDFTools::toRigidBodyDefinition).collect(Collectors.toList());
      List<JointDefinition> jointDefinitions;
      if (sdfJoints == null)
         jointDefinitions = Collections.emptyList();
      else
         jointDefinitions = sdfJoints.stream().map(SDFTools::toJointDefinition).collect(Collectors.toList());
      RigidBodyDefinition rootBodyDefinition = connectKinematics(rigidBodyDefinitions, jointDefinitions, sdfJoints, sdfLinks);

      RobotDefinition robotDefinition = new RobotDefinition(sdfModel.getName());
      robotDefinition.setRootBodyDefinition(rootBodyDefinition);

      return robotDefinition;
   }

   public static RobotDefinition toFloatingRobotDefinition(SDFModel sdfModel)
   {
      return addFloatingJoint(toRobotDefinition(sdfModel), "");
   }

   public static RobotDefinition addFloatingJoint(RobotDefinition robotDefinition, String jointName)
   {
      RigidBodyDefinition elevatorDefinition = new RigidBodyDefinition("elevator");
      SixDoFJointDefinition rootJoint = new SixDoFJointDefinition(jointName);
      elevatorDefinition.addChildJoint(rootJoint);
      rootJoint.setSuccessor(robotDefinition.getRootBodyDefinition());
      robotDefinition.setRootBodyDefinition(elevatorDefinition);

      return robotDefinition;
   }

   public static RigidBodyDefinition toRigidBodyDefinition(SDFLink sdfLink)
   {
      RigidBodyDefinition definition = new RigidBodyDefinition(sdfLink.getName());

      SDFInertial sdfInertial = sdfLink.getInertial();

      if (sdfInertial == null)
      {
         definition.setMass(parseDouble(null, DEFAULT_MASS));
         definition.getMomentOfInertia().set(parseMomentOfInertia(null));
         definition.getInertiaPose().set(parsePose(null));
      }
      else
      {
         definition.setMass(parseDouble(sdfInertial.getMass(), DEFAULT_MASS));
         definition.getMomentOfInertia().set(parseMomentOfInertia(sdfInertial.getInertia()));
         definition.getInertiaPose().set(parsePose(sdfInertial.getPose()));
      }

      if (sdfLink.getVisuals() != null)
         sdfLink.getVisuals().stream().map(SDFTools::toVisualDefinition).forEach(definition::addVisualDefinition);

      return definition;
   }

   public static JointDefinition toJointDefinition(SDFJoint sdfJoint)
   {
      switch (sdfJoint.getType())
      {
         case "continuous":
            return toRevoluteJointDefinition(sdfJoint, true);
         case "revolute":
            return toRevoluteJointDefinition(sdfJoint, false);
         case "prismatic":
            return toPrismaticJointDefinition(sdfJoint);
         case "fixed":
            return toFixedJoint(sdfJoint);
         case "floating":
            return toSixDoFJointDefinition(sdfJoint);
         case "planar":
            return toPlanarJointDefinition(sdfJoint);
         default:
            throw new RuntimeException("Unexpected value for the joint type: " + sdfJoint.getType());
      }
   }

   public static RigidBodyDefinition connectKinematics(List<RigidBodyDefinition> rigidBodyDefinitions, List<JointDefinition> jointDefinitions,
                                                       List<SDFJoint> sdfJoints, List<SDFLink> sdfLinks)
   {
      Map<String, SDFLink> sdfLinkMap = sdfLinks.stream().collect(Collectors.toMap(SDFLink::getName, Function.identity()));
      Map<String, RigidBodyDefinition> rigidBodyDefinitionMap = rigidBodyDefinitions.stream().collect(Collectors.toMap(RigidBodyDefinition::getName,
                                                                                                                       Function.identity()));
      Map<String, JointDefinition> jointDefinitionMap = jointDefinitions.stream().collect(Collectors.toMap(JointDefinition::getName, Function.identity()));

      if (sdfJoints != null)
      {
         for (SDFJoint sdfJoint : sdfJoints)
         {
            String parent = sdfJoint.getParent();
            String child = sdfJoint.getChild();
            RigidBodyDefinition parentRigidBodyDefinition = rigidBodyDefinitionMap.get(parent);
            RigidBodyDefinition childRigidBodyDefinition = rigidBodyDefinitionMap.get(child);
            JointDefinition jointDefinition = jointDefinitionMap.get(sdfJoint.getName());
            jointDefinition.getTransformToParent().set(parsePose(sdfLinkMap.get(sdfJoint.getParent()).getPose()));

            jointDefinition.setSuccessor(childRigidBodyDefinition);
            parentRigidBodyDefinition.getChildrenJoints().add(jointDefinition);
         }
      }

      if (sdfJoints == null)
      {
         return rigidBodyDefinitions.get(0);
      }
      else
      {
         Map<String, SDFJoint> childToParentJoint = sdfJoints.stream().collect(Collectors.toMap(SDFJoint::getChild, Function.identity()));

         String rootLinkName = sdfJoints.get(0).getParent();
         SDFJoint parentJoint = childToParentJoint.get(rootLinkName);

         while (parentJoint != null)
         {
            rootLinkName = parentJoint.getParent();
            parentJoint = childToParentJoint.get(rootLinkName);
         }

         for (SDFJoint sdfJoint : sdfJoints)
         {
            String jointName = sdfJoint.getName();
            JointDefinition jointDefinition = jointDefinitionMap.get(jointName);

            String parentLinkName = sdfJoint.getParent();
            String childLinkName = sdfJoint.getChild();
            SDFJoint parentSDFJoint = childToParentJoint.get(parentLinkName);
            SDFLink parentSDFLink = sdfLinkMap.get(parentLinkName);
            SDFLink childSDFLink = sdfLinkMap.get(childLinkName);

            RigidBodyTransform parentLinkPose = parsePose(parentSDFLink.getPose());
            RigidBodyTransform childLinkPose = parsePose(childSDFLink.getPose());
            RigidBodyTransform parentJointParsedPose = parsePose(parentSDFJoint != null ? parentSDFJoint.getPose() : null);
            RigidBodyTransform jointParsedPose = parsePose(sdfJoint.getPose());

            RigidBodyTransform parentJointPose = new RigidBodyTransform(parentLinkPose);
            parentJointPose.multiply(parentJointParsedPose);

            RigidBodyTransform jointPose = new RigidBodyTransform(childLinkPose);
            jointPose.multiply(jointParsedPose);

            RigidBodyTransform transformToParentJoint = jointDefinition.getTransformToParent();
            transformToParentJoint.setAndInvert(parentJointPose);
            transformToParentJoint.multiply(jointPose);

            jointDefinition.getTransformToParent().getRotation().setToZero();
            parentLinkPose.transform(jointDefinition.getTransformToParent().getTranslation());

            RigidBodyDefinition childDefinition = rigidBodyDefinitionMap.get(childSDFLink.getName());
            RigidBodyTransform inertiaPose = childDefinition.getInertiaPose();
            Vector3DBasics comOffset = childDefinition.getInertiaPose().getTranslation();
            childLinkPose.transform(comOffset);
            inertiaPose.transform(childDefinition.getMomentOfInertia());
            childLinkPose.transform(childDefinition.getMomentOfInertia());
            inertiaPose.getRotation().setToZero();
            for (VisualDefinition visualDefinition : childDefinition.getVisualDefinitions())
            {
               RigidBodyTransform visualPose = visualDefinition.getOriginPose();
               childLinkPose.getRotation().transform(visualPose.getRotation());
               childLinkPose.getRotation().transform(visualPose.getTranslation());
            }
         }

         return rigidBodyDefinitionMap.get(rootLinkName);
      }
   }

   private static RevoluteJointDefinition toRevoluteJointDefinition(SDFJoint sdfJoint, boolean ignorePositionLimits)
   {
      RevoluteJointDefinition definition = new RevoluteJointDefinition(sdfJoint.getName());

      definition.getTransformToParent().set(parsePose(sdfJoint.getPose()));
      definition.getAxis().set(parseAxis(sdfJoint.getAxis()));
      parseLimit(sdfJoint.getAxis().getLimit(), definition, ignorePositionLimits);

      return definition;
   }

   private static PrismaticJointDefinition toPrismaticJointDefinition(SDFJoint sdfJoint)
   {
      PrismaticJointDefinition definition = new PrismaticJointDefinition(sdfJoint.getName());

      definition.getTransformToParent().set(parsePose(sdfJoint.getPose()));
      definition.getAxis().set(parseAxis(sdfJoint.getAxis()));
      parseLimit(sdfJoint.getAxis().getLimit(), definition, false);

      return definition;
   }

   private static FixedJointDefinition toFixedJoint(SDFJoint sdfJoint)
   {
      FixedJointDefinition definition = new FixedJointDefinition(sdfJoint.getName());

      RigidBodyTransform parseRigidBodyTransform = parsePose(sdfJoint.getPose());
      definition.getTransformToParent().set(parseRigidBodyTransform);

      return definition;
   }

   private static SixDoFJointDefinition toSixDoFJointDefinition(SDFJoint sdfJoint)
   {
      SixDoFJointDefinition definition = new SixDoFJointDefinition(sdfJoint.getName());

      definition.getTransformToParent().set(parsePose(sdfJoint.getPose()));

      return definition;
   }

   private static PlanarJointDefinition toPlanarJointDefinition(SDFJoint sdfJoint)
   {
      PlanarJointDefinition definition = new PlanarJointDefinition(sdfJoint.getName());

      definition.getTransformToParent().set(parsePose(sdfJoint.getPose()));

      Vector3D surfaceNormal = parseAxis(sdfJoint.getAxis());

      if (!surfaceNormal.geometricallyEquals(Axis3D.Y, 1.0e-5))
         throw new UnsupportedOperationException("Planar joint are supported only with a surface normal equal to: " + EuclidCoreIOTools.getTuple3DString(Axis3D.Y)
               + ", received:" + surfaceNormal);

      return definition;
   }

   public static VisualDefinition toVisualDefinition(SDFVisual sdfVisual)
   {
      if (sdfVisual == null)
         return null;

      VisualDefinition visualDefinition = new VisualDefinition();
      visualDefinition.setName(sdfVisual.getName());
      visualDefinition.setOriginPose(parsePose(sdfVisual.getPose()));
      visualDefinition.setMaterialDefinition(toMaterialDefinition(sdfVisual.getMaterial()));
      visualDefinition.setGeometryDefinition(toGeometryDefinition(sdfVisual.getGeometry()));
      return visualDefinition;
   }

   public static GeometryDefinition toGeometryDefinition(SDFGeometry sdfGeometry)
   {
      return toGeometryDefinition(sdfGeometry, Collections.emptyList());
   }

   public static GeometryDefinition toGeometryDefinition(SDFGeometry sdfGeometry, List<String> resourceDirectories)
   {
      if (sdfGeometry.getBox() != null)
      {
         BoxGeometryDefinition boxGeometryDefinition = new BoxGeometryDefinition();
         boxGeometryDefinition.getSize().set(parseVector3D(sdfGeometry.getBox().getSize(), null));
         return boxGeometryDefinition;
      }
      if (sdfGeometry.getCylinder() != null)
      {
         CylinderGeometryDefinition cylinderGeometryDefinition = new CylinderGeometryDefinition();
         cylinderGeometryDefinition.setRadius(parseDouble(sdfGeometry.getCylinder().getRadius(), 0.0));
         cylinderGeometryDefinition.setLength(parseDouble(sdfGeometry.getCylinder().getLength(), 0.0));
         return cylinderGeometryDefinition;
      }
      if (sdfGeometry.getSphere() != null)
      {
         SphereGeometryDefinition sphereGeometryDefinition = new SphereGeometryDefinition();
         sphereGeometryDefinition.setRadius(parseDouble(sdfGeometry.getSphere().getRadius(), 0.0));
         return sphereGeometryDefinition;
      }
      if (sdfGeometry.getMesh() != null)
      {
         ModelFileGeometryDefinition modelFileGeometryDefinition = new ModelFileGeometryDefinition();
         modelFileGeometryDefinition.getResourceDirectories().addAll(resourceDirectories);
         modelFileGeometryDefinition.setFileName(sdfGeometry.getMesh().getUri());
         modelFileGeometryDefinition.setScale(parseVector3D(sdfGeometry.getMesh().getScale(), new Vector3D(1, 1, 1)));
         return modelFileGeometryDefinition;
      }
      throw new IllegalArgumentException("The given SDF Geometry is empty.");
   }

   public static MaterialDefinition toMaterialDefinition(SDFMaterial sdfMaterial)
   {
      if (sdfMaterial == null)
         return null;

      MaterialDefinition materialDefinition = new MaterialDefinition();
      materialDefinition.setLighting(parseDouble(sdfMaterial.getLighting(), Double.NaN));
      materialDefinition.setAmbientcolorDefinition(toColorDefinition(sdfMaterial.getAmbient()));
      materialDefinition.setDiffuseColorDefinition(toColorDefinition(sdfMaterial.getDiffuse()));
      materialDefinition.setSpecularColorDefinition(toColorDefinition(sdfMaterial.getSpecular()));
      materialDefinition.setEmissiveColorDefinition(toColorDefinition(sdfMaterial.getEmissive()));
      // TODO handle the script
      return materialDefinition;
   }

   public static ColorDefinition toColorDefinition(String sdfColor)
   {
      if (sdfColor == null)
         return null;

      return new ColorDefinition(parseArray(sdfColor, null));
   }

   public static RigidBodyTransform parsePose(String pose)
   {
      RigidBodyTransform rigidBodyTransform = new RigidBodyTransform();

      if (pose != null)
      {
         String[] split = pose.split("\\s+");
         Vector3D position = new Vector3D(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
         YawPitchRoll orientation = new YawPitchRoll(Double.parseDouble(split[5]), Double.parseDouble(split[4]), Double.parseDouble(split[3]));
         rigidBodyTransform.set(orientation, position);
      }
      return rigidBodyTransform;
   }

   public static Matrix3D parseMomentOfInertia(SDFInertia inertia)
   {
      if (inertia == null)
         inertia = new SDFInertia();

      Matrix3D momentOfInertia = new Matrix3D();

      double ixx = parseDouble(inertia.getIxx(), DEFAULT_IXX);
      double iyy = parseDouble(inertia.getIyy(), DEFAULT_IYY);
      double izz = parseDouble(inertia.getIzz(), DEFAULT_IZZ);

      double ixy = parseDouble(inertia.getIxy(), DEFAULT_IXY);
      double ixz = parseDouble(inertia.getIxz(), DEFAULT_IXZ);
      double iyz = parseDouble(inertia.getIyz(), DEFAULT_IYZ);

      momentOfInertia.setM00(ixx);
      momentOfInertia.setM11(iyy);
      momentOfInertia.setM22(izz);

      momentOfInertia.setM01(ixy);
      momentOfInertia.setM02(ixz);
      momentOfInertia.setM12(iyz);

      momentOfInertia.setM10(ixy);
      momentOfInertia.setM20(ixz);
      momentOfInertia.setM21(iyz);

      return momentOfInertia;
   }

   public static void parseLimit(SDFLimit sdfLimit, OneDoFJointDefinition jointDefinitionToParseLimitInto, boolean ignorePositionLimits)
   {
      double lowerLimit, upperLimit, effortLimit, velocityLimit;

      if (sdfLimit != null)
      {
         if (ignorePositionLimits)
         {
            lowerLimit = DEFAULT_LOWER_LIMIT;
            upperLimit = DEFAULT_UPPER_LIMIT;
         }
         else
         {
            lowerLimit = parseDouble(sdfLimit.getLower(), DEFAULT_LOWER_LIMIT);
            upperLimit = parseDouble(sdfLimit.getUpper(), DEFAULT_UPPER_LIMIT);
         }
         effortLimit = parseDouble(sdfLimit.getEffort(), DEFAULT_EFFORT_LIMIT);
         velocityLimit = parseDouble(sdfLimit.getVelocity(), DEFAULT_VELOCITY_LIMIT);
      }
      else
      {
         lowerLimit = DEFAULT_LOWER_LIMIT;
         upperLimit = DEFAULT_UPPER_LIMIT;
         effortLimit = DEFAULT_EFFORT_LIMIT;
         velocityLimit = DEFAULT_VELOCITY_LIMIT;
      }

      jointDefinitionToParseLimitInto.setPositionLimits(lowerLimit, upperLimit);
      jointDefinitionToParseLimitInto.setEffortLimits(effortLimit);
      jointDefinitionToParseLimitInto.setVelocityLimits(velocityLimit);
   }

   public static Vector3D parseAxis(SDFAxis axis)
   {
      if (axis == null)
         return new Vector3D(DEFAULT_AXIS);
      Vector3D parsedAxis = parseVector3D(axis.getXYZ(), new Vector3D(DEFAULT_AXIS));
      parsedAxis.normalize();
      return parsedAxis;
   }

   public static double parseDouble(String value, double defaultValue)
   {
      if (value == null)
         return defaultValue;
      return Double.parseDouble(value);
   }

   public static Vector3D parseVector3D(String value, Vector3D defaultValue)
   {
      if (value == null)
         return defaultValue;

      String[] split = value.split("\\s+");
      Vector3D vector = new Vector3D();
      vector.setX(Double.parseDouble(split[0]));
      vector.setY(Double.parseDouble(split[1]));
      vector.setZ(Double.parseDouble(split[2]));
      return vector;
   }

   public static double[] parseArray(String value, double[] defaultValue)
   {
      if (value == null)
         return defaultValue;

      String[] split = value.split("\\s+");
      double[] array = new double[split.length];

      for (int i = 0; i < split.length; i++)
         array[i] = Double.parseDouble(split[i]);

      return array;
   }
}
