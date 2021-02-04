package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSixDoFJoint;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimSixDoFJoint extends YoSixDoFJoint implements SimJointBasics
{
   private final SimJointAuxiliaryData auxiliaryData;

   public SimSixDoFJoint(SixDoFJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
   {
      this(definition.getName(), predecessor, definition.getTransformToParent(), registry);
   }

   public SimSixDoFJoint(String name, SimRigidBody predecessor, YoRegistry registry)
   {
      this(name, predecessor, null, registry);
   }

   public SimSixDoFJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, predecessor, transformToParent, registry);
      auxiliaryData = new SimJointAuxiliaryData(this, registry);
   }

   @Override
   public SimJointAuxiliaryData getAuxialiryData()
   {
      return auxiliaryData;
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
