package us.ihmc.scs2.examples.chaoticBall;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class ChaoticBallSimulation
{
   public static void main(String[] args)
   {
      ChaoticBallDefinition definition = new ChaoticBallDefinition();

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition, new ChaosPhysicsPlugin());

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }
}