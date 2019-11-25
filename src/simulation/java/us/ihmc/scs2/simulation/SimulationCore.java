package us.ihmc.scs2.simulation;

import java.util.List;

import us.ihmc.commons.Conversions;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.simulation.physicsEngine.FeatherstoneForwardDynamicsPlugin;
import us.ihmc.scs2.simulation.physicsEngine.FirstOrderMultiBodyStateIntegratorPlugin;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine;
import us.ihmc.scs2.simulation.physicsEngine.RobotPhysicsEnginePlugin;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoFrameVector3D;

public class SimulationCore extends Session
{
   private final ReferenceFrame worldFrame = ReferenceFrameTools.constructARootFrame("worldFrame");
   private final PhysicsEngine physicsEngine = new PhysicsEngine(worldFrame, rootRegistry);
   private final YoDouble simulationTime = new YoDouble("simulationTime", rootRegistry);
   private final YoFrameVector3D gravity = new YoFrameVector3D("gravity", ReferenceFrame.getWorldFrame(), rootRegistry);
   private final String simulationName;

   public SimulationCore()
   {
      this(retrieveCallerName());
   }

   public SimulationCore(String simulationName)
   {
      this.simulationName = simulationName;
      submitBufferSizeRequest(200000);
      setSessionTickToTimeIncrement(Conversions.secondsToNanoseconds(0.0001));
      setSessionMode(SessionMode.PAUSE);
      gravity.set(0.0, 0.0, -9.81);
   }

   @Override
   public void initializeSession()
   {
      physicsEngine.initialize();
   }

   @Override
   protected void doSpecificRunTick()
   {
      double dt = Conversions.nanosecondsToSeconds(getSessionTickToTimeIncrement());
      physicsEngine.simulate(dt, gravity);
      simulationTime.add(dt);
   }

   public void addRobot(RobotDefinition input)
   {
      addRobot(input, ControllerDefinition.emptyControllerDefinition());
   }

   public void addRobot(RobotDefinition input, ControllerDefinition robotControllerDefinition)
   {
      addRobot(input, robotControllerDefinition, RobotInitialStateProvider.emptyProvider());
   }

   public void addRobot(RobotDefinition input, ControllerDefinition robotControllerDefinition, RobotInitialStateProvider initialStateProvider)
   {
      addRobot(input, robotControllerDefinition, initialStateProvider, new FeatherstoneForwardDynamicsPlugin(), new FirstOrderMultiBodyStateIntegratorPlugin());
   }

   public void addRobot(RobotDefinition input, ControllerDefinition robotControllerDefinition, RobotInitialStateProvider initialStateProvider,
                        RobotPhysicsEnginePlugin... plugins)
   {
      physicsEngine.addRobot(input, robotControllerDefinition, initialStateProvider, plugins);
      sharedBuffer.registerMissingBuffers();
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      physicsEngine.addTerrainObject(terrainObjectDefinition);
   }

   @Override
   public LinkedYoVariableFactory getLinkedYoVariableFactory()
   {
      return sharedBuffer;
   }

   @Override
   public String getSessionName()
   {
      return simulationName;
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return physicsEngine.getRobotDefinitions();
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return physicsEngine.getTerrainObjectDefinitions();
   }

}
