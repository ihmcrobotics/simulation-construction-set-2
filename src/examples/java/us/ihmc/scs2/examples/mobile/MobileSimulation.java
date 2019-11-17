package us.ihmc.scs2.examples.mobile;

import us.ihmc.scs2.sessionVisualizer.JavaFXSimulationConstructionSet;

public class MobileSimulation
{
   public static void main(String[] args)
   {
      MobileDefinition definition = new MobileDefinition();

      JavaFXSimulationConstructionSet scs = new JavaFXSimulationConstructionSet();
      scs.addRobot(definition, definition.getRobotControllerDefinition(), definition);
      scs.startSimulation();
   }
}
