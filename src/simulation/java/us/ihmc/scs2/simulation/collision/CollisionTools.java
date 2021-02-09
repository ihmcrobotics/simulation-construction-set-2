package us.ihmc.scs2.simulation.collision;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.FrameBox3D;
import us.ihmc.euclid.referenceFrame.FrameCapsule3D;
import us.ihmc.euclid.referenceFrame.FrameCylinder3D;
import us.ihmc.euclid.referenceFrame.FrameEllipsoid3D;
import us.ihmc.euclid.referenceFrame.FramePointShape3D;
import us.ihmc.euclid.referenceFrame.FrameRamp3D;
import us.ihmc.euclid.referenceFrame.FrameSphere3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FrameShape3DReadOnly;
import us.ihmc.euclid.referenceFrame.polytope.FrameConvexPolytope3D;
import us.ihmc.euclid.shape.convexPolytope.ConvexPolytope3D;
import us.ihmc.euclid.shape.convexPolytope.tools.EuclidPolytopeFactories;
import us.ihmc.euclid.shape.primitives.Box3D;
import us.ihmc.euclid.shape.primitives.Capsule3D;
import us.ihmc.euclid.shape.primitives.Cylinder3D;
import us.ihmc.euclid.shape.primitives.Ellipsoid3D;
import us.ihmc.euclid.shape.primitives.PointShape3D;
import us.ihmc.euclid.shape.primitives.Ramp3D;
import us.ihmc.euclid.shape.primitives.Sphere3D;
import us.ihmc.euclid.shape.primitives.Torus3D;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.scs2.definition.geometry.BoxGeometryDefinition;
import us.ihmc.scs2.definition.geometry.CapsuleGeometryDefinition;
import us.ihmc.scs2.definition.geometry.ConeGeometryDefinition;
import us.ihmc.scs2.definition.geometry.CylinderGeometryDefinition;
import us.ihmc.scs2.definition.geometry.EllipsoidGeometryDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.PointGeometryDefinition;
import us.ihmc.scs2.definition.geometry.SphereGeometryDefinition;
import us.ihmc.scs2.definition.geometry.TorusGeometryDefinition;
import us.ihmc.scs2.definition.geometry.WedgeGeometryDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRigidBodyBasics;

public class CollisionTools
{
   private static final int CONE_NUMBER_OF_DIVISIONS = 64;

   public static List<Collidable> extractCollidableRigidBodies(RobotDefinition robotDefinition, SimRigidBodyBasics rootBody)
   {
      return rootBody.subtreeStream()
                     .flatMap(rigidBody -> toCollidableRigidBody(robotDefinition.getRigidBodyDefinition(rigidBody.getName()), rigidBody).stream())
                     .collect(Collectors.toList());
   }

   public static List<Collidable> toCollidableRigidBody(RigidBodyDefinition definition, SimRigidBodyBasics rigidBodyInstance)
   {
      return definition.getCollisionShapeDefinitions().stream()
                       .map(collisionShapeDefinition -> toFrameShape3D(collisionShapeDefinition.getOriginPose(),
                                                                       rigidBodyInstance.isRootBody() ? rigidBodyInstance.getBodyFixedFrame()
                                                                             : rigidBodyInstance.getParentJoint().getFrameAfterJoint(),
                                                                       collisionShapeDefinition.getGeometryDefinition()))
                       .map(shape -> toCollidable(rigidBodyInstance, shape)).collect(Collectors.toList());
   }

   private static Collidable toCollidable(SimRigidBodyBasics rigidBody, FrameShape3DReadOnly shape)
   {
      return new Collidable(rigidBody, -1, -1, shape);
   }

   public static List<Collidable> toCollisionShape(TerrainObjectDefinition definition, ReferenceFrame worldFrame)
   {
      return definition.getCollisionShapeDefinitions().stream()
                       .map(collisionShapeDefinition -> toFrameShape3D(collisionShapeDefinition.getOriginPose(),
                                                                       worldFrame,
                                                                       collisionShapeDefinition.getGeometryDefinition()))
                       .map(shape -> new Collidable(null, -1, -1, shape)).collect(Collectors.toList());
   }

   public static Shape3DReadOnly toShape3D(RigidBodyTransformReadOnly originPose, GeometryDefinition definition)
   {
      if (definition instanceof BoxGeometryDefinition)
         return toBox3D(originPose, (BoxGeometryDefinition) definition);
      else if (definition instanceof CapsuleGeometryDefinition)
         return toCapsule3D(originPose, (CapsuleGeometryDefinition) definition);
      else if (definition instanceof ConeGeometryDefinition)
         return toConvexPolytope3D(originPose, (ConeGeometryDefinition) definition);
      else if (definition instanceof CylinderGeometryDefinition)
         return toCylinder3D(originPose, (CylinderGeometryDefinition) definition);
      else if (definition instanceof EllipsoidGeometryDefinition)
         return toEllipsoid3D(originPose, (EllipsoidGeometryDefinition) definition);
      else if (definition instanceof PointGeometryDefinition)
         return toPointShape3D(originPose, (PointGeometryDefinition) definition);
      else if (definition instanceof SphereGeometryDefinition)
         return toSphere3D(originPose, (SphereGeometryDefinition) definition);
      else if (definition instanceof TorusGeometryDefinition)
         return toTorus3D(originPose, (TorusGeometryDefinition) definition);
      else if (definition instanceof WedgeGeometryDefinition)
         return toRamp3D(originPose, (WedgeGeometryDefinition) definition);
      else
         throw new UnsupportedOperationException("Unhandled geometry type: " + definition.getClass().getSimpleName());
   }

