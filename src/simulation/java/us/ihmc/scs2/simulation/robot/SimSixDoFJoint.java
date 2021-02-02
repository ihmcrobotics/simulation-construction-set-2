package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.SixDoFJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class SimSixDoFJoint extends SixDoFJoint implements SimJointBasics
{

   public SimSixDoFJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, predecessor, transformToParent);
      // TODO Auto-generated constructor stub
   }

   public SimSixDoFJoint(String name, RigidBodyBasics predecessor)
   {
      super(name, predecessor);
      // TODO Auto-generated constructor stub
   }

}
