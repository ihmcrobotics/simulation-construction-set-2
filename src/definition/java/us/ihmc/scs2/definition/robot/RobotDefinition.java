package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;

public class RobotDefinition
{
   private String name;
   private RigidBodyDefinition rootBodyDefinition;
   private List<JointDefinition> definitionsOfJointsToIgnore = new ArrayList<>();

   public RobotDefinition()
   {
   }

   public RobotDefinition(String name)
   {
      setName(name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setRootBodyDefinition(RigidBodyDefinition rootBodyDefinition)
   {
      this.rootBodyDefinition = rootBodyDefinition;
   }

   public String getName()
   {
      return name;
   }

   public RigidBodyDefinition getRootBodyDefinition()
   {
      return rootBodyDefinition;
   }

   public List<JointDefinition> getRootJointDefinitions()
   {
      return rootBodyDefinition.getChildrenJoints();
   }

   public List<JointDefinition> getDefinitionsOfJointsToIgnore()
   {
      return definitionsOfJointsToIgnore;
   }

   public JointDefinition getJointDefinition(String jointName)
   {
      return getAllJoints().stream().filter(joint -> joint.getName().equals(jointName)).findFirst().orElse(null);
   }

   public RigidBodyDefinition getRigidBodyDefinition(String bodyName)
   {
      if (rootBodyDefinition.getName().equals(bodyName))
         return rootBodyDefinition;
      return getAllJoints().stream().map(JointDefinition::getSuccessor).filter(rigidBody -> rigidBody.getName().equals(bodyName)).findFirst().orElse(null);
   }

   public List<JointDefinition> getAllJoints()
   {
      return collectSubtreeJointDefinitions(rootBodyDefinition);
   }

   private static List<JointDefinition> collectSubtreeJointDefinitions(RigidBodyDefinition start)
   {
      if (start == null)
         return Collections.emptyList();

      List<JointDefinition> joints = new ArrayList<>();

      for (JointDefinition childJoint : start.getChildrenJoints())
      {
         joints.add(childJoint);
         joints.addAll(collectSubtreeJointDefinitions(childJoint.getSuccessor()));
      }

      return joints;
   }

   public MultiBodySystemBasics toMultiBodySystemBasics(ReferenceFrame rootFrame)
   {
      RigidBodyBasics rootBody = newIntance(rootFrame);
      Set<String> namesOfJointsToIgnore = definitionsOfJointsToIgnore.stream().map(JointDefinition::getName).collect(Collectors.toSet());
      List<JointBasics> jointsToIgnore = SubtreeStreams.fromChildren(rootBody).filter(joint -> namesOfJointsToIgnore.contains(joint.getName()))
                                                       .collect(Collectors.toList());
      return MultiBodySystemBasics.toMultiBodySystemBasics(rootBody, jointsToIgnore);
   }

   public RigidBodyBasics newIntance(ReferenceFrame rootFrame)
   {
      if (rootBodyDefinition == null)
         throw new NullPointerException("The robot " + name + " has no definition!");
      RigidBodyBasics rootBody = rootBodyDefinition.toRootBody(rootFrame);
      instantiateRecursively(rootBody, rootBodyDefinition);
      return rootBody;
   }

   private void instantiateRecursively(RigidBodyBasics predecessor, RigidBodyDefinition predecessorDefinition)
   {
      for (JointDefinition childDefinition : predecessorDefinition.getChildrenJoints())
      {
         JointBasics child = childDefinition.toJoint(predecessor);
         RigidBodyDefinition successorDefinition = childDefinition.getSuccessor();

         if (successorDefinition == null)
            throw new NullPointerException("The joint " + child.getName() + " is missing the definition for its successor, robot name: " + name + ".");

         RigidBodyBasics successor = successorDefinition.toRigidBody(child);
         instantiateRecursively(successor, successorDefinition);
      }
   }
}
