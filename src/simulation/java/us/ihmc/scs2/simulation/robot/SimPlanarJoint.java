package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoPlanarJoint;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimPlanarJoint extends YoPlanarJoint implements SimJointBasics
{

   public SimPlanarJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, predecessor, transformToParent, registry);
   }

   public SimPlanarJoint(String name, SimRigidBody predecessor, YoRegistry registry)
   {
      super(name, predecessor, registry);
   }

   public SimPlanarJoint(PlanarJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
   {
      super(definition.getName(), predecessor, definition.getTransformToParent(), registry);
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
