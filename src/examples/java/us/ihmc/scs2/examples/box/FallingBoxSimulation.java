package us.ihmc.scs2.examples.box;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.examples.ball.SlopeGroundDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class FallingBoxSimulation
{
   public static void main(String[] args)
   {
      BoxRobotDefinition definition = new BoxRobotDefinition();
      RobotInitialStateProvider robotInitialStateProvider = robotInitialStateProvider(definition);

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), robotInitialStateProvider);
      simulationCore.addTerrainObject(new SlopeGroundDefinition());

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(BoxRobotDefinition definition)
   {
      return jointName ->
      {
         if (!jointName.equals(definition.getRootJointName()))
            return null;

         SixDoFJointState jointState = new SixDoFJointState();
         jointState.setConfiguration(new Pose3D(0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
         return jointState;
      };
   }
}
