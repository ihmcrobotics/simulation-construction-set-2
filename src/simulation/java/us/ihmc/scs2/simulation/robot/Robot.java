package us.ihmc.scs2.simulation.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.JointMatrixIndexProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollidableHolder;
import us.ihmc.scs2.simulation.collision.FrameShapePosePredictor;
import us.ihmc.scs2.simulation.physicsEngine.MultiBodySystemStateWriter;
import us.ihmc.scs2.simulation.physicsEngine.RobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.SingleContactImpulseCalculator;
import us.ihmc.scs2.simulation.physicsEngine.SingleRobotFirstOrderIntegrator;
import us.ihmc.scs2.simulation.physicsEngine.SingleRobotForwardDynamicsPlugin;
import us.ihmc.scs2.simulation.physicsEngine.YoRobotJointLimitImpulseBasedCalculator;
import us.ihmc.scs2.simulation.physicsEngine.YoSingleContactImpulseCalculatorPool;
import us.ihmc.scs2.simulation.robot.controller.RobotControllerManager;
import us.ihmc.yoVariables.registry.YoRegistry;

public class Robot implements MultiBodySystemBasics, CollidableHolder
{
   private static final String ContactCalculatorNameSuffix = SingleContactImpulseCalculator.class.getSimpleName();

   public static final JointBuilderFromDefinition DEFAULT_JOINT_BUILDER = new JointBuilderFromDefinition()
   {
   };
   public static final RigidBodyBuilderFromDefinition DEFAULT_BODY_BUILDER = new RigidBodyBuilderFromDefinition()
   {
   };

   private final YoRegistry registry;

   private final RobotDefinition robotDefinition;
   private final String name;
   private final SimRigidBody rootBody;
   private final ReferenceFrame inertialFrame;

   private final Map<String, SimJointBasics> nameToJointMap;
   private final Map<String, SimRigidBody> nameToBodyMap;
   private final List<SimJointBasics> allJoints;
   private final List<SimJointBasics> jointsToIgnore;
   private final List<SimJointBasics> jointsToConsider;
   private final JointMatrixIndexProvider jointMatrixIndexProvider;

   private final RobotControllerManager controllerManager;

   private MultiBodySystemStateWriter robotInitialStateWriter;
   private final YoRegistry environmentContactCalculatorRegistry = new YoRegistry("Environment" + ContactCalculatorNameSuffix);
   private final YoRegistry interRobotContactCalculatorRegistry = new YoRegistry("InterRobot" + ContactCalculatorNameSuffix);
   private final YoRegistry selfContactCalculatorRegistry = new YoRegistry("Self" + ContactCalculatorNameSuffix);
   private final List<Collidable> collidables;

   // TODO Following fields are specific to the type of engine used, they need interfacing.
   private final SingleRobotForwardDynamicsPlugin forwardDynamicsPlugin;
   private final RobotJointLimitImpulseBasedCalculator jointLimitConstraintCalculator;
   private final YoSingleContactImpulseCalculatorPool environmentContactConstraintCalculatorPool;
   private final YoSingleContactImpulseCalculatorPool selfContactConstraintCalculatorPool;
   private final Map<RigidBodyBasics, YoSingleContactImpulseCalculatorPool> interRobotContactConstraintCalculatorPools = new HashMap<>();

   private final SingleRobotFirstOrderIntegrator integrator;

