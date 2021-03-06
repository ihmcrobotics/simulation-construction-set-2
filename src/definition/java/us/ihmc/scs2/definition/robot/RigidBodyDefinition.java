package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import us.ihmc.euclid.interfaces.Transformable;
import us.ihmc.euclid.matrix.Matrix3D;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.transform.interfaces.Transform;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.mecano.multiBodySystem.RigidBody;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

public class RigidBodyDefinition implements Transformable
{
   private String name;
   private double mass;
   private final Matrix3D momentOfInertia = new Matrix3D();
   private final RigidBodyTransform inertiaPose = new RigidBodyTransform();

   private JointDefinition parentJoint;
   private final List<JointDefinition> childrenJoints = new ArrayList<>();

   private final List<VisualDefinition> visualDefinitions = new ArrayList<>();
   private final List<CollisionShapeDefinition> collisionShapeDefinitions = new ArrayList<>();

   public RigidBodyDefinition()
   {
   }

   public RigidBodyDefinition(String name)
   {
      setName(name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setMass(double mass)
   {
      this.mass = mass;
   }

   public double getMass()
   {
      return mass;
   }

   public void setMomentOfInertia(Matrix3DReadOnly momentOfInertia)
   {
      this.momentOfInertia.set(momentOfInertia);
   }

   public Matrix3D getMomentOfInertia()
   {
      return momentOfInertia;
   }

   public void setInertiaPose(RigidBodyTransformReadOnly inertiaPose)
   {
      this.inertiaPose.set(inertiaPose);
   }

   public void setCenterOfMassOffset(double x, double y, double z)
   {
      inertiaPose.getTranslation().set(x, y, z);
   }

   public void setCenterOfMassOffset(Tuple3DReadOnly centerOfMassOffset)
   {
      inertiaPose.getTranslation().set(centerOfMassOffset);
   }

   public RigidBodyTransform getInertiaPose()
   {
      return inertiaPose;
   }

   public Vector3DBasics getCenterOfMassOffset()
   {
      return inertiaPose.getTranslation();
   }

   public void setParentJoint(JointDefinition parentJoint)
   {
      this.parentJoint = parentJoint;
   }

   public JointDefinition getParentJoint()
   {
      return parentJoint;
   }

   public void addChildJoint(JointDefinition childJoint)
   {
      childrenJoints.add(childJoint);
      childJoint.setPredecessor(this);
   }

   public void addChildJoints(Collection<? extends JointDefinition> childJoints)
   {
      childJoints.forEach(child -> addChildJoint(child));
   }

   public void removeChildJoint(JointDefinition childJoint)
   {
      if (childrenJoints.remove(childJoint))
         childJoint.setPredecessor(null);
   }

   public List<JointDefinition> getChildrenJoints()
   {
      return childrenJoints;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      visualDefinitions.add(visualDefinition);
   }

   public void addVisualDefinitions(Collection<VisualDefinition> visualDefinitions)
   {
      visualDefinitions.forEach(this::addVisualDefinition);
   }

   public List<VisualDefinition> getVisualDefinitions()
   {
      return visualDefinitions;
   }

   public void addCollisionShapeDefinition(CollisionShapeDefinition collisionShapeDefinition)
   {
      collisionShapeDefinitions.add(collisionShapeDefinition);
   }

   public List<CollisionShapeDefinition> getCollisionShapeDefinitions()
   {
      return collisionShapeDefinitions;
   }

   public RigidBodyBasics toRootBody(ReferenceFrame parentStationaryFrame)
   {
      return new RigidBody(getName(), parentStationaryFrame);
   }

   public RigidBodyBasics toRigidBody(JointBasics parentJoint)
   {
      return new RigidBody(getName(), parentJoint, getMomentOfInertia(), getMass(), getInertiaPose());
   }

   @Override
   public void applyTransform(Transform transform)
   {
      transform.transform(inertiaPose);
      transform.transform(momentOfInertia);
      if (visualDefinitions != null)
         visualDefinitions.forEach(visual -> transform.transform(visual.getOriginPose()));
   }

   @Override
   public void applyInverseTransform(Transform transform)
   {
      transform.inverseTransform(inertiaPose);
      transform.inverseTransform(momentOfInertia);
      if (visualDefinitions != null)
         visualDefinitions.forEach(visual -> transform.inverseTransform(visual.getOriginPose()));
   }

   @Override
   public String toString()
   {
      return name + ": inertia pose: (x,y,z) " + inertiaPose.getTranslation() + "(y,p,r) " + inertiaPose.getRotation().toStringAsYawPitchRoll() + "children: "
            + Arrays.toString(childrenJoints.stream().map(JointDefinition::getName).toArray(String[]::new));
   }
}
