package us.ihmc.scs2.simulation.robot;

import org.ejml.data.DMatrix;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.interfaces.FixedFrameTwistBasics;
import us.ihmc.mecano.tools.MecanoTools;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSixDoFJoint;
import us.ihmc.mecano.yoVariables.spatial.YoFixedFrameTwist;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;

public class SimSixDoFJoint extends YoSixDoFJoint implements SimJointBasics
{
   private final SimJointAuxiliaryData auxiliaryData;
   private final FixedFrameTwistBasics jointDeltaTwist;

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
      jointDeltaTwist = new YoFixedFrameTwist(name + "DeltaTwist", afterJointFrame, beforeJointFrame, afterJointFrame, registry);
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
   public FixedFrameTwistBasics getJointDeltaTwist()
   {
      return jointDeltaTwist;
   }

   @Override
   public int getJointDeltaVelocity(int rowStart, DMatrix matrixToPack)
   {
      getJointTwist().get(rowStart, matrixToPack);
      return rowStart + getDegreesOfFreedom();
   }

   @Override
   public void setJointDeltaTwistToZero()
   {
      jointDeltaTwist.setToZero();
   }

   @Override
   public void setJointDeltaTwist(JointReadOnly other)
   {
      setJointDeltaTwist(MecanoTools.checkTypeAndCast(other, SimSixDoFJoint.class));
   }

   public void setJointDeltaTwist(SimSixDoFJoint other)
   {
      // Cast to frameless object so we don't perform frame checks which would automatically fail.
      Vector3DReadOnly otherAngularDeltaVelocity = other.getJointDeltaTwist().getAngularPart();
      Vector3DReadOnly otherLinearDeltaVelocity = other.getJointDeltaTwist().getLinearPart();

      setJointAngularDeltaVelocity(otherAngularDeltaVelocity);
      setJointLinearDeltaVelocity(otherLinearDeltaVelocity);
   }

   @Override
   public int setJointDeltaVelocity(int rowStart, DMatrix matrix)
   {
      getJointTwist().set(rowStart, matrix);
      return rowStart + getDegreesOfFreedom();
   }

   @Override
   public void setJointAngularDeltaVelocity(Vector3DReadOnly jointAngularDeltaVelocity)
   {
      getJointDeltaTwist().getAngularPart().set(jointAngularDeltaVelocity);
   }

   @Override
   public void setJointLinearDeltaVelocity(Vector3DReadOnly jointLinearDeltaVelocity)
   {
      getJointDeltaTwist().getLinearPart().set(jointLinearDeltaVelocity);
   }
}
