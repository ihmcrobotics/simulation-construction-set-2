package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.mecano.tools.MultiBodySystemTools;

public class SingleRobotForwardDynamicsPlugin
{
   private final MultiBodySystemBasics input;
   private final ForwardDynamicsCalculator forwardDynamicsCalculator;

   public SingleRobotForwardDynamicsPlugin(MultiBodySystemBasics input)
   {
      this.input = input;
      forwardDynamicsCalculator = new ForwardDynamicsCalculator(input);
   }

   public void doScience(double time, double dt, Vector3DReadOnly gravity)
   {
      forwardDynamicsCalculator.setGravitionalAcceleration(gravity);
      forwardDynamicsCalculator.compute();
   }

   public void writeJointAccelerations()
   {
      MultiBodySystemTools.insertJointsState(input.getJointsToConsider(), JointStateType.ACCELERATION, forwardDynamicsCalculator.getJointAccelerationMatrix());
   }

   public void resetExternalWrenches()
   {
      forwardDynamicsCalculator.setExternalWrenchesToZero();
   }

   public ForwardDynamicsCalculator getForwardDynamicsCalculator()
   {
      return forwardDynamicsCalculator;
   }
}