package us.ihmc.scs2.examples.ball;

import us.ihmc.euclid.geometry.Pose3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.SixDoFJointState;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class FallingBallSimulation
{
   public static void main(String[] args)
   {
      BallRobotDefinition definition = new BallRobotDefinition();
      definition.setInitialStateProvider(robotInitialStateProvider(definition));

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition);
      simulationSession.addTerrainObject(new SlopeGroundDefinition(Math.toRadians(15.0)));

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }

   private static RobotInitialStateProvider robotInitialStateProvider(BallRobotDefinition definition)
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
