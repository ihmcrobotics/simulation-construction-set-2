package us.ihmc.scs2.examples.springDamperEngine;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.SimulationVisualizer;

public class SpringDamperSimulation
{
   public static void main(String[] args)
   {
      SpringDamperDefinition definition = new SpringDamperDefinition();

      SimulationVisualizer scs = new SimulationVisualizer();
      scs.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition, new SimplePhysicsPlugin());


      scs.startSimulation();
   }
}
