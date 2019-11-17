package us.ihmc.scs2.definition.state.interfaces;

import org.ejml.data.DenseMatrix64F;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.tools.JointStateType;

public interface JointStateReadOnly
{
   boolean hasOutputFor(JointStateType query);

   void getConfiguration(JointBasics jointToUpdate);

   void getVelocity(JointBasics jointToUpdate);

   void getAcceleration(JointBasics jointToUpdate);

   void getEffort(JointBasics jointToUpdate);

   int getConfiguration(int startRow, DenseMatrix64F configurationToPack);

   int getVelocity(int startRow, DenseMatrix64F velocityToPack);

   int getAcceleration(int startRow, DenseMatrix64F accelerationToPack);

   int getEffort(int startRow, DenseMatrix64F effortToPack);
}