   public static FrameShape3DReadOnly toFrameShape3D(RigidBodyTransformReadOnly originPose, ReferenceFrame referenceFrame, GeometryDefinition definition)
   {
      if (definition instanceof BoxGeometryDefinition)
         return new FrameBox3D(referenceFrame, toBox3D(originPose, (BoxGeometryDefinition) definition));
      else if (definition instanceof CapsuleGeometryDefinition)
         return new FrameCapsule3D(referenceFrame, toCapsule3D(originPose, (CapsuleGeometryDefinition) definition));
      else if (definition instanceof ConeGeometryDefinition)
         return new FrameConvexPolytope3D(referenceFrame, toConvexPolytope3D(originPose, (ConeGeometryDefinition) definition));
      else if (definition instanceof CylinderGeometryDefinition)
         return new FrameCylinder3D(referenceFrame, toCylinder3D(originPose, (CylinderGeometryDefinition) definition));
      else if (definition instanceof EllipsoidGeometryDefinition)
         return new FrameEllipsoid3D(referenceFrame, toEllipsoid3D(originPose, (EllipsoidGeometryDefinition) definition));
      else if (definition instanceof PointGeometryDefinition)
         return new FramePointShape3D(referenceFrame, toPointShape3D(originPose, (PointGeometryDefinition) definition));
      else if (definition instanceof SphereGeometryDefinition)
         return new FrameSphere3D(referenceFrame, toSphere3D(originPose, (SphereGeometryDefinition) definition));
      else if (definition instanceof TorusGeometryDefinition)
         throw new UnsupportedOperationException("Torus shape is not supported as collidable.");
      else if (definition instanceof WedgeGeometryDefinition)
         return new FrameRamp3D(referenceFrame, toRamp3D(originPose, (WedgeGeometryDefinition) definition));
      else
         throw new UnsupportedOperationException("Unhandled geometry type: " + definition.getClass().getSimpleName());
   }

   public static Box3D toBox3D(RigidBodyTransformReadOnly originPose, BoxGeometryDefinition definition)
   {
      Box3D box3D = new Box3D();
      box3D.getSize().set(definition.getSize());
      if (originPose != null)
         box3D.getPose().set(originPose);
      return box3D;
   }

   public static Capsule3D toCapsule3D(RigidBodyTransformReadOnly originPose, CapsuleGeometryDefinition definition)
   {
      Capsule3D capsule3D = new Capsule3D();
      capsule3D.setSize(definition.getLength(), definition.getRadius());
      if (originPose != null)
         capsule3D.applyTransform(originPose);
      return capsule3D;
   }

   public static ConvexPolytope3D toConvexPolytope3D(RigidBodyTransformReadOnly originPose, ConeGeometryDefinition definition)
   {
      ConvexPolytope3D cone3D = EuclidPolytopeFactories.newCone(definition.getHeight(), definition.getRadius(), CONE_NUMBER_OF_DIVISIONS);
      if (originPose != null)
         cone3D.applyTransform(originPose);
      return cone3D;
   }

   public static Cylinder3D toCylinder3D(RigidBodyTransformReadOnly originPose, CylinderGeometryDefinition definition)
   {
      Cylinder3D cylinder3D = new Cylinder3D();
      cylinder3D.setSize(definition.getLength(), definition.getRadius());
      if (originPose != null)
         cylinder3D.applyTransform(originPose);
      return cylinder3D;
   }

   public static Ellipsoid3D toEllipsoid3D(RigidBodyTransformReadOnly originPose, EllipsoidGeometryDefinition definition)
   {
      Ellipsoid3D ellipsoid3D = new Ellipsoid3D();
      ellipsoid3D.getRadii().set(definition.getRadii());
      if (originPose != null)
         ellipsoid3D.getPose().set(originPose);
      return ellipsoid3D;
   }

   public static PointShape3D toPointShape3D(RigidBodyTransformReadOnly originPose, PointGeometryDefinition definition)
   {
      PointShape3D pointShape3D = new PointShape3D(definition.getPosition());
      if (originPose != null)
         pointShape3D.applyTransform(originPose);
      return pointShape3D;
   }

   public static Sphere3D toSphere3D(RigidBodyTransformReadOnly originPose, SphereGeometryDefinition definition)
   {
      Sphere3D sphere3D = new Sphere3D();
      sphere3D.setRadius(definition.getRadius());
      if (originPose != null)
         sphere3D.applyTransform(originPose);
      return sphere3D;
   }

   public static Torus3D toTorus3D(RigidBodyTransformReadOnly originPose, TorusGeometryDefinition definition)
   {
      Torus3D torus3D = new Torus3D();
      double tubeRadius = 0.5 * (definition.getMajorRadius() - definition.getMinorRadius());
      double radius = definition.getMinorRadius() + tubeRadius;
      torus3D.setRadii(radius, tubeRadius);
      if (originPose != null)
         torus3D.applyTransform(originPose);
      return torus3D;
   }

   public static Ramp3D toRamp3D(RigidBodyTransformReadOnly originPose, WedgeGeometryDefinition definition)
   {
      Ramp3D ramp3D = new Ramp3D();
      ramp3D.getSize().set(definition.getSize());
      if (originPose != null)
         ramp3D.getPose().set(originPose);
      return ramp3D;
   }
}
