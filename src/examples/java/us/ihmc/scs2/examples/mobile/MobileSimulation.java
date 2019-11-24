package us.ihmc.scs2.examples.mobile;

import us.ihmc.scs2.sessionVisualizer.SimulationVisualizer;

public class MobileSimulation
{
   public static void main(String[] args)
   {
      MobileDefinition definition = new MobileDefinition();

      SimulationVisualizer scs = new SimulationVisualizer();
      scs.addRobot(definition, definition.getRobotControllerDefinition(), definition);
      scs.startSimulation();
   }
}
