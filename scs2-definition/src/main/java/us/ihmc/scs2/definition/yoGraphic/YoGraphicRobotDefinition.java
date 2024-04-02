package us.ihmc.scs2.definition.yoGraphic;

import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.visual.MaterialDefinition;
import us.ihmc.scs2.definition.yoComposite.YoOrientation3DDefinition;
import us.ihmc.scs2.definition.yoComposite.YoTuple3DDefinition;

import java.util.Objects;

public class YoGraphicRobotDefinition extends YoGraphicDefinition
{
   private RobotDefinition robotDefinition;
   private MaterialDefinition materialDefinition;
   private YoRobotStateDefinition robotStateDefinition;

   public YoGraphicRobotDefinition()
   {
   }

   public YoGraphicRobotDefinition(RobotDefinition robotDefinition)
   {
      setRobotDefinition(robotDefinition);
   }

   public void setRobotDefinition(RobotDefinition robotDefinition)
   {
      this.robotDefinition = robotDefinition;
   }

   public void setMaterialDefinition(MaterialDefinition materialDefinition)
   {
      this.materialDefinition = materialDefinition;
   }

   public void setRobotStateDefinition(YoRobotStateDefinition robotStateDefinition)
   {
      this.robotStateDefinition = robotStateDefinition;
   }

   public void setRootJointPosition(YoTuple3DDefinition rootJointPosition)
   {
      if (robotStateDefinition == null)
         robotStateDefinition = new YoRobotStateDefinition();
      robotStateDefinition.setRootJointPosition(rootJointPosition);
   }

   public void setRootJointOrientation(YoOrientation3DDefinition rootJointOrientation)
   {
      if (robotStateDefinition == null)
         robotStateDefinition = new YoRobotStateDefinition();
      robotStateDefinition.setRootJointOrientation(rootJointOrientation);
   }

   public void setJointPositions(YoListDefinition jointPositions)
   {
      if (robotStateDefinition == null)
         robotStateDefinition = new YoRobotStateDefinition();
      robotStateDefinition.setJointPositions(jointPositions);
   }

   public RobotDefinition getRobotDefinition()
   {
      return robotDefinition;
   }

   public MaterialDefinition getMaterialDefinition()
   {
      return materialDefinition;
   }

   public YoRobotStateDefinition getRobotStateDefinition()
   {
      return robotStateDefinition;
   }

   public YoTuple3DDefinition getRootJointPosition()
   {
      return robotStateDefinition == null ? null : robotStateDefinition.getRootJointPosition();
   }

   public YoOrientation3DDefinition getRootJointOrientation()
   {
      return robotStateDefinition == null ? null : robotStateDefinition.getRootJointOrientation();
   }

   public YoListDefinition getJointPositions()
   {
      return robotStateDefinition == null ? null : robotStateDefinition.getJointPositions();
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
         if (!Objects.equals(materialDefinition, other.materialDefinition))
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
      private YoListDefinition jointPositions;

      public YoRobotStateDefinition()
      {
      }

      public void setRootJointPosition(YoTuple3DDefinition rootJointPosition)
      {
         this.rootJointPosition = rootJointPosition;
      }

      public void setRootJointOrientation(YoOrientation3DDefinition rootJointOrientation)
      {
         this.rootJointOrientation = rootJointOrientation;
      }

      public void setJointPositions(YoListDefinition jointPositions)
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

      public YoListDefinition getJointPositions()
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
}
