package us.ihmc.scs2.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import us.ihmc.scs2.definition.SessionInformationDefinition;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.geometry.ArcTorus3DDefinition;
import us.ihmc.scs2.definition.geometry.Box3DDefinition;
import us.ihmc.scs2.definition.geometry.Capsule3DDefinition;
import us.ihmc.scs2.definition.geometry.Cone3DDefinition;
import us.ihmc.scs2.definition.geometry.Cylinder3DDefinition;
import us.ihmc.scs2.definition.geometry.Ellipsoid3DDefinition;
import us.ihmc.scs2.definition.geometry.ExtrudedPolygon2DDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.HemiEllipsoid3DDefinition;
import us.ihmc.scs2.definition.geometry.ModelFileGeometryDefinition;
import us.ihmc.scs2.definition.geometry.Point3DDefinition;
import us.ihmc.scs2.definition.geometry.Polygon2DDefinition;
import us.ihmc.scs2.definition.geometry.Polygon3DDefinition;
import us.ihmc.scs2.definition.geometry.PyramidBox3DDefinition;
import us.ihmc.scs2.definition.geometry.Ramp3DDefinition;
import us.ihmc.scs2.definition.geometry.STPBox3DDefinition;
import us.ihmc.scs2.definition.geometry.STPCapsule3DDefinition;
import us.ihmc.scs2.definition.geometry.STPCylinder3DDefinition;
import us.ihmc.scs2.definition.geometry.STPRamp3DDefinition;
import us.ihmc.scs2.definition.geometry.Sphere3DDefinition;
import us.ihmc.scs2.definition.geometry.Tetrahedron3DDefinition;
import us.ihmc.scs2.definition.geometry.Torus3DDefinition;
import us.ihmc.scs2.definition.geometry.TriangleMesh3DDefinition;
import us.ihmc.scs2.definition.geometry.TruncatedCone3DDefinition;
import us.ihmc.scs2.definition.robot.CameraSensorDefinition;
import us.ihmc.scs2.definition.robot.ExternalWrenchPointDefinition;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.scs2.definition.robot.GroundContactPointDefinition;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.KinematicPointDefinition;
import us.ihmc.scs2.definition.robot.LidarSensorDefinition;
import us.ihmc.scs2.definition.robot.OneDoFJointDefinition;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SensorDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.robot.SphericalJointDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.scs2.definition.state.JointState;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.definition.state.SphericalJointState;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.visual.ColorDefinition;
import us.ihmc.scs2.definition.visual.MaterialDefinition;
import us.ihmc.scs2.definition.visual.MaterialScriptDefinition;
import us.ihmc.scs2.definition.visual.TextureDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositeDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoQuaternionDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple2DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoYawPitchRollDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphic2DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphic3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicArrow3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicBox3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCapsule3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCone3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCoordinateSystem3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicCylinder3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicEllipsoid3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicGroupDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicLine2DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicListDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPoint2DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPoint3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPointcloud2DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPointcloud3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolygon2DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolygonExtruded3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicPolynomial3DDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicSTPBox3DDefinition;

public class DefinitionIOTools
{
   private static final JAXBContext definitionContext;

