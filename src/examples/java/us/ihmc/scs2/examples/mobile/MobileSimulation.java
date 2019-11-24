package us.ihmc.scs2.examples.mobile;

import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class MobileSimulation
{
   public static void main(String[] args)
   {
      MobileDefinition definition = new MobileDefinition();

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, definition.getRobotControllerDefinition(), definition);

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }
}
