package us.ihmc.scs2.simulation.robot;

import org.ejml.data.DMatrix;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.FixedJoint;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.Twist;
import us.ihmc.mecano.spatial.interfaces.TwistReadOnly;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimFixedJoint extends FixedJoint implements SimJointBasics
{
   private final SimJointAuxiliaryData auxiliaryData;
   private final TwistReadOnly jointDeltaTwist;

   public SimFixedJoint(FixedJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
   {
      this(definition.getName(), predecessor, definition.getTransformToParent(), registry);
   }

   public SimFixedJoint(String name, SimRigidBody predecessor, YoRegistry registry)
   {
      this(name, predecessor, null, registry);
   }

   public SimFixedJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, predecessor, transformToParent);
      auxiliaryData = new SimJointAuxiliaryData(this, registry);
      jointDeltaTwist = new Twist(jointFrame, jointFrame, jointFrame);
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

   @Override
   public TwistReadOnly getJointDeltaTwist()
   {
      return jointDeltaTwist;
   }

   @Override
   public int getJointDeltaVelocity(int rowStart, DMatrix matrixToPack)
   {
      return rowStart;
   }

   @Override
   public void setJointDeltaTwistToZero()
   {
   }

   @Override
   public void setJointDeltaTwist(JointReadOnly other)
   {
   }

   @Override
   public int setJointDeltaVelocity(int rowStart, DMatrix jointDeltaVelocity)
   {
      return rowStart;
   }

   @Override
   public void setJointAngularDeltaVelocity(Vector3DReadOnly jointAngularDeltaVelocity)
   {
   }

   @Override
   public void setJointLinearDeltaVelocity(Vector3DReadOnly jointLinearDeltaVelocity)
   {
   }
}
