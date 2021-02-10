package us.ihmc.scs2.examples.simulations;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class SlidingBoxSimulation
{
   public static void main(String[] args)
   {
      double slopeAngle = Math.toRadians(15.0);
      BoxRobotDefinition definition = new BoxRobotDefinition();
      definition.setInitialStateProvider(robotInitialStateProvider(definition, slopeAngle));

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition);
      simulationSession.addTerrainObject(new SlopeGroundDefinition(slopeAngle));

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(BoxRobotDefinition definition, double angle)
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
