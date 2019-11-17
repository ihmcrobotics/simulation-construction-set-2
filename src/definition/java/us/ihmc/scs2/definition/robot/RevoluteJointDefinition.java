package us.ihmc.scs2.definition.robot;

import us.ihmc.mecano.multiBodySystem.RevoluteJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RevoluteJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class RevoluteJointDefinition extends OneDoFJointDefinition
{
   public RevoluteJointDefinition()
   {
   }

   public RevoluteJointDefinition(String name)
   {
      setName(name);
   }

   @Override
   public RevoluteJointBasics toJoint(RigidBodyBasics predecessor)
   {
      RevoluteJoint joint = new RevoluteJoint(getName(), predecessor, getTransformToParent(), getAxis());
      joint.setJointLimits(getPositionLowerLimit(), getPositionUpperLimit());
      joint.setVelocityLimits(getVelocityLowerLimit(), getVelocityUpperLimit());
      joint.setEffortLimits(getEffortLowerLimit(), getEffortUpperLimit());
      return joint;
   }
}
