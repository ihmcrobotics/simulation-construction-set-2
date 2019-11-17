package us.ihmc.scs2.sessionVisualizer;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.stage.Stage;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.SimulationCore;
import us.ihmc.scs2.simulation.physicsEngine.RobotPhysicsEnginePlugin;

public class JavaFXSimulationConstructionSet
{
   private final SimulationCore simulationCore;
   private final SessionVisualizer simulationGUI;

   public JavaFXSimulationConstructionSet()
   {
      simulationCore = new SimulationCore();
      simulationGUI = new SessionVisualizer();
   }

   public void addRobot(RobotDefinition robotDefinition)
   {
      simulationCore.addRobot(robotDefinition, ControllerDefinition.emptyControllerDefinition());
   }

   public void addRobot(RobotDefinition robotDefinition, ControllerDefinition controllerDefinition, RobotInitialStateProvider initialStateProvider,
                        RobotPhysicsEnginePlugin... plugins)
   {
      simulationCore.addRobot(robotDefinition, controllerDefinition, initialStateProvider, plugins);
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      simulationCore.addTerrainObject(terrainObjectDefinition);
   }

   public void startSimulation()
   {
      PlatformImpl.startup(() ->
      {
         try
         {
            simulationGUI.start(new Stage());
            simulationGUI.startSession(simulationCore);
         }
         catch (Exception e)
         {
            Platform.exit();
            throw new RuntimeException(e);
         }
      });
   }

   public SimulationCore getSimulationCore()
   {
      return simulationCore;
   }
}
