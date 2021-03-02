package us.ihmc.scs2.examples.ellipsoid;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.examples.ball.SlopeGroundDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class EllipsoidCylinderSimulation
{
   public static void main(String[] args)
   {
      EllipsoidRobotSimulation definition = new EllipsoidRobotSimulation();
      RobotInitialStateProvider robotInitialStateProvider = robotInitialStateProvider(definition);

      SimulationSession simulationCore = new SimulationSession();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), robotInitialStateProvider);
      simulationCore.addTerrainObject(new SlopeGroundDefinition());

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(EllipsoidRobotSimulation definition)
   {
      return jointName ->
      {
         if (!jointName.equals(definition.getRootJointName()))
            return null;

         SixDoFJointState jointState = new SixDoFJointState();
         jointState.setConfiguration(new Pose3D(0.0, 0.0, 1.0, 0.0, 0.0, 0.6));
         return jointState;
      };
   }
}
