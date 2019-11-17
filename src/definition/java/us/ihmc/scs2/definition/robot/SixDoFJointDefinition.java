package us.ihmc.scs2.definition.robot;

import us.ihmc.mecano.multiBodySystem.SixDoFJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointBasics;

public class SixDoFJointDefinition extends JointDefinition
{
   public SixDoFJointDefinition()
   {
   }

   public SixDoFJointDefinition(String name)
   {
      setName(name);
   }

   @Override
   public SixDoFJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new SixDoFJoint(getName(), predecessor, getTransformToParent());
   }
}
