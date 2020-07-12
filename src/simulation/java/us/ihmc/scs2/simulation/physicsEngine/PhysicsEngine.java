package us.ihmc.scs2.simulation.physicsEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoMultiBodySystem;
import us.ihmc.scs2.definition.controller.ControllerInput;
import us.ihmc.scs2.definition.controller.ControllerOutput;
import us.ihmc.scs2.definition.controller.interfaces.Controller;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.interfaces.RobotInitialStateProvider;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollisionTools;
import us.ihmc.scs2.simulation.collision.DefaultCollisionManagerPlugin;
import us.ihmc.yoVariables.registry.YoRegistry;

public class PhysicsEngine
{
   private final ReferenceFrame rootFrame;
   private final YoRegistry parentRegistry;

   private final YoRegistry physicsEngineRegistry = new YoRegistry("PhysicsPlugins");
   private final List<RobotPhysicsEngine> robotPhysicsEngineList = new ArrayList<>();
   private EnvironmentPhysicsEnginePlugin environmentPlugin;

   private final List<Collidable> environmentShapes = new ArrayList<>();
   private final List<TerrainObjectDefinition> terrainObjectDefinitions = new ArrayList<>();

   private boolean initialize = true;

   public PhysicsEngine(ReferenceFrame rootFrame, YoRegistry parentRegistry)
   {
      this.rootFrame = rootFrame;
      this.parentRegistry = parentRegistry;
      environmentPlugin = new DefaultCollisionManagerPlugin(rootFrame);
      parentRegistry.addChild(physicsEngineRegistry);
      physicsEngineRegistry.addChild(environmentPlugin.getYoRegistry());
   }

   public void addRobot(RobotDefinition input, ControllerDefinition robotControllerDefinition, RobotInitialStateProvider initialStateProvider,
                        RobotPhysicsEnginePlugin... plugins)
   {
      RobotPhysicsEngine robotPhysicsEngine = new RobotPhysicsEngine(input, robotControllerDefinition, initialStateProvider, rootFrame);

      if (plugins != null && plugins.length > 0)
      {
         for (RobotPhysicsEnginePlugin plugin : plugins)
            robotPhysicsEngine.addRobotPhysicsPlugin(plugin);
      }
      else
      {
         robotPhysicsEngine.addRobotPhysicsPlugin(new FeatherstoneForwardDynamicsPlugin());
         robotPhysicsEngine.addRobotPhysicsPlugin(new FirstOrderMultiBodyStateIntegratorPlugin());
      }

      parentRegistry.addChild(robotPhysicsEngine.getRobotRegistry());
      robotPhysicsEngineList.add(robotPhysicsEngine);
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      terrainObjectDefinitions.add(terrainObjectDefinition);
      environmentShapes.addAll(CollisionTools.toCollisionShape(terrainObjectDefinition, rootFrame));
   }

   public void initialize()
   {
      if (!initialize)
         return;

      robotPhysicsEngineList.forEach(RobotPhysicsEngine::initialize);
      for (RobotPhysicsEngine robotPhysicsEngine : robotPhysicsEngineList)
      {
         robotPhysicsEngine.initialize();
      }

      initialize = false;
   }

