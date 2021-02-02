package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.interfaces.Transformable;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.transform.interfaces.Transform;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class SensorDefinition implements Transformable
{
   private String name;
   private final RigidBodyTransform transformToJoint = new RigidBodyTransform();

   // Period in milliseconds
   private int updatePeriod;

   public SensorDefinition()
   {
   }

   public SensorDefinition(String name, Tuple3DReadOnly offsetFromJoint)
   {
      this.name = name;
      transformToJoint.setTranslationAndIdentityRotation(offsetFromJoint);
   }

   public SensorDefinition(String name, RigidBodyTransformReadOnly transformToJoint)
   {
      this.name = name;
      this.transformToJoint.set(transformToJoint);
   }

   public SensorDefinition(SensorDefinition other)
   {
      name = other.name;
      transformToJoint.set(other.transformToJoint);
      updatePeriod = other.updatePeriod;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public RigidBodyTransform getTransformToJoint()
   {
      return transformToJoint;
   }

   public void setOffsetFromJoint(Tuple3DReadOnly offsetFromJoint)
   {
      transformToJoint.setTranslationAndIdentityRotation(offsetFromJoint);
   }

   public void setTransformToJoint(RigidBodyTransformReadOnly transformToJoint)
   {
      this.transformToJoint.set(transformToJoint);
   }

   public int getUpdatePeriod()
   {
      return updatePeriod;
   }

   public void setUpdatePeriod(int updatePeriod)
   {
      this.updatePeriod = updatePeriod;
   }

   public SensorDefinition copy()
   {
      return new SensorDefinition(this);
   }

   @Override
   public void applyTransform(Transform transform)
   {
      transform.transform(transformToJoint);
   }

   @Override
   public void applyInverseTransform(Transform transform)
   {
      transform.inverseTransform(transformToJoint);
   }
}
