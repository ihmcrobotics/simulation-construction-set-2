package us.ihmc.scs2.simulation.robot;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoRevoluteJoint;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimRevoluteJoint extends YoRevoluteJoint implements SimJointBasics
{

   public SimRevoluteJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, Vector3DReadOnly jointAxis, YoRegistry registry)
   {
      super(name, predecessor, transformToParent, jointAxis, registry);
   }

   public SimRevoluteJoint(String name, SimRigidBody predecessor, Tuple3DReadOnly jointOffset, Vector3DReadOnly jointAxis, YoRegistry registry)
   {
      super(name, predecessor, jointOffset, jointAxis, registry);
   }

   public SimRevoluteJoint(String name, SimRigidBody predecessor, Vector3DReadOnly jointAxis, YoRegistry registry)
   {
      super(name, predecessor, jointAxis, registry);
   }

   public SimRevoluteJoint(RevoluteJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
   {
      super(definition.getName(), predecessor, definition.getTransformToParent(), definition.getAxis(), registry);
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
