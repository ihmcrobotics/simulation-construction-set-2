package us.ihmc.scs2.simulation.physicsEngine;

import java.util.List;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.algorithms.ForwardDynamicsCalculator;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.interfaces.FixedFrameWrenchBasics;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.definition.controller.interfaces.ControllerOutputReadOnly;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;

public class FeatherstoneForwardDynamicsPlugin implements RobotPhysicsEnginePlugin
{
   private MultiBodySystemBasics input;
   private ForwardDynamicsCalculator forwardDynamicsCalculator;

   private ControllerOutputReadOnly controllerOutput;
   private ExternalInteractionProvider externalInteractionProvider;

   public FeatherstoneForwardDynamicsPlugin()
   {
   }

   @Override
   public void setMultiBodySystem(MultiBodySystemBasics multiBodySystem)
   {
      input = multiBodySystem;
      forwardDynamicsCalculator = new ForwardDynamicsCalculator(input);
   }

   @Override
   public void initialize()
   {
   }

   @Override
   public void submitControllerOutput(ControllerOutputReadOnly controllerOutput)
   {
      this.controllerOutput = controllerOutput;
   }

   @Override
   public void submitExternalInteractions(ExternalInteractionProvider externalInteractionProvider)
   {
      this.externalInteractionProvider = externalInteractionProvider;
   }

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      applyControllerOutput();
      applyExternalWrenches();
      forwardDynamicsCalculator.setGravitionalAcceleration(gravity);
      forwardDynamicsCalculator.compute();
      MultiBodySystemTools.insertJointsState(input.getJointsToConsider(), JointStateType.ACCELERATION, forwardDynamicsCalculator.getJointAccelerationMatrix());
   }

   public void applyControllerOutput()
   {
      List<? extends JointBasics> jointsToConsider = input.getJointsToConsider();

      for (JointBasics joint : jointsToConsider)
      {
         JointStateReadOnly jointOutput = controllerOutput.getJointOutput(joint);
         if (jointOutput.hasOutputFor(JointStateType.EFFORT))
            jointOutput.getEffort(joint);
      }
   }

   public void applyExternalWrenches()
   {
      forwardDynamicsCalculator.setExternalWrenchesToZero();

      if (externalInteractionProvider == null)
         return;

      for (RigidBodyBasics rigidBody : externalInteractionProvider.getRigidBodies())
      {
         FixedFrameWrenchBasics externalWrench = forwardDynamicsCalculator.getExternalWrench(rigidBody);
         if (externalWrench != null)
         {
            WrenchReadOnly externalWrench2 = externalInteractionProvider.getExternalWrench(rigidBody);
            externalWrench.setMatchingFrame(externalWrench2);
         }
      }

      externalInteractionProvider = null;
   }

   @Override
   public String getPluginName()
   {
      return getClass().getSimpleName();
   }
}
