package us.ihmc.scs2.definition.robot;

import us.ihmc.mecano.multiBodySystem.PlanarJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.PlanarJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class PlanarJointDefinition extends JointDefinition
{
   public PlanarJointDefinition()
   {
   }

   public PlanarJointDefinition(String name)
   {
      setName(name);
   }

   @Override
   public PlanarJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new PlanarJoint(getName(), predecessor, getTransformToParent());
   }
}
