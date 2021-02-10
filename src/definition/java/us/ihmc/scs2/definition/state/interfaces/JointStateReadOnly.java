package us.ihmc.scs2.definition.state.interfaces;

import org.ejml.data.DMatrix;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.tools.JointStateType;

public interface JointStateReadOnly
{
   boolean hasOutputFor(JointStateType query);

   void getConfiguration(JointBasics jointToUpdate);

   void getVelocity(JointBasics jointToUpdate);

   void getAcceleration(JointBasics jointToUpdate);

   void getEffort(JointBasics jointToUpdate);

   int getConfiguration(int startRow, DMatrix configurationToPack);

   int getVelocity(int startRow, DMatrix velocityToPack);

   int getAcceleration(int startRow, DMatrix accelerationToPack);

   int getEffort(int startRow, DMatrix effortToPack);

   default void getAllStates(JointBasics jointToUpdate)
   {
      if (hasOutputFor(JointStateType.CONFIGURATION))
         getConfiguration(jointToUpdate);
      if (hasOutputFor(JointStateType.VELOCITY))
         getVelocity(jointToUpdate);
      if (hasOutputFor(JointStateType.ACCELERATION))
         getAcceleration(jointToUpdate);
      if (hasOutputFor(JointStateType.EFFORT))
         getEffort(jointToUpdate);
   }
}