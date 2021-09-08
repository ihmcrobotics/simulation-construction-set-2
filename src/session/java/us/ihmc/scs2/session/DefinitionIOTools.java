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

public class DefinitionIOTools
{
   private static final JAXBContext definitionContext;

   static
   {
      try
      {
         List<Class<?>> classesToBeBound = new ArrayList<>();
         classesToBeBound.add(CollisionShapeDefinition.class);

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

         classesToBeBound.add(SensorDefinition.class);
         classesToBeBound.add(CameraSensorDefinition.class);
         classesToBeBound.add(IMUSensorDefinition.class);
         classesToBeBound.add(LidarSensorDefinition.class);
         classesToBeBound.add(WrenchSensorDefinition.class);

         classesToBeBound.add(KinematicPointDefinition.class);
         classesToBeBound.add(ExternalWrenchPointDefinition.class);
         classesToBeBound.add(GroundContactPointDefinition.class);

         classesToBeBound.add(JointDefinition.class);
         classesToBeBound.add(FixedJointDefinition.class);
         classesToBeBound.add(OneDoFJointDefinition.class);
         classesToBeBound.add(PrismaticJointDefinition.class);
         classesToBeBound.add(RevoluteJointDefinition.class);
         classesToBeBound.add(PlanarJointDefinition.class);
         classesToBeBound.add(SixDoFJointDefinition.class);
         classesToBeBound.add(SphericalJointDefinition.class);

         classesToBeBound.add(RigidBodyDefinition.class);

         classesToBeBound.add(RobotDefinition.class);

         classesToBeBound.add(JointState.class);
         classesToBeBound.add(OneDoFJointState.class);
         classesToBeBound.add(SixDoFJointState.class);
         classesToBeBound.add(SphericalJointState.class);

         classesToBeBound.add(TerrainObjectDefinition.class);

         classesToBeBound.add(ColorDefinition.class);
         classesToBeBound.add(MaterialDefinition.class);
         classesToBeBound.add(MaterialScriptDefinition.class);
         classesToBeBound.add(TextureDefinition.class);
         classesToBeBound.add(VisualDefinition.class);

         definitionContext = JAXBContext.newInstance(classesToBeBound.toArray(new Class[classesToBeBound.size()]));
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
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
