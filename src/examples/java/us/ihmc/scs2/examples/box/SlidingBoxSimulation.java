package us.ihmc.scs2.examples.box;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.examples.ball.SlopeGroundDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class SlidingBoxSimulation
{
   public static void main(String[] args)
   {
      double slopeAngle = Math.toRadians(15.0);
      BoxDefinition definition = new BoxDefinition();
      RobotInitialStateProvider robotInitialStateProvider = robotInitialStateProvider(definition, slopeAngle);

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), robotInitialStateProvider);
      simulationCore.addTerrainObject(new SlopeGroundDefinition(slopeAngle));

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(BoxDefinition definition, double angle)
   {
      return jointName ->
      {
         if (!jointName.equals(definition.getRootJointName()))
            return null;

         SixDoFJointState jointState = new SixDoFJointState();
         jointState.setConfiguration(new Pose3D(0.0, 0.0, 0.155, 0.0, angle, 0.0));
         return jointState;
      };
   }
}
