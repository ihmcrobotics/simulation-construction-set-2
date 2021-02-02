package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.FixedJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimFixedJoint extends FixedJoint implements SimJointBasics
{
   public SimFixedJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, predecessor, transformToParent);
   }

   public SimFixedJoint(String name, SimRigidBody predecessor, YoRegistry registry)
   {
      super(name, predecessor);
   }

   public SimFixedJoint(FixedJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
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
