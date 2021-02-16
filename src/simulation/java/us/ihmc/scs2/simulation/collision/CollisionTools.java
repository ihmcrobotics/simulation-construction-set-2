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
import us.ihmc.scs2.definition.geometry.Box3DDefinition;
import us.ihmc.scs2.definition.geometry.Capsule3DDefinition;
import us.ihmc.scs2.definition.geometry.Cone3DDefinition;
import us.ihmc.scs2.definition.geometry.Cylinder3DDefinition;
import us.ihmc.scs2.definition.geometry.Ellipsoid3DDefinition;
import us.ihmc.scs2.definition.geometry.GeometryDefinition;
import us.ihmc.scs2.definition.geometry.Point3DDefinition;
import us.ihmc.scs2.definition.geometry.Sphere3DDefinition;
import us.ihmc.scs2.definition.geometry.Torus3DDefinition;
import us.ihmc.scs2.definition.geometry.Wedge3DDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimRigidBodyBasics;

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
      if (definition instanceof Box3DDefinition)
         return toBox3D(originPose, (Box3DDefinition) definition);
      else if (definition instanceof Capsule3DDefinition)
         return toCapsule3D(originPose, (Capsule3DDefinition) definition);
      else if (definition instanceof Cone3DDefinition)
         return toConvexPolytope3D(originPose, (Cone3DDefinition) definition);
      else if (definition instanceof Cylinder3DDefinition)
         return toCylinder3D(originPose, (Cylinder3DDefinition) definition);
      else if (definition instanceof Ellipsoid3DDefinition)
         return toEllipsoid3D(originPose, (Ellipsoid3DDefinition) definition);
      else if (definition instanceof Point3DDefinition)
         return toPointShape3D(originPose, (Point3DDefinition) definition);
      else if (definition instanceof Sphere3DDefinition)
         return toSphere3D(originPose, (Sphere3DDefinition) definition);
      else if (definition instanceof Torus3DDefinition)
         return toTorus3D(originPose, (Torus3DDefinition) definition);
      else if (definition instanceof Wedge3DDefinition)
         return toRamp3D(originPose, (Wedge3DDefinition) definition);
      else
         throw new UnsupportedOperationException("Unhandled geometry type: " + definition.getClass().getSimpleName());
   }

   public static FrameShape3DReadOnly toFrameShape3D(RigidBodyTransformReadOnly originPose, ReferenceFrame referenceFrame, GeometryDefinition definition)
   {
      if (definition instanceof Box3DDefinition)
         return new FrameBox3D(referenceFrame, toBox3D(originPose, (Box3DDefinition) definition));
      else if (definition instanceof Capsule3DDefinition)
         return new FrameCapsule3D(referenceFrame, toCapsule3D(originPose, (Capsule3DDefinition) definition));
      else if (definition instanceof Cone3DDefinition)
         return new FrameConvexPolytope3D(referenceFrame, toConvexPolytope3D(originPose, (Cone3DDefinition) definition));
      else if (definition instanceof Cylinder3DDefinition)
         return new FrameCylinder3D(referenceFrame, toCylinder3D(originPose, (Cylinder3DDefinition) definition));
      else if (definition instanceof Ellipsoid3DDefinition)
         return new FrameEllipsoid3D(referenceFrame, toEllipsoid3D(originPose, (Ellipsoid3DDefinition) definition));
      else if (definition instanceof Point3DDefinition)
         return new FramePointShape3D(referenceFrame, toPointShape3D(originPose, (Point3DDefinition) definition));
      else if (definition instanceof Sphere3DDefinition)
         return new FrameSphere3D(referenceFrame, toSphere3D(originPose, (Sphere3DDefinition) definition));
      else if (definition instanceof Torus3DDefinition)
         throw new UnsupportedOperationException("Torus shape is not supported as collidable.");
      else if (definition instanceof Wedge3DDefinition)
         return new FrameRamp3D(referenceFrame, toRamp3D(originPose, (Wedge3DDefinition) definition));
      else
         throw new UnsupportedOperationException("Unhandled geometry type: " + definition.getClass().getSimpleName());
   }

   public static Box3D toBox3D(RigidBodyTransformReadOnly originPose, Box3DDefinition definition)
   {
      Box3D box3D = new Box3D();
      box3D.getSize().set(definition.getSizeX(), definition.getSizeY(), definition.getSizeZ());
      if (originPose != null)
         box3D.getPose().set(originPose);
      return box3D;
   }

   public static Capsule3D toCapsule3D(RigidBodyTransformReadOnly originPose, Capsule3DDefinition definition)
   {
      if (!definition.isRegular())
         throw new UnsupportedOperationException("Only regular capsules are supported.");
      Capsule3D capsule3D = new Capsule3D();
      capsule3D.setSize(definition.getLength(), definition.getRadiusX());
      if (originPose != null)
         capsule3D.applyTransform(originPose);
      return capsule3D;
   }

   public static ConvexPolytope3D toConvexPolytope3D(RigidBodyTransformReadOnly originPose, Cone3DDefinition definition)
   {
      ConvexPolytope3D cone3D = EuclidPolytopeFactories.newCone(definition.getHeight(), definition.getRadius(), CONE_NUMBER_OF_DIVISIONS);
      if (originPose != null)
         cone3D.applyTransform(originPose);
      return cone3D;
   }

   public static Cylinder3D toCylinder3D(RigidBodyTransformReadOnly originPose, Cylinder3DDefinition definition)
   {
      Cylinder3D cylinder3D = new Cylinder3D();
      cylinder3D.setSize(definition.getLength(), definition.getRadius());
      if (originPose != null)
         cylinder3D.applyTransform(originPose);
      return cylinder3D;
   }

   public static Ellipsoid3D toEllipsoid3D(RigidBodyTransformReadOnly originPose, Ellipsoid3DDefinition definition)
   {
      Ellipsoid3D ellipsoid3D = new Ellipsoid3D();
      ellipsoid3D.getRadii().set(definition.getRadiusX(), definition.getRadiusY(), definition.getRadiusZ());
      if (originPose != null)
         ellipsoid3D.getPose().set(originPose);
      return ellipsoid3D;
   }

   public static PointShape3D toPointShape3D(RigidBodyTransformReadOnly originPose, Point3DDefinition definition)
   {
      PointShape3D pointShape3D = new PointShape3D(definition.getPosition());
      if (originPose != null)
         pointShape3D.applyTransform(originPose);
      return pointShape3D;
   }

   public static Sphere3D toSphere3D(RigidBodyTransformReadOnly originPose, Sphere3DDefinition definition)
   {
      Sphere3D sphere3D = new Sphere3D();
      sphere3D.setRadius(definition.getRadius());
      if (originPose != null)
         sphere3D.applyTransform(originPose);
      return sphere3D;
   }

   public static Torus3D toTorus3D(RigidBodyTransformReadOnly originPose, Torus3DDefinition definition)
   {
      Torus3D torus3D = new Torus3D();
      double tubeRadius = 0.5 * (definition.getMajorRadius() - definition.getMinorRadius());
      double radius = definition.getMinorRadius() + tubeRadius;
      torus3D.setRadii(radius, tubeRadius);
      if (originPose != null)
         torus3D.applyTransform(originPose);
      return torus3D;
   }

   public static Ramp3D toRamp3D(RigidBodyTransformReadOnly originPose, Wedge3DDefinition definition)
   {
      Ramp3D ramp3D = new Ramp3D();
      ramp3D.getSize().set(definition.getSizeX(), definition.getSizeY(), definition.getSizeZ());
      if (originPose != null)
         ramp3D.getPose().set(originPose);
      return ramp3D;
   }
}
