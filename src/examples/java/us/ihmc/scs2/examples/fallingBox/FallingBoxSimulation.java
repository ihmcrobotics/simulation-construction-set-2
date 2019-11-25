package us.ihmc.scs2.examples.fallingBox;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.examples.fallingBall.SlopeGroundDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class FallingBoxSimulation
{
   public static void main(String[] args)
   {
      FallingBoxDefinition definition = new FallingBoxDefinition();

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition);
      simulationCore.addTerrainObject(new SlopeGroundDefinition());

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }
}
