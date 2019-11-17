package us.ihmc.scs2.definition.state.interfaces;

import org.ejml.data.DenseMatrix64F;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointBasics;

public interface OneDoFJointStateReadOnly extends JointStateReadOnly
{
   double getConfiguration();

   double getVelocity();

   double getAcceleration();

   double getEffort();

   @Override
   default int getConfiguration(int startRow, DenseMatrix64F configurationToPack)
   {
      configurationToPack.set(startRow, 0, getConfiguration());
      return startRow + 1;
   }

   @Override
   default int getVelocity(int startRow, DenseMatrix64F velocityToPack)
   {
      velocityToPack.set(startRow, 0, getVelocity());
      return startRow + 1;
   }

   @Override
   default int getAcceleration(int startRow, DenseMatrix64F accelerationToPack)
   {
      accelerationToPack.set(startRow, 0, getAcceleration());
      return startRow + 1;
   }

   @Override
   default int getEffort(int startRow, DenseMatrix64F effortToPack)
   {
      effortToPack.set(startRow, 0, getEffort());
      return startRow + 1;
   }

   @Override
   default void getConfiguration(JointBasics jointToUpdate)
   {
      ((OneDoFJointBasics) jointToUpdate).setQ(getConfiguration());
   }

   @Override
   default void getVelocity(JointBasics jointToUpdate)
   {
      ((OneDoFJointBasics) jointToUpdate).setQd(getVelocity());
   }

   @Override
   default void getAcceleration(JointBasics jointToUpdate)
   {
      ((OneDoFJointBasics) jointToUpdate).setQdd(getAcceleration());
   }

   @Override
   default void getEffort(JointBasics jointToUpdate)
   {
      ((OneDoFJointBasics) jointToUpdate).setTau(getEffort());
   }
}
