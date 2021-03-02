package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.interfaces.Transformable;
import us.ihmc.euclid.transform.interfaces.Transform;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class KinematicPointDefinition implements Transformable
{
   private String name;
   private Vector3D offsetFromJoint = new Vector3D();

   public KinematicPointDefinition()
   {
   }

   public KinematicPointDefinition(String name, Tuple3DReadOnly offsetFromJoint)
   {
      this.name = name;
      this.offsetFromJoint.set(offsetFromJoint);
   }

   public KinematicPointDefinition(KinematicPointDefinition other)
   {
      name = other.name;
      offsetFromJoint.set(other.offsetFromJoint);
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Vector3D getOffsetFromJoint()
   {
      return offsetFromJoint;
   }

   public void setOffsetFromJoint(Tuple3DReadOnly offsetFromJoint)
   {
      this.offsetFromJoint.set(offsetFromJoint);
   }

   public KinematicPointDefinition copy()
   {
      return new KinematicPointDefinition(this);
   }

   @Override
   public void applyTransform(Transform transform)
   {
      transform.transform(offsetFromJoint);
   }

   @Override
   public void applyInverseTransform(Transform transform)
   {
      transform.inverseTransform(offsetFromJoint);
   }
}
