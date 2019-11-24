package us.ihmc.scs2.examples.fallingBall;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.SimulationVisualizer;

public class FallingBallSimulation
{
   public static void main(String[] args)
   {
      FallingBallDefinition definition = new FallingBallDefinition();

      SimulationVisualizer scs = new SimulationVisualizer();
      scs.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition);
      scs.addTerrainObject(new SlopeGroundDefinition());
      scs.startSimulation();
   }
}
