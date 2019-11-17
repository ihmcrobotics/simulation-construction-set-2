package us.ihmc.scs2.definition.robot;

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
      setName(name);
   }

   @Override
   public SphericalJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new SphericalJoint(getName(), predecessor, getTransformToParent());
   }
}
