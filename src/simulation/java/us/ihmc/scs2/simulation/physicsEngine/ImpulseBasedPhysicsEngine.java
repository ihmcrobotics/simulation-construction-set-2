package us.ihmc.scs2.simulation.physicsEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.YoTimer;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollisionTools;
import us.ihmc.scs2.simulation.parameters.ConstraintParametersReadOnly;
import us.ihmc.scs2.simulation.parameters.ContactParametersReadOnly;
import us.ihmc.scs2.simulation.parameters.YoConstraintParameters;
import us.ihmc.scs2.simulation.parameters.YoContactParameters;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;

/**
 * Physics engine that simulates the dynamic behavior of multiple robots and their contact
 * interactions.
 * <p>
 * Its uses Featherstone forward dynamics algorithm to account for controller outputs, i.e. joint
 * efforts. The interactions are simulated using an impulse-based framework.
 * </p>
 * <p>
 * References that this physics engine is based on:
 * <ul>
 * <li>Multi-body forward dynamics algorithm: Featherstone, Roy. <i>Rigid Body Dynamics
 * Algorithms</i> Springer, 2008.
 * <li>Multi-body impulse response algorithm: Mirtich, Brian Vincent. <i>Impulse-based dynamic
 * simulation of rigid body systems</i>. University of California, Berkeley, 1996.
 * <li>Impulse-based contact resolution algorithm: Hwangbo, Jemin, Joonho Lee, and Marco Hutter.
 * <i>Per-contact iteration method for solving contact dynamics.</i> IEEE Robotics and Automation
 * Letters 3.2 (2018): 895-902.
 * </ul>
 * </p>
 *
 * @author Sylvain Bertrand
 */
public class ImpulseBasedPhysicsEngine implements PhysicsEngine
{
   private final ReferenceFrame inertialFrame;

   private final YoRegistry rootRegistry;
   private final YoRegistry physicsEngineRegistry = new YoRegistry(getClass().getSimpleName());
   private final List<Robot> robotList = new ArrayList<>();
   private final Map<RigidBodyBasics, Robot> robotMap = new HashMap<>();
   private final YoMultiContactImpulseCalculatorPool multiContactImpulseCalculatorPool;

   private List<MultiRobotCollisionGroup> collisionGroups;

   private final List<TerrainObjectDefinition> terrainObjectDefinitions = new ArrayList<>();
   private final List<Collidable> environmentCollidables = new ArrayList<>();

   private final SimpleCollisionDetection collisionDetectionPlugin;

   private final YoBoolean hasGlobalContactParameters;
   private final YoContactParameters globalContactParameters;
   private final YoBoolean hasGlobalConstraintParameters;
   private final YoConstraintParameters globalConstraintParameters;

   private final YoDouble time = new YoDouble("physicsTime", physicsEngineRegistry);
   private final YoTimer physicsEngineTotalTimer = new YoTimer("physicsEngineTotalTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoDouble physicsEngineRealTimeRate = new YoDouble("physicsEngineRealTimeRate", physicsEngineRegistry);

   private boolean initialize = true;

   public ImpulseBasedPhysicsEngine(ReferenceFrame inertialFrame, YoRegistry rootRegistry)
   {
      this.rootRegistry = rootRegistry;
      this.inertialFrame = inertialFrame;

      rootRegistry.addChild(physicsEngineRegistry);

      collisionDetectionPlugin = new SimpleCollisionDetection(inertialFrame);

      YoRegistry multiContactCalculatorRegistry = new YoRegistry(MultiContactImpulseCalculator.class.getSimpleName());
      physicsEngineRegistry.addChild(multiContactCalculatorRegistry);

      hasGlobalContactParameters = new YoBoolean("hasGlobalContactParameters", physicsEngineRegistry);
      globalContactParameters = new YoContactParameters("globalContact", physicsEngineRegistry);
      hasGlobalConstraintParameters = new YoBoolean("hasGlobalConstraintParameters", physicsEngineRegistry);
      globalConstraintParameters = new YoConstraintParameters("globalConstraint", physicsEngineRegistry);
      multiContactImpulseCalculatorPool = new YoMultiContactImpulseCalculatorPool(1, inertialFrame, multiContactCalculatorRegistry);
   }

   @Override
   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      terrainObjectDefinitions.add(terrainObjectDefinition);
      environmentCollidables.addAll(CollisionTools.toCollisionShape(terrainObjectDefinition, inertialFrame));
   }

   @Override
   public void addRobot(Robot robot)
   {
      robot.setupPhysicsAndControllers();
      robotMap.put(robot.getRootBody(), robot);
      rootRegistry.addChild(robot.getRegistry());
      robotList.add(robot);
   }

   public void setGlobalConstraintParameters(ConstraintParametersReadOnly parameters)
   {
      globalConstraintParameters.set(parameters);
      hasGlobalConstraintParameters.set(true);
   }

   public void setGlobalContactParameters(ContactParametersReadOnly parameters)
   {
      globalContactParameters.set(parameters);
      hasGlobalContactParameters.set(true);
   }

