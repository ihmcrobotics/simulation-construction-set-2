package us.ihmc.scs2.simulation.robot;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.JointMatrixIndexProvider;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.scs2.definition.robot.IMUSensorDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SensorDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;
import us.ihmc.scs2.definition.robot.WrenchSensorDefinition;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.collision.CollidableHolder;
import us.ihmc.scs2.simulation.robot.controller.RobotControllerManager;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimFixedJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimPlanarJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimPrismaticJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRevoluteJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimRigidBody;
import us.ihmc.scs2.simulation.robot.multiBodySystem.SimSixDoFJoint;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimJointBasics;
import us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces.SimMultiBodySystemBasics;
import us.ihmc.yoVariables.registry.YoRegistry;

public class Robot implements SimMultiBodySystemBasics, CollidableHolder
{
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
   private final List<JointStateReadOnly> allJointInitialStates;

   private final RobotControllerManager controllerManager;

   private final RobotPhysics robotPhysics;

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
      allJointInitialStates = allJoints.stream().map(joint -> robotDefinition.getJointDefinition(joint.getName()).getInitialJointState())
                                       .collect(Collectors.toList());

      controllerManager = new RobotControllerManager(this, registry);
      robotDefinition.getControllerDefinitions().forEach(controllerManager::addController);

      robotPhysics = new RobotPhysics(this);
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
         SimJointBasics childJoint = jointBuilder.fromDefinition(childJointDefinition, rigidBody);
         SimRigidBody childSuccessor = bodyBuilder.fromDefinition(childJointDefinition.getSuccessor(), childJoint);

         childJointDefinition.getKinematicPointDefinitions().forEach(childJoint.getAuxialiryData()::addKinematicPoint);
         childJointDefinition.getExternalWrenchPointDefinitions().forEach(childJoint.getAuxialiryData()::addExternalWrenchPoint);
         childJointDefinition.getGroundContactPointDefinitions().forEach(childJoint.getAuxialiryData()::addGroundContactPoint);
         for (SensorDefinition sensorDefinition : childJointDefinition.getSensorDefinitions())
         {
            if (sensorDefinition instanceof IMUSensorDefinition)
               childJoint.getAuxialiryData().addIMUSensor((IMUSensorDefinition) sensorDefinition);
            else if (sensorDefinition instanceof WrenchSensorDefinition)
               childJoint.getAuxialiryData().addWrenchSensor((WrenchSensorDefinition) sensorDefinition);
            else
               LogTools.warn("Unsupported sensor: " + sensorDefinition);
         }

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

   public void initializeState()
   {
      for (int i = 0; i < allJoints.size(); i++)
      {
         JointStateReadOnly initialState = allJointInitialStates.get(i);
         if (initialState != null)
            initialState.getAllStates(allJoints.get(i));
      }
      rootBody.updateFramesRecursively();
   }

   public RobotPhysics getRobotPhysics()
   {
      return robotPhysics;
   }

   public void updateFrames()
   {
      rootBody.updateFramesRecursively();
   }

   public void updateSensors()
   {
      for (SimJointBasics joint : rootBody.childrenSubtreeIterable())
      {
         joint.getAuxialiryData().update(robotPhysics.getPhysicsOutput());
      }
   }

   @Override
   public List<Collidable> getCollidables()
   {
      return robotPhysics.getCollidables();
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
   public List<? extends SimJointBasics> getJointsToConsider()
   {
      return jointsToConsider;
   }

   @Override
   public List<? extends SimJointBasics> getJointsToIgnore()
   {
      return jointsToIgnore;
   }

   @Override
   public YoRegistry getRegistry()
   {
      return registry;
   }

   public static interface JointBuilderFromDefinition
   {
      default SimJointBasics fromDefinition(JointDefinition definition, SimRigidBody predecessor)
      {
         if (definition instanceof FixedJointDefinition)
            return new SimFixedJoint((FixedJointDefinition) definition, predecessor);
         else if (definition instanceof PlanarJointDefinition)
            return new SimPlanarJoint((PlanarJointDefinition) definition, predecessor);
         else if (definition instanceof SixDoFJointDefinition)
            return new SimSixDoFJoint((SixDoFJointDefinition) definition, predecessor);
         else if (definition instanceof PrismaticJointDefinition)
            return new SimPrismaticJoint((PrismaticJointDefinition) definition, predecessor);
         else if (definition instanceof RevoluteJointDefinition)
            return new SimRevoluteJoint((RevoluteJointDefinition) definition, predecessor);
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

      default SimRigidBody fromDefinition(RigidBodyDefinition rigidBodyDefinition, SimJointBasics parentJoint)
      {
         return new SimRigidBody(rigidBodyDefinition, parentJoint);
      }
   }
}
