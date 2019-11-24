package us.ihmc.scs2.examples.fallingBall;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizer;
import us.ihmc.scs2.simulation.SimulationCore;

public class FallingBallSimulation
{
   public static void main(String[] args)
   {
      FallingBallDefinition definition = new FallingBallDefinition();

      SimulationCore simulationCore = new SimulationCore();
      simulationCore.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition);
      simulationCore.addTerrainObject(new SlopeGroundDefinition());

      SessionVisualizer.startSessionVisualizer(simulationCore);
   }
}
