package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
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
      super(name);
   }

   public SixDoFJointDefinition(String name, Tuple3DReadOnly offsetFromParent)
   {
      super(name, offsetFromParent);
   }

   @Override
   public SixDoFJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new SixDoFJoint(getName(), predecessor, getTransformToParent());
   }
}
