package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.ihmc.euclid.matrix.Matrix3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.mecano.multiBodySystem.RigidBody;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

public class RigidBodyDefinition
{
   private String name;
   private double mass;
   private final Matrix3D momentOfInertia = new Matrix3D();
   private final RigidBodyTransform inertiaPose = new RigidBodyTransform();

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

   public Matrix3D getMomentOfInertia()
   {
      return momentOfInertia;
   }

   public RigidBodyTransform getInertiaPose()
   {
      return inertiaPose;
   }

   public void addChildJoint(JointDefinition childJoint)
   {
      childrenJoints.add(childJoint);
   }

   public List<JointDefinition> getChildrenJoints()
   {
      return childrenJoints;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      visualDefinitions.add(visualDefinition);
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
   public String toString()
   {
      return name + ": pose: (x,y,z) " + inertiaPose.getTranslation() + "(y,p,r) " + inertiaPose.getRotation().toStringAsYawPitchRoll() + "children: "
            + Arrays.toString(childrenJoints.stream().map(JointDefinition::getName).toArray(String[]::new));
   }
}
