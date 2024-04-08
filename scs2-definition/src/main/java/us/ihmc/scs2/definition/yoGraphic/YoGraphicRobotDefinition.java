package us.ihmc.scs2.definition.yoGraphic;

import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class YoGraphicRobotDefinition extends YoGraphic3DDefinition
{
   private RobotDefinition robotDefinition;
   private YoRobotStateDefinition robotStateDefinition;

   public YoGraphicRobotDefinition()
   {
   }

   public YoGraphicRobotDefinition(YoGraphicRobotDefinition other)
   {
      if (other.name != null)
         setName(other.name);
      if (other.color != null)
         setColor(other.color);
      if (other.robotDefinition != null)
         setRobotDefinition(new RobotDefinition(other.robotDefinition));
      if (other.robotStateDefinition != null)
         setRobotStateDefinition(new YoRobotStateDefinition(other.robotStateDefinition));
   }

   public YoGraphicRobotDefinition(RobotDefinition robotDefinition)
   {
      setRobotDefinition(robotDefinition);
   }

   public void setRobotDefinition(RobotDefinition robotDefinition)
   {
      this.robotDefinition = robotDefinition;
   }

   public void setRobotStateDefinition(YoRobotStateDefinition robotStateDefinition)
   {
      this.robotStateDefinition = robotStateDefinition;
   }

   public RobotDefinition getRobotDefinition()
   {
      return robotDefinition;
   }

   public YoRobotStateDefinition getRobotStateDefinition()
   {
      return robotStateDefinition;
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (!super.equals(object))
      {
         return false;
      }
      else if (object instanceof YoGraphicRobotDefinition other)
      {
         if (!Objects.equals(robotDefinition, other.robotDefinition))
            return false;
         if (!Objects.equals(robotStateDefinition, other.robotStateDefinition))
            return false;
         return true;
      }
      else
      {
         return false;
      }
   }

   public static class YoRobotStateDefinition
   {
      private YoTuple3DDefinition rootJointPosition;
      private YoOrientation3DDefinition rootJointOrientation;
      private List<YoOneDoFJointStateDefinition> jointPositions;

      public YoRobotStateDefinition()
      {
      }

      public YoRobotStateDefinition(YoRobotStateDefinition other)
      {
         if (other == null)
            return;
         if (other.rootJointPosition != null)
            setRootJointPosition(other.rootJointPosition.copy());
         if (other.rootJointOrientation != null)
            setRootJointOrientation(other.rootJointOrientation.copy());
         if (other.jointPositions != null)
            jointPositions = other.jointPositions.stream().map(YoOneDoFJointStateDefinition::new).collect(Collectors.toList());
      }

      public void setRootJointPosition(YoTuple3DDefinition rootJointPosition)
      {
         this.rootJointPosition = rootJointPosition;
      }

      public void setRootJointOrientation(YoOrientation3DDefinition rootJointOrientation)
      {
         this.rootJointOrientation = rootJointOrientation;
      }

      public void setJointPositions(List<YoOneDoFJointStateDefinition> jointPositions)
      {
         this.jointPositions = jointPositions;
      }

      public YoTuple3DDefinition getRootJointPosition()
      {
         return rootJointPosition;
      }

      public YoOrientation3DDefinition getRootJointOrientation()
      {
         return rootJointOrientation;
      }

      public List<YoOneDoFJointStateDefinition> getJointPositions()
      {
         return jointPositions;
      }

      @Override
      public boolean equals(Object object)
      {
         if (object == this)
         {
            return true;
         }
         else if (object instanceof YoRobotStateDefinition other)
         {
            if (!Objects.equals(rootJointPosition, other.rootJointPosition))
               return false;
            if (!Objects.equals(rootJointOrientation, other.rootJointOrientation))
               return false;
            if (!Objects.equals(jointPositions, other.jointPositions))
               return false;
            return true;
         }
         else
         {
            return false;
         }
      }
   }

   public static class YoOneDoFJointStateDefinition
   {
      private String jointName;
      private String jointPosition;

      public YoOneDoFJointStateDefinition()
      {
      }

      public YoOneDoFJointStateDefinition(String jointName, String jointPosition)
      {
         setJointName(jointName);
         setJointPosition(jointPosition);
      }

      public YoOneDoFJointStateDefinition(YoOneDoFJointStateDefinition other)
      {
         setJointName(other.jointName);
         setJointPosition(other.jointPosition);
      }

      public void setJointName(String jointName)
      {
         this.jointName = jointName;
      }

      public void setJointPosition(String jointPosition)
      {
         this.jointPosition = jointPosition;
      }

      public String getJointName()
      {
         return jointName;
      }

      public String getJointPosition()
      {
         return jointPosition;
      }

      @Override
      public boolean equals(Object object)
      {
         if (object == this)
         {
            return true;
         }
         else if (object instanceof YoOneDoFJointStateDefinition other)
         {
            if (!Objects.equals(jointName, other.jointName))
               return false;
            if (!Objects.equals(jointPosition, other.jointPosition))
               return false;
            return true;
         }
         else
         {
            return false;
         }
      }
   }
}
