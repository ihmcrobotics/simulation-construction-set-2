package us.ihmc.scs2.examples.springDamperEngine;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationSession;

public class SpringDamperSimulation
{
   public static void main(String[] args)
   {
      SpringDamperDefinition definition = new SpringDamperDefinition();

      SimulationSession simulationSession = new SimulationSession();
      simulationSession.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition, new SimplePhysicsPlugin());

      SessionVisualizer.startSessionVisualizer(simulationSession);
   }
}
