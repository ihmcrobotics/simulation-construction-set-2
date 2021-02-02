package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.RevoluteJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;

public class SimRevoluteJoint extends RevoluteJoint implements SimJointBasics
{

   public SimRevoluteJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, transformToParent, jointAxis);
   }

   public SimRevoluteJoint(String name, SimRigidBody predecessor, Tuple3DReadOnly jointOffset, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, jointOffset, jointAxis);
   }

   public SimRevoluteJoint(String name, SimRigidBody predecessor, Vector3DReadOnly jointAxis)
   {
      super(name, predecessor, jointAxis);
   }

   public SimRevoluteJoint(RevoluteJointDefinition definition, SimRigidBody predecessor)
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
