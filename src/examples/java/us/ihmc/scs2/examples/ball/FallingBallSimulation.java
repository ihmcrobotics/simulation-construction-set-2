package us.ihmc.scs2.examples.ball;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class FallingBallSimulation
{
   public static void main(String[] args)
   {
      BallDefinition definition = new BallDefinition();
      RobotInitialStateProvider robotInitialStateProvider = robotInitialStateProvider(definition);

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), robotInitialStateProvider);
      simulationCore.addTerrainObject(new SlopeGroundDefinition(Math.toRadians(15.0)));

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(BallDefinition definition)
   {
      return jointName ->
      {
         if (!jointName.equals(definition.getRootJointName()))
            return null;

         SixDoFJointState jointState = new SixDoFJointState();
         jointState.setConfiguration(new Pose3D(0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
         jointState.setVelocity(new Vector3D(10.0, 0.0, 0.0), new Vector3D(-1.0, 0.0, 0.0));
         return jointState;
      };
   }
}
