package us.ihmc.scs2.simulation.physicsEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gnu.trove.list.linked.TDoubleLinkedList;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollisionTools;
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
public class PhysicsEngine
{
   private final ReferenceFrame inertialFrame;

   private final YoRegistry registry = new YoRegistry("PhysicsPlugins");
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

   private final YoDouble time = new YoDouble("physicsTime", registry);
   private final YoDouble rawTickDurationMilliseconds = new YoDouble("rawTickDurationMilliseconds", registry);
   private final YoDouble averageTickDurationMilliseconds = new YoDouble("averageTickDurationMilliseconds", registry);
   private final YoDouble rawRealTimeRate = new YoDouble("rawRealTimeRate", registry);
   private final YoDouble averageRealTimeRate = new YoDouble("averageRealTimeRate", registry);
   private final int averageWindow = 100;
   private final TDoubleLinkedList rawTickDurationBuffer = new TDoubleLinkedList();

   private boolean initialize = true;

   public PhysicsEngine(ReferenceFrame inertialFrame)
   {
      this(inertialFrame, null);
   }

   public PhysicsEngine(ReferenceFrame inertialFrame, YoRegistry parentRegistry)
   {
      this.inertialFrame = inertialFrame;

      collisionDetectionPlugin = new SimpleCollisionDetection(inertialFrame);

      YoRegistry multiContactCalculatorRegistry = new YoRegistry(MultiContactImpulseCalculator.class.getSimpleName());
      registry.addChild(multiContactCalculatorRegistry);

      hasGlobalContactParameters = new YoBoolean("hasGlobalContactParameters", registry);
      globalContactParameters = new YoContactParameters("globalContact", registry);
      hasGlobalConstraintParameters = new YoBoolean("hasGlobalConstraintParameters", registry);
      globalConstraintParameters = new YoConstraintParameters("globalConstraint", registry);
      multiContactImpulseCalculatorPool = new YoMultiContactImpulseCalculatorPool(1, inertialFrame, multiContactCalculatorRegistry);

      if (parentRegistry != null)
         parentRegistry.addChild(registry);
   }

   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      terrainObjectDefinitions.add(terrainObjectDefinition);
      environmentCollidables.addAll(CollisionTools.toCollisionShape(terrainObjectDefinition, inertialFrame));
   }

   public void addRobot(RobotDefinition robotDefinition)
   {
      addRobot(new Robot(robotDefinition, inertialFrame));
   }

   public void addRobot(Robot robot)
   {
      robotMap.put(robot.getRootBody(), robot);
      registry.addChild(robot.getRegistry());
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

   public boolean initialize()
   {
      if (!initialize)
         return false;

      for (Robot robot : robotList)
      {
         robot.initializeState();
         robot.getControllerManager().initializeControllers();
      }
      initialize = false;
      return true;
   }

   public void simulate(double dt, Vector3DReadOnly gravity)
   {
      if (initialize())
         return;

      long startTick = System.nanoTime();

      for (Robot robot : robotList)
      {
         robot.getRobotPhysics().resetCalculators();
         robot.getControllerManager().updateControllers();
         robot.getRobotPhysics().updateCollidableBoundingBoxes();
      }

      for (Robot robot : robotMap.values())
      {
         SingleRobotForwardDynamicsPlugin forwardDynamicsPlugin = robot.getRobotPhysics().getForwardDynamicsPlugin();
         forwardDynamicsPlugin.resetExternalWrenches();
         robot.getControllerManager().writeControllerOutput(JointStateType.EFFORT);
         forwardDynamicsPlugin.doScience(time.getValue(), dt, gravity);
      }

      environmentCollidables.forEach(collidable -> collidable.updateBoundingBox(inertialFrame));
      if (hasGlobalContactParameters.getValue())
         collisionDetectionPlugin.setMinimumPenetration(globalContactParameters.getMinimumPenetration());
      collisionDetectionPlugin.evaluationCollisions(robotList, () -> environmentCollidables, dt);

      collisionGroups = MultiRobotCollisionGroup.toCollisionGroups(collisionDetectionPlugin.getAllCollisions());

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

      for (RigidBodyBasics rootBody : uncoveredRobotsRootBody)
      {
         Robot robot = robotMap.get(rootBody);
         RobotJointLimitImpulseBasedCalculator jointLimitConstraintCalculator = robot.getRobotPhysics().getJointLimitConstraintCalculator();
         jointLimitConstraintCalculator.initialize(dt);
         jointLimitConstraintCalculator.updateInertia(null, null);
         jointLimitConstraintCalculator.computeImpulse(dt);
         robot.getRobotPhysics().getIntegrator().addJointVelocityChange(jointLimitConstraintCalculator.getJointVelocityChange(0));
      }

      for (MultiContactImpulseCalculator impulseCalculator : impulseCalculators)
      {
         impulseCalculator.computeImpulses(time.getValue(), dt, false);
         impulseCalculator.applyJointVelocityChanges();
      }

      for (Robot robot : robotMap.values())
      {
         SingleRobotForwardDynamicsPlugin forwardDynamicsPlugin = robot.getRobotPhysics().getForwardDynamicsPlugin();
         forwardDynamicsPlugin.writeJointAccelerations();
         robot.getRobotPhysics().getIntegrator().integrate(dt);
      }

      for (int i = 0; i < robotList.size(); i++)
      {
         Robot robot = robotList.get(i);
         robot.updateFrames();
      }

      time.add(dt);

      long endTick = System.nanoTime();

      double dtMilliseconds = dt * 1.0e3;
      double tickDuration = (endTick - startTick) / 1.0e6;
      rawTickDurationMilliseconds.set(tickDuration);
      rawRealTimeRate.set(dtMilliseconds / tickDuration);
      rawTickDurationBuffer.add(tickDuration);

      if (rawTickDurationBuffer.size() >= averageWindow)
      {
         averageTickDurationMilliseconds.set(rawTickDurationBuffer.sum() / averageWindow);
         averageRealTimeRate.set(dtMilliseconds / averageTickDurationMilliseconds.getValue());
         rawTickDurationBuffer.removeAt(0);
      }
   }

   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotList.stream().map(Robot::getRobotDefinition).collect(Collectors.toList());
   }

   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return terrainObjectDefinitions;
   }

   public List<String> getRobotNames()
   {
      return robotList.stream().map(Robot::getName).collect(Collectors.toList());
   }

   public double getTime()
   {
      return time.getValue();
   }

   public YoRegistry getPhysicsEngineRegistry()
   {
      return registry;
   }
}