   @Override
   public boolean initialize(Vector3DReadOnly gravity)
   {
      if (!initialize)
         return false;

      for (Robot robot : robotList)
      {
         robot.initializeState();
         robot.getRobotPhysics().resetCalculators();
         // Fill out the joint accelerations so the accelerometers can get initialized.
         robot.getRobotPhysics().doForwardDynamics(gravity);
         robot.updateSensors();
         robot.getControllerManager().initializeControllers();
      }
      initialize = false;
      return true;
   }

   private final YoTimer initialPhaseTimer = new YoTimer("initialPhaseTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer detectCollisionsTimer = new YoTimer("detectCollisionsTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer configureCollisionHandlersTimer = new YoTimer("configureCollisionHandlersTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer handleCollisionsTimer = new YoTimer("handleCollisionsTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer finalPhaseTimer = new YoTimer("finalPhaseTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);

   @Override
   public void simulate(double dt, Vector3DReadOnly gravity)
   {
      if (initialize(gravity))
         return;

      physicsEngineTotalTimer.start();
      initialPhaseTimer.start();

      for (Robot robot : robotList)
      {
         robot.getRobotPhysics().resetCalculators();
         robot.getControllerManager().updateControllers(time.getValue());
         robot.getRobotPhysics().updateCollidableBoundingBoxes();
      }

      for (Robot robot : robotList)
      {
         robot.getControllerManager().writeControllerOutput(JointStateType.EFFORT);
         robot.getRobotPhysics().doForwardDynamics(gravity);
      }

      environmentCollidables.forEach(collidable -> collidable.updateBoundingBox(inertialFrame));
      initialPhaseTimer.stop();
      detectCollisionsTimer.start();

      if (hasGlobalContactParameters.getValue())
         collisionDetectionPlugin.setMinimumPenetration(globalContactParameters.getMinimumPenetration());
      collisionDetectionPlugin.evaluationCollisions(robotList, () -> environmentCollidables, dt);

      collisionGroups = MultiRobotCollisionGroup.toCollisionGroups(collisionDetectionPlugin.getAllCollisions());

      detectCollisionsTimer.stop();
      configureCollisionHandlersTimer.start();

      Set<RigidBodyBasics> uncoveredRobotsRootBody = new HashSet<>(robotMap.keySet());
      List<MultiContactImpulseCalculator> impulseCalculators = new ArrayList<>();

      multiContactImpulseCalculatorPool.clear();

      for (MultiRobotCollisionGroup collisionGroup : collisionGroups)
      {
         MultiContactImpulseCalculator calculator = multiContactImpulseCalculatorPool.nextAvailable();

         calculator.configure(robotMap, collisionGroup);

         if (hasGlobalConstraintParameters.getValue())
            calculator.setConstraintParameters(globalConstraintParameters);
         if (hasGlobalContactParameters.getValue())
            calculator.setContactParameters(globalContactParameters);

         impulseCalculators.add(calculator);
         uncoveredRobotsRootBody.removeAll(collisionGroup.getRootBodies());
      }

      configureCollisionHandlersTimer.stop();
      handleCollisionsTimer.start();

      for (RigidBodyBasics rootBody : uncoveredRobotsRootBody)
      {
         Robot robot = robotMap.get(rootBody);
         RobotJointLimitImpulseBasedCalculator jointLimitConstraintCalculator = robot.getRobotPhysics().getJointLimitConstraintCalculator();
         jointLimitConstraintCalculator.initialize(dt);
         jointLimitConstraintCalculator.updateInertia(null, null);
         jointLimitConstraintCalculator.computeImpulse(dt);
         robot.getRobotPhysics().addJointVelocityChange(jointLimitConstraintCalculator.getJointVelocityChange(0));
      }

      for (MultiContactImpulseCalculator impulseCalculator : impulseCalculators)
      {
         impulseCalculator.computeImpulses(time.getValue(), dt, false);
         impulseCalculator.writeJointDeltaVelocities();
         impulseCalculator.writeImpulses();
      }

      handleCollisionsTimer.stop();
      finalPhaseTimer.start();

      for (Robot robot : robotList)
      {
         robot.getRobotPhysics().writeJointAccelerations();
         robot.getRobotPhysics().writeJointDeltaVelocities();
         robot.getRobotPhysics().integrateState(dt);
         robot.updateFrames();
         robot.updateSensors();
      }

      time.add(dt);

      finalPhaseTimer.stop();
      physicsEngineTotalTimer.stop();
      physicsEngineRealTimeRate.set((dt * 1.0e3) / physicsEngineTotalTimer.getTimer().getValue());
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotList.stream().map(Robot::getRobotDefinition).collect(Collectors.toList());
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return terrainObjectDefinitions;
   }

   @Override
   public YoRegistry getPhysicsEngineRegistry()
   {
      return rootRegistry;
   }

   @Override
   public ReferenceFrame getInertialFrame()
   {
      return inertialFrame;
   }
}
