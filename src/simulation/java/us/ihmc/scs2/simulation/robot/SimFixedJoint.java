package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.FixedJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class SimFixedJoint extends FixedJoint implements SimJointBasics
{

   public SimFixedJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, predecessor, transformToParent);
      // TODO Auto-generated constructor stub
   }

   public SimFixedJoint(String name, RigidBodyBasics predecessor)
   {
      super(name, predecessor);
      // TODO Auto-generated constructor stub
   }
}
