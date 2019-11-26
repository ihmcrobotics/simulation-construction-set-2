package us.ihmc.scs2.definition.robot.interfaces;

import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;

public interface RobotInitialStateProvider
{
   JointStateReadOnly getInitialJointState(String jointName);

   public static RobotInitialStateProvider emptyProvider()
   {
      return jointName -> null;
   }
}
