package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.RevoluteJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class SimRevoluteJoint extends RevoluteJoint implements SimJointBasics
{

   public SimRevoluteJoint(String name, RigidBodyBasics predecessor, RigidBodyTransformReadOnly transformToParent, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, transformToParent, jointAxis);
      // TODO Auto-generated constructor stub
   }

   public SimRevoluteJoint(String name, RigidBodyBasics predecessor, Tuple3DReadOnly jointOffset, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, jointOffset, jointAxis);
      // TODO Auto-generated constructor stub
   }

   public SimRevoluteJoint(String name, RigidBodyBasics predecessor, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, jointAxis);
      // TODO Auto-generated constructor stub
   }

}
