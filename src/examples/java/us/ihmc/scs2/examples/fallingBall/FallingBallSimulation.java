package us.ihmc.scs2.examples.fallingBall;

import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.sessionVisualizer.JavaFXSimulationConstructionSet;

public class FallingBallSimulation
{
   public static void main(String[] args)
   {
      FallingBallDefinition definition = new FallingBallDefinition();

      JavaFXSimulationConstructionSet scs = new JavaFXSimulationConstructionSet();
      scs.addRobot(definition, ControllerDefinition.emptyControllerDefinition(), definition);
      scs.addTerrainObject(new SlopeGroundDefinition());
      scs.startSimulation();
   }
}
