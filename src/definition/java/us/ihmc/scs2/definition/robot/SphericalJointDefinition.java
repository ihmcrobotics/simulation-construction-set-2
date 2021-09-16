package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.mecano.multiBodySystem.SphericalJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SphericalJointBasics;

public class SphericalJointDefinition extends JointDefinition
{
   public SphericalJointDefinition()
   {
   }

   public SphericalJointDefinition(String name)
   {
      super(name);
   }

   public SphericalJointDefinition(String name, Tuple3DReadOnly offsetFromParent)
   {
      super(name, offsetFromParent);
   }

   public SphericalJointDefinition(SphericalJointDefinition other)
   {
      super(other);
   }

   @Override
   public SphericalJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new SphericalJoint(getName(), predecessor, getTransformToParent());
   }

   @Override
   public SphericalJointDefinition copy()
   {
      return new SphericalJointDefinition(this);
   }
}
