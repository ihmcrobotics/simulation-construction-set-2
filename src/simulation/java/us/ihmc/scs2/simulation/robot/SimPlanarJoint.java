package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.PlanarJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;

public class SimPlanarJoint extends PlanarJoint implements SimJointBasics
{

   public SimPlanarJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent)
   {
      super(name, predecessor, transformToParent);
   }

   public SimPlanarJoint(String name, SimRigidBody predecessor)
   {
      super(name, predecessor);
   }

   public SimPlanarJoint(PlanarJointDefinition definition, SimRigidBody predecessor)
   {
      super(definition.getName(), predecessor, definition.getTransformToParent());
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
