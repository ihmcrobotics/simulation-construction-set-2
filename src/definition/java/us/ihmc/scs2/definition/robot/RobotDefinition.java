package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;

public class RobotDefinition
{
   private String name;
   private RigidBodyDefinition rootBodyDefinition;
   private List<String> nameOfJointsToIgnore = new ArrayList<>();

   private final List<ControllerDefinition> controllerDefinitions = new ArrayList<>();

   private ClassLoader resourceClassLoader;

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

   public void ignoreAllJoints()
   {
      for (JointDefinition jointDefinition : collectSubtreeJointDefinitions(rootBodyDefinition))
      {
         addJointToIgnore(jointDefinition.getName());
      }
   }

   public void addJointToIgnore(String nameOfJointToIgnore)
   {
      nameOfJointsToIgnore.add(nameOfJointToIgnore);
   }

   public void addSubtreeJointsToIgnore(String nameOfLastJointToConsider)
   {
      List<JointDefinition> definitionOfJointsToIgnore = collectSubtreeJointDefinitions(getJointDefinition(nameOfLastJointToConsider).getSuccessor());

      for (JointDefinition jointDefinition : definitionOfJointsToIgnore)
      {
         nameOfJointsToIgnore.add(jointDefinition.getName());
      }
   }

   public void addControllerDefinition(ControllerDefinition controllerDefinition)
   {
      controllerDefinitions.add(controllerDefinition);
   }

   public void setResourceClassLoader(ClassLoader resourceClassLoader)
   {
      this.resourceClassLoader = resourceClassLoader;
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

   public List<String> getNameOfJointsToIgnore()
   {
      return nameOfJointsToIgnore;
   }

   public ClassLoader getResourceClassLoader()
   {
      return resourceClassLoader;
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

   public List<RigidBodyDefinition> getAllRigidBodies()
   {
      return collectSubtreeRigidBodyDefinitions(rootBodyDefinition);
   }

   public List<ControllerDefinition> getControllerDefinitions()
   {
      return controllerDefinitions;
   }

   private static List<JointDefinition> collectSubtreeJointDefinitions(RigidBodyDefinition start)
   {
      return collectSubtreeJointDefinitions(start, new ArrayList<>());
   }

   private static List<JointDefinition> collectSubtreeJointDefinitions(RigidBodyDefinition start, List<JointDefinition> jointsToPack)
   {
      if (start == null)
         return Collections.emptyList();

      for (JointDefinition childJoint : start.getChildrenJoints())
      {
         jointsToPack.add(childJoint);
         collectSubtreeJointDefinitions(childJoint.getSuccessor(), jointsToPack);
      }

      return jointsToPack;
   }

   private static List<RigidBodyDefinition> collectSubtreeRigidBodyDefinitions(RigidBodyDefinition start)
   {
      return collectSubtreeRigidBodyDefinitions(start, new ArrayList<>());
   }

   private static List<RigidBodyDefinition> collectSubtreeRigidBodyDefinitions(RigidBodyDefinition start, List<RigidBodyDefinition> rigidBodiesToPack)
   {
      if (start == null)
         return Collections.emptyList();

      rigidBodiesToPack.add(start);

      for (JointDefinition childJoint : start.getChildrenJoints())
      {
         collectSubtreeRigidBodyDefinitions(childJoint.getSuccessor(), rigidBodiesToPack);
      }

      return rigidBodiesToPack;
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
