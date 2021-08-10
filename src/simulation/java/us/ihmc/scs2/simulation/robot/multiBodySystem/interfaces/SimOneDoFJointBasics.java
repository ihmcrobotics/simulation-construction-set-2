package us.ihmc.scs2.simulation.robot.multiBodySystem.interfaces;

import org.ejml.data.DMatrix;

import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointReadOnly;
import us.ihmc.mecano.tools.MecanoTools;

public interface SimOneDoFJointBasics extends SimJointBasics, OneDoFJointBasics, SimOneDoFJointReadOnly
{
   void setDeltaQd(double deltaQd);

   @Override
   default void setJointDeltaTwistToZero()
   {
      setQd(0.0);
   }

   default void setJointDeltaTwist(JointReadOnly other)
   {
      setJointDeltaTwist(MecanoTools.checkTypeAndCast(other, OneDoFJointReadOnly.class));
   }

   default int setJointDeltaVelocity(int rowStart, DMatrix matrix)
   {
      setDeltaQd(matrix.get(rowStart, 0));
      return rowStart + getDegreesOfFreedom();
   }

   default void setJointDeltaTwist(SimOneDoFJointReadOnly other)
   {
      setDeltaQd(other.getDeltaQd());
   }
}
