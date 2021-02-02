package us.ihmc.scs2.simulation.robot;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.JointMatrixIndexProvider;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.scs2.definition.robot.FixedJointDefinition;
import us.ihmc.scs2.definition.robot.JointDefinition;
import us.ihmc.scs2.definition.robot.PlanarJointDefinition;
import us.ihmc.scs2.definition.robot.PrismaticJointDefinition;
import us.ihmc.scs2.definition.robot.RevoluteJointDefinition;
import us.ihmc.scs2.definition.robot.RigidBodyDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.SixDoFJointDefinition;

public class Robot implements MultiBodySystemBasics
{
   public static final JointBuilderFromDefinition DEFAULT_JOINT_BUILDER = new JointBuilderFromDefinition()
   {
   };
   public static final RigidBodyBuilderFromDefinition DEFAULT_BODY_BUILDER = new RigidBodyBuilderFromDefinition()
   {
   };

   private final String name;
   private final SimRigidBody rootBody;
   private final ReferenceFrame inertialFrame;

   private final Map<String, SimJointBasics> nameToJointMap;
   private final Map<String, SimRigidBody> nameToBodyMap;
   private final List<SimJointBasics> allJoints;
   private final List<SimJointBasics> jointsToIgnore;
   private final List<SimJointBasics> jointsToConsider;
   private final JointMatrixIndexProvider jointMatrixIndexProvider;

   public Robot(RobotDefinition robotDefinition, ReferenceFrame inertialFrame)
   {
      this.inertialFrame = inertialFrame;

      name = robotDefinition.getName();

      rootBody = createRobot(robotDefinition.getRootBodyDefinition(), inertialFrame, DEFAULT_JOINT_BUILDER, DEFAULT_BODY_BUILDER);
      nameToJointMap = SubtreeStreams.fromChildren(SimJointBasics.class, rootBody).collect(Collectors.toMap(SimJointBasics::getName, Function.identity()));
      nameToBodyMap = rootBody.subtreeStream().collect(Collectors.toMap(SimRigidBody::getName, Function.identity()));
      allJoints = SubtreeStreams.fromChildren(SimJointBasics.class, rootBody).collect(Collectors.toList());
      jointsToIgnore = robotDefinition.getDefinitionsOfJointsToIgnore().stream().map(def -> nameToJointMap.get(def.getName())).collect(Collectors.toList());
      jointsToConsider = allJoints.stream().filter(joint -> !jointsToIgnore.contains(joint)).collect(Collectors.toList());
      jointMatrixIndexProvider = JointMatrixIndexProvider.toIndexProvider(getJointsToConsider());
   }

   public static SimRigidBody createRobot(RigidBodyDefinition rootBodyDefinition, ReferenceFrame inertialFrame, JointBuilderFromDefinition jointBuilder,
                                          RigidBodyBuilderFromDefinition bodyBuilder)
   {
      SimRigidBody rootBody = bodyBuilder.rootFromDefinition(rootBodyDefinition, inertialFrame);
      createJointsRecursive(rootBody, rootBodyDefinition, jointBuilder, bodyBuilder);
      return rootBody;
   }

   public static void createJointsRecursive(SimRigidBody rigidBody, RigidBodyDefinition rigidBodyDefinition, JointBuilderFromDefinition jointBuilder,
                                            RigidBodyBuilderFromDefinition bodyBuilder)
   {
      for (JointDefinition childJointDefinition : rigidBodyDefinition.getChildrenJoints())
      {
         SimJointBasics childJoint = jointBuilder.fromDefinition(childJointDefinition, rigidBody);
         SimRigidBody childSuccessor = bodyBuilder.fromDefinition(childJointDefinition.getSuccessor(), childJoint);
         createJointsRecursive(childSuccessor, childJointDefinition.getSuccessor(), jointBuilder, bodyBuilder);
      }
   }

   public String getName()
   {
      return name;
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
      default SimRigidBody rootFromDefinition(RigidBodyDefinition rootBodyDefinition, ReferenceFrame inertialFrame)
      {
         return new SimRigidBody(rootBodyDefinition, inertialFrame);
      }

      default SimRigidBody fromDefinition(RigidBodyDefinition rigidBodyDefinition, SimJointBasics parentJoint)
      {
         return new SimRigidBody(rigidBodyDefinition, parentJoint);
      }
   }
}