   public void simulate(double dt, Vector3DReadOnly gravity)
   {
      for (RobotPhysicsEngine robotPhysicsEngine : robotPhysicsEngineList)
      {
         robotPhysicsEngine.doControl();
      }

      environmentPlugin.submitWorldElements(robotPhysicsEngineList, environmentShapes);
      environmentPlugin.doScience(dt, gravity);

      for (RobotPhysicsEngine robotPhysicsEngine : robotPhysicsEngineList)
      {
         ExternalInteractionProvider robotInteractions = environmentPlugin.getRobotInteractions(robotPhysicsEngine);
         robotPhysicsEngine.submitExternalInteractions(robotInteractions);
         robotPhysicsEngine.doScience(dt, gravity);
      }
   }

   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotPhysicsEngineList.stream().map(RobotPhysicsEngine::getRobotDefinition).collect(Collectors.toList());
   }

   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return terrainObjectDefinitions;
   }

   public static class RobotPhysicsEngine
   {
      private final RobotDefinition robotDefinition;
      private final ControllerDefinition controllerDefinition;
      private final RobotInitialStateProvider initialStateProvider;

      private final YoRegistry robotRegistry;
      private final YoMultiBodySystem multiBodySystem;
      private final ControllerInput controllerInput;
      private final ControllerOutput controllerOutput;
      private final Controller controller;
      private final List<RobotPhysicsEnginePlugin> robotPlugins = new ArrayList<>();
      private final List<Collidable> collidables;
      private ExternalInteractionProvider externalInteractionProvider;

      public RobotPhysicsEngine(RobotDefinition robotDefinition, ControllerDefinition controllerDefinition, RobotInitialStateProvider robotInitialStateProvider,
                                ReferenceFrame rootFrame)
      {
         this.robotDefinition = robotDefinition;
         this.controllerDefinition = controllerDefinition != null ? controllerDefinition : ControllerDefinition.emptyControllerDefinition();
         this.initialStateProvider = robotInitialStateProvider != null ? robotInitialStateProvider : RobotInitialStateProvider.emptyProvider();

         String name = robotDefinition.getName();
         robotRegistry = new YoRegistry(name);
         multiBodySystem = new YoMultiBodySystem(robotDefinition.toMultiBodySystemBasics(rootFrame), rootFrame, robotRegistry);
         controllerInput = new ControllerInput(multiBodySystem);
         controllerOutput = new ControllerOutput(multiBodySystem);

         controller = controllerDefinition.newController(controllerInput, controllerOutput);

         collidables = CollisionTools.extractCollidableRigidBodies(robotDefinition, multiBodySystem.getRootBody());

         if (controller.getYoRegistry() != null)
            robotRegistry.addChild(controller.getYoRegistry());
      }

      public void addRobotPhysicsPlugin(RobotPhysicsEnginePlugin plugin)
      {
         plugin.setMultiBodySystem(multiBodySystem);

         if (plugin.getYoRegistry() != null)
            robotRegistry.addChild(plugin.getYoRegistry());
         robotPlugins.add(plugin);
      }

      public void initialize()
      {
         for (JointBasics joint : multiBodySystem.getAllJoints())
         {
            JointStateReadOnly initialJointState = initialStateProvider.getInitialJointState(joint.getName());
            if (initialJointState == null)
               continue;
            if (initialJointState.hasOutputFor(JointStateType.CONFIGURATION))
               initialJointState.getConfiguration(joint);
            if (initialJointState.hasOutputFor(JointStateType.VELOCITY))
               initialJointState.getVelocity(joint);
            if (initialJointState.hasOutputFor(JointStateType.ACCELERATION))
               initialJointState.getAcceleration(joint);
            if (initialJointState.hasOutputFor(JointStateType.EFFORT))
               initialJointState.getEffort(joint);
         }
         multiBodySystem.getRootBody().updateFramesRecursively();
         robotPlugins.forEach(PhysicsEnginePlugin::initialize);
      }

      public void doControl()
      {
         controller.doControl();
      }

      public void submitExternalInteractions(ExternalInteractionProvider externalInteractionProvider)
      {
         this.externalInteractionProvider = externalInteractionProvider;
      }

      public void doScience(double dt, Vector3DReadOnly gravity)
      {
         for (RobotPhysicsEnginePlugin plugin : robotPlugins)
         {
            plugin.submitExternalInteractions(externalInteractionProvider);
            plugin.submitControllerOutput(controllerOutput);
            plugin.doScience(dt, gravity);
         }
         multiBodySystem.getRootBody().updateFramesRecursively();
      }

      public RobotDefinition getRobotDefinition()
      {
         return robotDefinition;
      }

      public ControllerDefinition getControllerDefinition()
      {
         return controllerDefinition;
      }

      public List<Collidable> getCollidables()
      {
         return collidables;
      }

      public YoRegistry getRobotRegistry()
      {
         return robotRegistry;
      }
   }
}
