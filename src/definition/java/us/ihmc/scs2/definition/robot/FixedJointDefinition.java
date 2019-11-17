package us.ihmc.scs2.definition.robot;

import us.ihmc.mecano.multiBodySystem.FixedJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.FixedJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class FixedJointDefinition extends JointDefinition
{
   public FixedJointDefinition()
   {
   }

   public FixedJointDefinition(String name)
   {
      setName(name);
   }

   @Override
   public FixedJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new FixedJoint(getName(), predecessor, getTransformToParent());
   }
}