   static
   {
      try
      {
         List<Class<?>> classesToBeBound = new ArrayList<>();
         classesToBeBound.add(CollisionShapeDefinition.class);
         classesToBeBound.add(TerrainObjectDefinition.class);

         // GeometryDefinitions
         classesToBeBound.add(GeometryDefinition.class);
         classesToBeBound.add(ArcTorus3DDefinition.class);
         classesToBeBound.add(Box3DDefinition.class);
         classesToBeBound.add(Capsule3DDefinition.class);
         classesToBeBound.add(Cone3DDefinition.class);
         //         classesToBeBound.add(ConvexPolytope3DDefinition.class); TODO Figure this one out
         classesToBeBound.add(Cylinder3DDefinition.class);
         classesToBeBound.add(Ellipsoid3DDefinition.class);
         classesToBeBound.add(ExtrudedPolygon2DDefinition.class);
         //         classesToBeBound.add(ExtrusionDefinition.class); TODO Figure this one out
         classesToBeBound.add(HemiEllipsoid3DDefinition.class);
         classesToBeBound.add(ModelFileGeometryDefinition.class);
         classesToBeBound.add(Point3DDefinition.class);
         classesToBeBound.add(Polygon2DDefinition.class);
         classesToBeBound.add(Polygon3DDefinition.class);
         classesToBeBound.add(PyramidBox3DDefinition.class);
         classesToBeBound.add(Ramp3DDefinition.class);
         classesToBeBound.add(Sphere3DDefinition.class);
         classesToBeBound.add(STPBox3DDefinition.class);
         classesToBeBound.add(STPCapsule3DDefinition.class);
         //         classesToBeBound.add(STPConvexPolytope3DDefinition.class); TODO Figure this one out
         classesToBeBound.add(STPCylinder3DDefinition.class);
         classesToBeBound.add(STPRamp3DDefinition.class);
         classesToBeBound.add(Tetrahedron3DDefinition.class);
         classesToBeBound.add(Torus3DDefinition.class);
         classesToBeBound.add(TriangleMesh3DDefinition.class);
         classesToBeBound.add(TruncatedCone3DDefinition.class);

         // SensorDefinitions
         classesToBeBound.add(SensorDefinition.class);
         classesToBeBound.add(CameraSensorDefinition.class);
         classesToBeBound.add(IMUSensorDefinition.class);
         classesToBeBound.add(LidarSensorDefinition.class);
         classesToBeBound.add(WrenchSensorDefinition.class);

         // Key robot points
         classesToBeBound.add(KinematicPointDefinition.class);
         classesToBeBound.add(ExternalWrenchPointDefinition.class);
         classesToBeBound.add(GroundContactPointDefinition.class);

         // RobotDefinition
         classesToBeBound.add(RobotDefinition.class);
         classesToBeBound.add(RigidBodyDefinition.class);
         classesToBeBound.add(JointDefinition.class);
         classesToBeBound.add(FixedJointDefinition.class);
         classesToBeBound.add(OneDoFJointDefinition.class);
         classesToBeBound.add(PrismaticJointDefinition.class);
         classesToBeBound.add(RevoluteJointDefinition.class);
         classesToBeBound.add(PlanarJointDefinition.class);
         classesToBeBound.add(SixDoFJointDefinition.class);
         classesToBeBound.add(SphericalJointDefinition.class);

         // JointState
         classesToBeBound.add(JointState.class);
         classesToBeBound.add(OneDoFJointState.class);
         classesToBeBound.add(SixDoFJointState.class);
         classesToBeBound.add(SphericalJointState.class);

         // Visuals
         classesToBeBound.add(ColorDefinition.class);
         classesToBeBound.add(MaterialDefinition.class);
         classesToBeBound.add(MaterialScriptDefinition.class);
         classesToBeBound.add(TextureDefinition.class);
         classesToBeBound.add(VisualDefinition.class);

         // YoGraphicDefinition
         classesToBeBound.add(YoGraphicListDefinition.class);
         classesToBeBound.add(YoGraphicDefinition.class);
         classesToBeBound.add(YoGraphicGroupDefinition.class);
         classesToBeBound.add(YoGraphic2DDefinition.class);
         classesToBeBound.add(YoGraphicLine2DDefinition.class);
         classesToBeBound.add(YoGraphicPoint2DDefinition.class);
         classesToBeBound.add(YoGraphicPointcloud2DDefinition.class);
         classesToBeBound.add(YoGraphicPolygon2DDefinition.class);
         classesToBeBound.add(YoGraphic3DDefinition.class);
         classesToBeBound.add(YoGraphicArrow3DDefinition.class);
         classesToBeBound.add(YoGraphicBox3DDefinition.class);
         classesToBeBound.add(YoGraphicSTPBox3DDefinition.class);
         classesToBeBound.add(YoGraphicCapsule3DDefinition.class);
         classesToBeBound.add(YoGraphicCone3DDefinition.class);
         classesToBeBound.add(YoGraphicCoordinateSystem3DDefinition.class);
         classesToBeBound.add(YoGraphicCylinder3DDefinition.class);
         classesToBeBound.add(YoGraphicEllipsoid3DDefinition.class);
         classesToBeBound.add(YoGraphicPoint3DDefinition.class);
         classesToBeBound.add(YoGraphicPointcloud3DDefinition.class);
         classesToBeBound.add(YoGraphicPolygonExtruded3DDefinition.class);
         classesToBeBound.add(YoGraphicPolynomial3DDefinition.class);

         // YoCompositeDefinition
         classesToBeBound.add(YoCompositeDefinition.class);
         classesToBeBound.add(YoTuple2DDefinition.class);
         classesToBeBound.add(YoTuple3DDefinition.class);
         classesToBeBound.add(YoOrientation3DDefinition.class);
         classesToBeBound.add(YoQuaternionDefinition.class);
         classesToBeBound.add(YoYawPitchRollDefinition.class);

         classesToBeBound.add(SessionInformationDefinition.class);

         definitionContext = JAXBContext.newInstance(classesToBeBound.toArray(new Class[classesToBeBound.size()]));
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static void loadResources()
   {
      // Only need to load this class to get the resources loaded.
   }

   public static YoGraphicListDefinition loadYoGraphicListDefinition(InputStream inputStream) throws JAXBException, IOException
   {
      try
      {
         Unmarshaller unmarshaller = definitionContext.createUnmarshaller();
         return (YoGraphicListDefinition) unmarshaller.unmarshal(inputStream);
      }
      finally
      {
         inputStream.close();
      }
   }

   public static void saveYoGraphicListDefinition(OutputStream outputStream, YoGraphicListDefinition definition) throws JAXBException, IOException
   {
      try
      {
         Marshaller marshaller = definitionContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(definition, outputStream);
      }
      finally
      {
         outputStream.close();
      }
   }

   public static RobotDefinition loadRobotDefinition(InputStream inputStream) throws JAXBException, IOException
   {
      try
      {
         Unmarshaller unmarshaller = definitionContext.createUnmarshaller();
         RobotDefinition loadedRobot = (RobotDefinition) unmarshaller.unmarshal(inputStream);
         connectKinematicsRecursive(loadedRobot.getRootBodyDefinition());
         return loadedRobot;
      }
      finally
      {
         inputStream.close();
      }
   }

   public static void saveRobotDefinition(OutputStream outputStream, RobotDefinition definition) throws JAXBException, IOException
   {
      try
      {
         Marshaller marshaller = definitionContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(definition, outputStream);
      }
      finally
      {
         outputStream.close();
      }
   }

   public static TerrainObjectDefinition loadTerrainObjectDefinition(InputStream inputStream) throws JAXBException, IOException
   {
      try
      {
         Unmarshaller unmarshaller = definitionContext.createUnmarshaller();
         return (TerrainObjectDefinition) unmarshaller.unmarshal(inputStream);
      }
      finally
      {
         inputStream.close();
      }
   }

   public static void saveTerrainObjectDefinition(OutputStream outputStream, TerrainObjectDefinition definition) throws JAXBException, IOException
   {
      try
      {
         Marshaller marshaller = definitionContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(definition, outputStream);
      }
      finally
      {
         outputStream.close();
      }
   }

   public static SessionInformationDefinition loadSessionInformationDefinition(InputStream inputStream) throws JAXBException, IOException
   {
      try
      {
         Unmarshaller unmarshaller = definitionContext.createUnmarshaller();
         return (SessionInformationDefinition) unmarshaller.unmarshal(inputStream);
      }
      finally
      {
         inputStream.close();
      }
   }

   public static void saveSessionInformationDefinition(OutputStream outputStream, SessionInformationDefinition definition) throws JAXBException, IOException
   {
      try
      {
         Marshaller marshaller = definitionContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(definition, outputStream);
      }
      finally
      {
         outputStream.close();
      }
   }

   private static void connectKinematicsRecursive(RigidBodyDefinition currentBody)
   {
      if (currentBody.getChildrenJoints() == null)
         return;

      for (JointDefinition childJoint : currentBody.getChildrenJoints())
      {
         childJoint.setPredecessor(currentBody);
         if (childJoint.getSuccessor() == null)
            continue;
         childJoint.getSuccessor().setParentJoint(childJoint);
         connectKinematicsRecursive(childJoint.getSuccessor());
      }
   }
}
