package us.ihmc.scs2.examples.springDamperEngine;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class SpringDamperSimulation
{
   public static void main(String[] args)
   {
      SpringDamperDefinition definition = new SpringDamperDefinition();

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition, new SimplePhysicsPlugin());

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }
}
