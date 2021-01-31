package us.ihmc.scs2.examples.chaoticBall;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class LorenzStrangeAttractorSimulation
{
   public static void main(String[] args)
   {
      LorenzBallDefinition definition = new LorenzBallDefinition();

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition, new LorenzPhysicsPlugin());

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }
}