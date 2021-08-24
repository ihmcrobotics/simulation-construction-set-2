package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
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
      super(name);
   }

   public PlanarJointDefinition(String name, Tuple3DReadOnly offsetFromParent)
   {
      super(name, offsetFromParent);
   }

   @Override
   public PlanarJointBasics toJoint(RigidBodyBasics predecessor)
   {
      return new PlanarJoint(getName(), predecessor, getTransformToParent());
   }
}
