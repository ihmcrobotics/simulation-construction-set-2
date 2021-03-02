package us.ihmc.scs2.examples.mobile;

import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class MobileSimulation
{
   public static void main(String[] args)
   {
      MobileDefinition definition = new MobileDefinition();

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition, definition.getRobotControllerDefinition(), definition);

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }
}
