package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.interfaces.Transformable;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.transform.interfaces.Transform;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.RigidBody;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.YawPitchRollTransformDefinition;
import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@XmlType(propOrder = {"name", "mass", "momentOfInertia", "inertiaPose", "childrenJoints", "visualDefinitions", "collisionShapeDefinitions"})
public class RigidBodyDefinition implements Transformable
{
   private String name;
   private double mass;
   private MomentOfInertiaDefinition momentOfInertia = new MomentOfInertiaDefinition();
   /**
    * In parent after joint frame.
    */
   private YawPitchRollTransformDefinition inertiaPose = new YawPitchRollTransformDefinition();

   private JointDefinition parentJoint;
   private List<JointDefinition> childrenJoints = new ArrayList<>();

   private List<VisualDefinition> visualDefinitions = new ArrayList<>();
   private List<CollisionShapeDefinition> collisionShapeDefinitions = new ArrayList<>();

   public RigidBodyDefinition()
   {
   }

   public RigidBodyDefinition(String name)
   {
      setName(name);
   }

   public RigidBodyDefinition(RigidBodyDefinition other)
   {
      name = other.name;
      mass = other.mass;
      momentOfInertia.set(other.momentOfInertia);
      inertiaPose.set(other.inertiaPose);
      for (VisualDefinition visualDefinition : other.visualDefinitions)
         visualDefinitions.add(visualDefinition.copy());
      for (CollisionShapeDefinition collisionShapeDefinition : other.collisionShapeDefinitions)
         collisionShapeDefinitions.add(collisionShapeDefinition.copy());
   }

   @XmlAttribute
   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   @XmlElement
   public void setMass(double mass)
   {
      this.mass = mass;
   }

   public double getMass()
   {
      return mass;
   }

   @XmlElement
   public void setMomentOfInertia(MomentOfInertiaDefinition momentOfInertia)
   {
      this.momentOfInertia = momentOfInertia;
   }

   public void setMomentOfInertia(Matrix3DReadOnly momentOfInertia)
   {
      this.momentOfInertia.set(momentOfInertia);
   }

   public MomentOfInertiaDefinition getMomentOfInertia()
   {
      return momentOfInertia;
   }

   @XmlElement
   public void setInertiaPose(YawPitchRollTransformDefinition inertiaPose)
   {
      this.inertiaPose = inertiaPose;
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
      setCenterOfMassOffset(centerOfMassOffset.getX(), centerOfMassOffset.getY(), centerOfMassOffset.getZ());
   }

   public YawPitchRollTransformDefinition getInertiaPose()
   {
      return inertiaPose;
   }

   public Vector3D getCenterOfMassOffset()
   {
      return inertiaPose.getTranslation();
   }

   @XmlTransient
   public void setParentJoint(JointDefinition parentJoint)
   {
      this.parentJoint = parentJoint;
   }

   public JointDefinition getParentJoint()
   {
      return parentJoint;
   }

   @XmlElement(name = "childJoint")
   public void setChildrenJoints(List<JointDefinition> childrenJoints)
   {
      if (this.childrenJoints != null)
      {
         for (JointDefinition childJoint : this.childrenJoints)
            childJoint.setPredecessor(null);
      }

      this.childrenJoints = childrenJoints;

      if (this.childrenJoints != null)
      {
         for (JointDefinition childJoint : this.childrenJoints)
            childJoint.setPredecessor(this);
      }
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

   @XmlElement(name = "visual")
   public void setVisualDefinitions(List<VisualDefinition> visualDefinitions)
   {
      this.visualDefinitions = visualDefinitions;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      if (visualDefinition == null)
         return;
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

   @XmlElement(name = "collision")
   public void setCollisionShapeDefinitions(List<CollisionShapeDefinition> collisionShapeDefinitions)
   {
      this.collisionShapeDefinitions = collisionShapeDefinitions;
   }

   public void addCollisionShapeDefinition(CollisionShapeDefinition collisionShapeDefinition)
   {
      if (collisionShapeDefinition == null)
         return;
      collisionShapeDefinitions.add(collisionShapeDefinition);
   }

   public void addCollisionShapeDefinitions(Collection<CollisionShapeDefinition> collisionShapeDefinitions)
   {
      collisionShapeDefinitions.forEach(this::addCollisionShapeDefinition);
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
      visualDefinitions.forEach(visual -> transform.transform(visual.getOriginPose()));
      collisionShapeDefinitions.forEach(collision -> transform.transform(collision.getOriginPose()));
   }

   @Override
   public void applyInverseTransform(Transform transform)
   {
      transform.inverseTransform(inertiaPose);
      transform.inverseTransform(momentOfInertia);
      visualDefinitions.forEach(visual -> transform.inverseTransform(visual.getOriginPose()));
      collisionShapeDefinitions.forEach(collision -> transform.inverseTransform(collision.getOriginPose()));
   }

   public RigidBodyDefinition copy()
   {
      return new RigidBodyDefinition(this);
   }

   public RigidBodyDefinition copyRecursive()
   {
      RigidBodyDefinition copy = copy();
      for (JointDefinition childJoint : childrenJoints)
      {
         JointDefinition childJointCopy = childJoint.copyRecursive();
         copy.addChildJoint(childJointCopy);
      }
      return copy;
   }

   @Override
   public String toString()
   {
      String childrenString = childrenJoints == null ? "[]" : EuclidCoreIOTools.getCollectionString("[", "]", ", ", childrenJoints, JointDefinition::getName);
      return name + ": inertia pose: " + inertiaPose + ", children: " + childrenString;
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, name);
      bits = EuclidHashCodeTools.addToHashCode(bits, mass);
      bits = EuclidHashCodeTools.addToHashCode(bits, momentOfInertia);
      bits = EuclidHashCodeTools.addToHashCode(bits, inertiaPose);
      bits = EuclidHashCodeTools.addToHashCode(bits, childrenJoints);
      bits = EuclidHashCodeTools.addToHashCode(bits, visualDefinitions);
      bits = EuclidHashCodeTools.addToHashCode(bits, collisionShapeDefinitions);
      return EuclidHashCodeTools.toIntHashCode(bits);
   }

   @Override
   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null)
         return false;
      if (getClass() != object.getClass())
         return false;

      RigidBodyDefinition other = (RigidBodyDefinition) object;

      if (!Objects.equals(name, other.name))
         return false;
      if (!EuclidCoreTools.equals(mass, other.mass))
         return false;
      if (!Objects.equals(momentOfInertia, other.momentOfInertia))
         return false;
      if (!Objects.equals(inertiaPose, other.inertiaPose))
         return false;
      if (!Objects.equals(childrenJoints, other.childrenJoints))
         return false;
      if (!Objects.equals(visualDefinitions, other.visualDefinitions))
         return false;
      if (!Objects.equals(collisionShapeDefinitions, other.collisionShapeDefinitions))
         return false;

      return true;
   }
}
