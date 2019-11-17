package us.ihmc.scs2.definition.robot;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.definition.visual.VisualDefinition;

public abstract class JointDefinition
{
   private String name;
   private final RigidBodyTransform transformToParent = new RigidBodyTransform();

   private RigidBodyDefinition successor;

   private final List<VisualDefinition> visualDefinitions = new ArrayList<>();

   public JointDefinition()
   {
   }

   public JointDefinition(String name)
   {
      setName(name);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public RigidBodyTransform getTransformToParent()
   {
      return transformToParent;
   }

   public void setSuccessor(RigidBodyDefinition successor)
   {
      this.successor = successor;
   }

   public RigidBodyDefinition getSuccessor()
   {
      return successor;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      visualDefinitions.add(visualDefinition);
   }

   public List<VisualDefinition> getVisualDefinitions()
   {
      return visualDefinitions;
   }

   public abstract JointBasics toJoint(RigidBodyBasics predecessor);

   @Override
   public String toString()
   {
      return name + ": origin: (x,y,z) " + transformToParent.getTranslation() + "(y,p,r) " + transformToParent.getRotation().toStringAsYawPitchRoll()
            + ", successor: " + successor.getName();
   }
}
