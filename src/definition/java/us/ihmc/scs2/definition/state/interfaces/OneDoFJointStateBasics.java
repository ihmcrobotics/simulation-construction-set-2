package us.ihmc.scs2.definition.state.interfaces;

import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointReadOnly;
import us.ihmc.mecano.tools.JointStateType;

public interface OneDoFJointStateBasics extends JointStateBasics, OneDoFJointStateReadOnly
{
   void setConfiguration(double q);

   void setVelocity(double qd);

   void setAcceleration(double qdd);

   void setEffort(double tau);

   default void addConfiguration(double q)
   {
      if (!hasOutputFor(JointStateType.CONFIGURATION))
         setConfiguration(q);
      else
         setConfiguration(getConfiguration() + q);
   }

   default void addVelocity(double qd)
   {
      if (!hasOutputFor(JointStateType.VELOCITY))
         setVelocity(qd);
      else
         setVelocity(getVelocity() + qd);
   }

   default void addAcceleration(double qdd)
   {
      if (!hasOutputFor(JointStateType.ACCELERATION))
         setAcceleration(qdd);
      else
         setAcceleration(getAcceleration() + qdd);
   }

   default void addEffort(double tau)
   {
      if (!hasOutputFor(JointStateType.EFFORT))
         setEffort(tau);
      else
         setEffort(getEffort() + tau);
   }

   @Override
   default void setConfiguration(JointReadOnly joint)
   {
      setConfiguration(((OneDoFJointReadOnly) joint).getQ());
   }

   @Override
   default void setVelocity(JointReadOnly joint)
   {
      setVelocity(((OneDoFJointReadOnly) joint).getQd());
   }

   @Override
   default void setAcceleration(JointReadOnly joint)
   {
      setAcceleration(((OneDoFJointReadOnly) joint).getQdd());
   }

   @Override
   default void setEffort(JointReadOnly joint)
   {
      setEffort(((OneDoFJointReadOnly) joint).getTau());
   }

   @Override
   default void addVelocity(JointReadOnly joint)
   {
      addVelocity(((OneDoFJointReadOnly) joint).getQd());
   }

   @Override
   default void addAcceleration(JointReadOnly joint)
   {
      addAcceleration(((OneDoFJointReadOnly) joint).getQdd());
   }

   @Override
   default void addEffort(JointReadOnly joint)
   {
      addEffort(((OneDoFJointReadOnly) joint).getTau());
   }
}
