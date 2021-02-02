package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.PlanarJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class SimPlanarJoint extends PlanarJoint implements SimJointBasics
{

   public SimPlanarJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, predecessor, transformToParent);
      // TODO Auto-generated constructor stub
   }

   public SimPlanarJoint(String name, RigidBodyBasics predecessor)
   {
      super(name, predecessor);
      // TODO Auto-generated constructor stub
   }

}
