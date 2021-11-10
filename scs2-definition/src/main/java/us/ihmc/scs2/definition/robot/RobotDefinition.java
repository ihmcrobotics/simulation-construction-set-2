package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.tools.MultiBodySystemTools;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;

@XmlRootElement(name = "Robot")
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

   public RobotDefinition(RobotDefinition other)
   {
      name = other.name;
      rootBodyDefinition = other.rootBodyDefinition == null ? null : other.rootBodyDefinition.copyRecursive();
      for (JointDefinition jointDefinition : getAllJoints())
      {
         if (!jointDefinition.isLoopClosure())
            continue;
         String successorName = other.getJointDefinition(jointDefinition.getName()).getSuccessor().getName();
         RigidBodyDefinition rigidBodyDefinition = getRigidBodyDefinition(successorName);
         jointDefinition.setLoopClosureSuccessor(rigidBodyDefinition);
      }
      nameOfJointsToIgnore.addAll(other.nameOfJointsToIgnore);
      resourceClassLoader = other.resourceClassLoader;
   }

   @XmlAttribute
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlElement(name = "rootBody")
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

   @XmlElement(name = "jointToIgnore")
   public void setNameOfJointsToIgnore(List<String> nameOfJointsToIgnore)
   {
      this.nameOfJointsToIgnore = nameOfJointsToIgnore;
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

   @XmlTransient
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
      return findJointDefinition(rootBodyDefinition, jointName);
   }

   public RigidBodyDefinition getRigidBodyDefinition(String bodyName)
   {
      return findRigidBodyDefinition(rootBodyDefinition, bodyName);
   }

   public void forEachJointDefinition(Consumer<JointDefinition> jointConsumer)
   {
      forEachJointDefinition(rootBodyDefinition, jointConsumer);
   }

   public void forEachJointDefinitionLazy(Predicate<JointDefinition> searchDoneCriteria)
   {
      forEachJointDefinitionLazy(rootBodyDefinition, searchDoneCriteria);
   }

   public void forEachRigidBodyDefinition(Consumer<RigidBodyDefinition> rigidBodyConsumer)
   {
      forEachRigidBodyDefinition(rootBodyDefinition, rigidBodyConsumer);
   }

   public void forEachRigidBodyDefinitionLazy(Predicate<RigidBodyDefinition> searchDoneCriteria)
   {
      forEachRigidBodyDefinitionLazy(rootBodyDefinition, searchDoneCriteria);
   }

   public static void forEachJointDefinition(RigidBodyDefinition start, Consumer<JointDefinition> jointConsumer)
   {
      if (start == null)
         return;

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         JointDefinition jointDefinition = start.getChildrenJoints().get(i);
         jointConsumer.accept(jointDefinition);
         forEachJointDefinition(jointDefinition.getSuccessor(), jointConsumer);
      }
   }

   public List<JointDefinition> getAllJoints()
   {
      List<JointDefinition> joints = new ArrayList<>();
      forEachJointDefinition(rootBodyDefinition, joints::add);
      return joints;
   }

   public List<RigidBodyDefinition> getAllRigidBodies()
   {
      return collectSubtreeRigidBodyDefinitions(rootBodyDefinition);
   }

   public List<ControllerDefinition> getControllerDefinitions()
   {
      return controllerDefinitions;
   }

   public RigidBodyBasics newIntance(ReferenceFrame rootFrame)
   {
      if (rootBodyDefinition == null)
         throw new NullPointerException("The robot " + name + " has no definition!");
      RigidBodyBasics rootBody = rootBodyDefinition.toRootBody(rootFrame);
      instantiateRecursively(rootBody, rootBodyDefinition);
      closeLoops(rootBody, rootBodyDefinition);
      return rootBody;
   }

   private void instantiateRecursively(RigidBodyBasics predecessor, RigidBodyDefinition predecessorDefinition)
   {
      for (JointDefinition childDefinition : predecessorDefinition.getChildrenJoints())
      {
         JointBasics child = childDefinition.toJoint(predecessor);
         if (childDefinition.isLoopClosure())
            continue; // The successor will be created by another path and will be attached to the loop closure afterward.
         RigidBodyDefinition successorDefinition = childDefinition.getSuccessor();

         if (successorDefinition == null)
            throw new NullPointerException("The joint " + child.getName() + " is missing the definition for its successor, robot name: " + name + ".");

         RigidBodyBasics successor = successorDefinition.toRigidBody(child);
         instantiateRecursively(successor, successorDefinition);
      }
   }

   private void closeLoops(RigidBodyBasics predecessor, RigidBodyDefinition predecessorDefinition)
   {
      if (predecessor == null)
         return;

      for (int i = 0; i < predecessor.getChildrenJoints().size(); i++)
      {
         JointBasics joint = predecessor.getChildrenJoints().get(i);
         JointDefinition jointDefinition = predecessorDefinition.getChildrenJoints().get(i);

         if (jointDefinition.isLoopClosure())
         {
            RigidBodyBasics rootBody = MultiBodySystemTools.getRootBody(predecessor);
            RigidBodyBasics successor = MultiBodySystemTools.findRigidBody(rootBody, jointDefinition.getSuccessor().getName());
            RigidBodyTransformReadOnly transformFromSuccessorParentJoint = jointDefinition.getLoopClosureDefinition().getTransformToSuccessorParent();
            joint.setupLoopClosure(successor, transformFromSuccessorParentJoint);
         }

         closeLoops(joint.getSuccessor(), jointDefinition.getSuccessor());
      }
   }

   public static List<JointDefinition> collectSubtreeJointDefinitions(RigidBodyDefinition start)
   {
      List<JointDefinition> joints = new ArrayList<>();
      forEachJointDefinition(start, joints::add);
      return joints;
   }

   public static List<RigidBodyDefinition> collectSubtreeRigidBodyDefinitions(RigidBodyDefinition start)
   {
      List<RigidBodyDefinition> rigidBodies = new ArrayList<>();
      forEachRigidBodyDefinition(start, rigidBodies::add);
      return rigidBodies;
   }

   public static JointDefinition findJointDefinition(RigidBodyDefinition start, String jointName)
   {
      if (start == null)
         return null;

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         JointDefinition jointDefinition = start.getChildrenJoints().get(i);

         if (jointDefinition.getName().equals(jointName))
            return jointDefinition;

         JointDefinition result = findJointDefinition(jointDefinition.getSuccessor(), jointName);

         if (result != null)
            return result;
      }

      return null;
   }

   public static RigidBodyDefinition findRigidBodyDefinition(RigidBodyDefinition start, String rigidBodyName)
   {
      if (start == null)
         return null;
      if (start.getName().equals(rigidBodyName))
         return start;

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         RigidBodyDefinition result = findRigidBodyDefinition(start.getChildrenJoints().get(i).getSuccessor(), rigidBodyName);

         if (result != null)
            return result;
      }
      return null;
   }

   public static void forEachJointDefinitionLazy(RigidBodyDefinition start, Predicate<JointDefinition> searchDoneCriteria)
   {
      if (start == null)
         return;

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         JointDefinition jointDefinition = start.getChildrenJoints().get(i);

         if (searchDoneCriteria.test(jointDefinition))
            return;

         forEachJointDefinitionLazy(jointDefinition.getSuccessor(), searchDoneCriteria);
      }
   }

   public static void forEachRigidBodyDefinition(RigidBodyDefinition start, Consumer<RigidBodyDefinition> rigidBodyConsumer)
   {
      if (start == null)
         return;

      rigidBodyConsumer.accept(start);

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         forEachRigidBodyDefinition(start.getChildrenJoints().get(i).getSuccessor(), rigidBodyConsumer);
      }
   }

   public static void forEachRigidBodyDefinitionLazy(RigidBodyDefinition start, Predicate<RigidBodyDefinition> searchDoneCriteria)
   {
      if (start == null)
         return;

      if (searchDoneCriteria.test(start))
         return;

      for (int i = 0; i < start.getChildrenJoints().size(); i++)
      {
         forEachRigidBodyDefinitionLazy(start.getChildrenJoints().get(i).getSuccessor(), searchDoneCriteria);
      }
   }
}
