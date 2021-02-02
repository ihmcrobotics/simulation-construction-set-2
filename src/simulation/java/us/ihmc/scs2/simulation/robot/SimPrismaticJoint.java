package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.PrismaticJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;

public class SimPrismaticJoint extends PrismaticJoint implements SimJointBasics
{

   public SimPrismaticJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, transformToParent, jointAxis);
   }

   public SimPrismaticJoint(String name, SimRigidBody predecessor, Tuple3DReadOnly jointOffset, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, jointOffset, jointAxis);
   }

   public SimPrismaticJoint(PrismaticJointDefinition definition, SimRigidBody predecessor)
   {
      super(definition.getName(), predecessor, definition.getTransformToParent(), definition.getAxis());
   }

   @Override
   public void setSuccessor(RigidBodyBasics successor)
   {
      if (successor instanceof SimRigidBody)
         super.setSuccessor(successor);
      else
         throw new IllegalArgumentException("Can only set a " + SimRigidBody.class.getSimpleName() + " as successor of a " + getClass().getSimpleName());
   }

   @Override
   public SimRigidBody getPredecessor()
   {
      return (SimRigidBody) super.getPredecessor();
   }

   @Override
   public SimRigidBody getSuccessor()
   {
      return (SimRigidBody) super.getSuccessor();
   }
}