   public Robot(RobotDefinition robotDefinition, ReferenceFrame inertialFrame)
   {
      this.robotDefinition = robotDefinition;
      this.inertialFrame = inertialFrame;

      name = robotDefinition.getName();

      registry = new YoRegistry(name);

      rootBody = createRobot(robotDefinition.getRootBodyDefinition(), inertialFrame, DEFAULT_JOINT_BUILDER, DEFAULT_BODY_BUILDER, registry);
      nameToJointMap = SubtreeStreams.fromChildren(SimJointBasics.class, rootBody).collect(Collectors.toMap(SimJointBasics::getName, Function.identity()));
      nameToBodyMap = rootBody.subtreeStream().collect(Collectors.toMap(SimRigidBody::getName, Function.identity()));
      allJoints = SubtreeStreams.fromChildren(SimJointBasics.class, rootBody).collect(Collectors.toList());
      jointsToIgnore = robotDefinition.getNameOfJointsToIgnore().stream().map(jointName -> nameToJointMap.get(jointName)).collect(Collectors.toList());
      jointsToConsider = allJoints.stream().filter(joint -> !jointsToIgnore.contains(joint)).collect(Collectors.toList());
      jointMatrixIndexProvider = JointMatrixIndexProvider.toIndexProvider(getJointsToConsider());

      controllerManager = new RobotControllerManager(this, registry);
      collidables = rootBody.subtreeStream().flatMap(body -> body.getCollidables().stream()).collect(Collectors.toList());

      forwardDynamicsPlugin = new SingleRobotForwardDynamicsPlugin(this);
      FrameShapePosePredictor frameShapePosePredictor = new FrameShapePosePredictor(forwardDynamicsPlugin.getForwardDynamicsCalculator());
      collidables.forEach(collidable -> collidable.setFrameShapePosePredictor(frameShapePosePredictor));

      YoRegistry jointLimitConstraintCalculatorRegistry = new YoRegistry(RobotJointLimitImpulseBasedCalculator.class.getSimpleName());
      registry.addChild(jointLimitConstraintCalculatorRegistry);

      jointLimitConstraintCalculator = new YoRobotJointLimitImpulseBasedCalculator(rootBody,
                                                                                   forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                   jointLimitConstraintCalculatorRegistry);

      registry.addChild(environmentContactCalculatorRegistry);
      registry.addChild(interRobotContactCalculatorRegistry);
      registry.addChild(selfContactCalculatorRegistry);

      environmentContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(20,
                                                                                            name + "Single",
                                                                                            inertialFrame,
                                                                                            rootBody,
                                                                                            forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                            null,
                                                                                            null,
                                                                                            environmentContactCalculatorRegistry);

      selfContactConstraintCalculatorPool = new YoSingleContactImpulseCalculatorPool(8,
                                                                                     name + "Self",
                                                                                     inertialFrame,
                                                                                     rootBody,
                                                                                     forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                     rootBody,
                                                                                     forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                                     selfContactCalculatorRegistry);

      integrator = new SingleRobotFirstOrderIntegrator(this);
   }

   public static SimRigidBody createRobot(RigidBodyDefinition rootBodyDefinition, ReferenceFrame inertialFrame, JointBuilderFromDefinition jointBuilder,
                                          RigidBodyBuilderFromDefinition bodyBuilder, YoRegistry registry)
   {
      SimRigidBody rootBody = bodyBuilder.rootFromDefinition(rootBodyDefinition, inertialFrame, registry);
      createJointsRecursive(rootBody, rootBodyDefinition, jointBuilder, bodyBuilder, registry);
      return rootBody;
   }

   public static void createJointsRecursive(SimRigidBody rigidBody, RigidBodyDefinition rigidBodyDefinition, JointBuilderFromDefinition jointBuilder,
                                            RigidBodyBuilderFromDefinition bodyBuilder, YoRegistry registry)
   {
      for (JointDefinition childJointDefinition : rigidBodyDefinition.getChildrenJoints())
      {
         SimJointBasics childJoint = jointBuilder.fromDefinition(childJointDefinition, rigidBody, registry);
         SimRigidBody childSuccessor = bodyBuilder.fromDefinition(childJointDefinition.getSuccessor(), childJoint, registry);
         createJointsRecursive(childSuccessor, childJointDefinition.getSuccessor(), jointBuilder, bodyBuilder, registry);
      }
   }

   public String getName()
   {
      return name;
   }

   public RobotDefinition getRobotDefinition()
   {
      return robotDefinition;
   }

   public RobotControllerManager getControllerManager()
   {
      return controllerManager;
   }

   public void setRobotInitialStateWriter(MultiBodySystemStateWriter robotInitialStateWriter)
   {
      this.robotInitialStateWriter = robotInitialStateWriter;
      this.robotInitialStateWriter.setMultiBodySystem(this);
   }

   public void initializeState()
   {
      if (robotInitialStateWriter != null)
      {
         robotInitialStateWriter.write();
         rootBody.updateFramesRecursively();
      }
   }

   public void updateCollidableBoundingBoxes()
   {
      collidables.forEach(collidable -> collidable.updateBoundingBox(inertialFrame));
   }

   public void updateFrames()
   {
      rootBody.updateFramesRecursively();
   }

   @Override
   public List<Collidable> getCollidables()
   {
      return collidables;
   }

   public SingleRobotForwardDynamicsPlugin getForwardDynamicsPlugin()
   {
      return forwardDynamicsPlugin;
   }

   public SingleRobotFirstOrderIntegrator getIntegrator()
   {
      return integrator;
   }

