package us.ihmc.scs2.definition.robot;

import us.ihmc.mecano.multiBodySystem.PrismaticJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.PrismaticJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class PrismaticJointDefinition extends OneDoFJointDefinition
{
   public PrismaticJointDefinition()
   {
   }

   public PrismaticJointDefinition(String name)
   {
      setName(name);
   }

   @Override
   public PrismaticJointBasics toJoint(RigidBodyBasics predecessor)
   {
      PrismaticJoint joint = new PrismaticJoint(getName(), predecessor, getTransformToParent(), getAxis());
      joint.setJointLimits(getPositionLowerLimit(), getPositionUpperLimit());
      joint.setVelocityLimits(getVelocityLowerLimit(), getVelocityUpperLimit());
      joint.setEffortLimits(getEffortLowerLimit(), getEffortUpperLimit());
      return joint;
   }
}
