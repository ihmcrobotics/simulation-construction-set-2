package us.ihmc.scs2.definition.robot.interfaces;

import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;

public interface RobotInitialStateProvider
{
   default JointStateReadOnly getInitialJointState(String jointName)
   {
      return null;
   }

   public static RobotInitialStateProvider emptyProvider()
   {
      return new RobotInitialStateProvider()
      {
         /* Do nothing */
      };
   }
}