   public void resetCalculators()
   {
      environmentContactConstraintCalculatorPool.clear();
      selfContactConstraintCalculatorPool.clear();
      interRobotContactConstraintCalculatorPools.forEach((rigidBodyBasics, calculators) -> calculators.clear());
      integrator.reset();
   }

   public RobotJointLimitImpulseBasedCalculator getJointLimitConstraintCalculator()
   {
      return jointLimitConstraintCalculator;
   }

   public SingleContactImpulseCalculator getOrCreateEnvironmentContactConstraintCalculator()
   {
      return environmentContactConstraintCalculatorPool.nextAvailable();
   }

   public SingleContactImpulseCalculator getOrCreateSelfContactConstraintCalculator()
   {
      return selfContactConstraintCalculatorPool.nextAvailable();
   }

   public SingleContactImpulseCalculator getOrCreateInterRobotContactConstraintCalculator(Robot otherRobot)
   {
      if (otherRobot == null)
         return getOrCreateEnvironmentContactConstraintCalculator();
      if (otherRobot == this)
         return getOrCreateSelfContactConstraintCalculator();

      YoSingleContactImpulseCalculatorPool calculators = interRobotContactConstraintCalculatorPools.get(otherRobot.getRootBody());

      if (calculators == null)
      {
         calculators = new YoSingleContactImpulseCalculatorPool(8,
                                                                name + otherRobot.getName() + "Dual",
                                                                inertialFrame,
                                                                rootBody,
                                                                forwardDynamicsPlugin.getForwardDynamicsCalculator(),
                                                                otherRobot.getRootBody(),
                                                                otherRobot.getForwardDynamicsPlugin().getForwardDynamicsCalculator(),
                                                                interRobotContactCalculatorRegistry);
         interRobotContactConstraintCalculatorPools.put(otherRobot.getRootBody(), calculators);
      }

      return calculators.nextAvailable();
   }

   @Override
   public SimRigidBody getRootBody()
   {
      return rootBody;
   }

   public SimRigidBody getRigidBody(String name)
   {
      return nameToBodyMap.get(name);
   }

   public SimJointBasics getJoint(String name)
   {
      return nameToJointMap.get(name);
   }

   @Override
   public List<? extends SimJointBasics> getAllJoints()
   {
      return allJoints;
   }

   @Override
   public ReferenceFrame getInertialFrame()
   {
      return inertialFrame;
   }

   @Override
   public JointMatrixIndexProvider getJointMatrixIndexProvider()
   {
      return jointMatrixIndexProvider;
   }

   @Override
   public List<? extends JointBasics> getJointsToConsider()
   {
      return jointsToConsider;
   }

   @Override
   public List<? extends JointBasics> getJointsToIgnore()
   {
      return jointsToIgnore;
   }

   public YoRegistry getRegistry()
   {
      return registry;
   }

   public static interface JointBuilderFromDefinition
   {
      default SimJointBasics fromDefinition(JointDefinition definition, SimRigidBody predecessor, YoRegistry registry)
      {
         if (definition instanceof FixedJointDefinition)
            return new SimFixedJoint((FixedJointDefinition) definition, predecessor, registry);
         else if (definition instanceof PlanarJointDefinition)
            return new SimPlanarJoint((PlanarJointDefinition) definition, predecessor, registry);
         else if (definition instanceof SixDoFJointDefinition)
            return new SimSixDoFJoint((SixDoFJointDefinition) definition, predecessor, registry);
         else if (definition instanceof PrismaticJointDefinition)
            return new SimPrismaticJoint((PrismaticJointDefinition) definition, predecessor, registry);
         else if (definition instanceof RevoluteJointDefinition)
            return new SimRevoluteJoint((RevoluteJointDefinition) definition, predecessor, registry);
         else
            throw new UnsupportedOperationException("Unsupported joint definition: " + definition.getClass().getSimpleName());
      }
   }

   public static interface RigidBodyBuilderFromDefinition
   {
      default SimRigidBody rootFromDefinition(RigidBodyDefinition rootBodyDefinition, ReferenceFrame inertialFrame, YoRegistry registry)
      {
         return new SimRigidBody(rootBodyDefinition, inertialFrame, registry);
      }

      default SimRigidBody fromDefinition(RigidBodyDefinition rigidBodyDefinition, SimJointBasics parentJoint, YoRegistry registry)
      {
         return new SimRigidBody(rigidBodyDefinition, parentJoint, registry);
      }
   }
}
