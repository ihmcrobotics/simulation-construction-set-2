package us.ihmc.scs2.simulation.robot;

import org.ejml.data.DMatrix;

import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.interfaces.FixedFrameTwistBasics;
import us.ihmc.mecano.tools.MecanoTools;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoPlanarJoint;
import us.ihmc.mecano.yoVariables.tools.YoMecanoFactories;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public class SimPlanarJoint extends YoPlanarJoint implements SimJointBasics
{
   private final SimJointAuxiliaryData auxiliaryData;
   private final FixedFrameTwistBasics jointDeltaTwist;

   public SimPlanarJoint(PlanarJointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
   {
      this(definition.getName(), predecessor, definition.getTransformToParent(), registry);
   }

   public SimPlanarJoint(String name, SimRigidBody predecessor, YoRegistry registry)
   {
      this(name, predecessor, null, registry);
   }

   public SimPlanarJoint(String name, SimRigidBody predecessor, RigidBodyTransformReadOnly transformToParent, YoRegistry registry)
   {
      super(name, predecessor, transformToParent, registry);
      auxiliaryData = new SimJointAuxiliaryData(this, registry);

      YoDouble angularDeltaVelocityY = new YoDouble(name + "AngularDeltaVelocityY", registry);
      YoDouble linearDeltaVelocityX = new YoDouble(name + "LinearDeltaVelocityX", registry);
      YoDouble linearDeltaVelocityZ = new YoDouble(name + "LinearDeltaVelocityZ", registry);
      jointDeltaTwist = YoMecanoFactories.newPlanarYoFixedFrameTwistBasics(angularDeltaVelocityY,
                                                                           linearDeltaVelocityX,
                                                                           linearDeltaVelocityZ,
                                                                           afterJointFrame,
                                                                           beforeJointFrame,
                                                                           afterJointFrame);
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
      matrixToPack.set(rowStart + 0, 0, getJointTwist().getAngularPartY());
      matrixToPack.set(rowStart + 1, 0, getJointTwist().getLinearPartX());
      matrixToPack.set(rowStart + 2, 0, getJointTwist().getLinearPartZ());
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
      setJointDeltaTwist(MecanoTools.checkTypeAndCast(other, SimPlanarJoint.class));
   }

   public void setJointDeltaTwist(SimPlanarJoint other)
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
      int index = rowStart;
      double qdRot = matrix.get(index++, 0);
      double xd = matrix.get(index++, 0);
      double zd = matrix.get(index++, 0);
      getJointDeltaTwist().setToZero();
      getJointDeltaTwist().setAngularPartY(qdRot);
      getJointDeltaTwist().getLinearPart().set(xd, 0.0, zd);
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
