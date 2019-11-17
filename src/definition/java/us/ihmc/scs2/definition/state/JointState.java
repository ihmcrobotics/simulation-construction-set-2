package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Set;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import us.ihmc.mecano.multiBodySystem.interfaces.JointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.JointStateBasics;

public class JointState implements JointStateBasics
{
   private final Set<JointStateType> availableStates = EnumSet.noneOf(JointStateType.class);
   private final DenseMatrix64F configuration = new DenseMatrix64F(JointReadOnly.MAX_NUMBER_OF_DOFS, 1);
   private final DenseMatrix64F velocity = new DenseMatrix64F(JointReadOnly.MAX_NUMBER_OF_DOFS, 1);
   private final DenseMatrix64F acceleration = new DenseMatrix64F(JointReadOnly.MAX_NUMBER_OF_DOFS, 1);
   private final DenseMatrix64F effort = new DenseMatrix64F(JointReadOnly.MAX_NUMBER_OF_DOFS, 1);

   private final DenseMatrix64F intermediateMatrix = new DenseMatrix64F(JointReadOnly.MAX_NUMBER_OF_DOFS, 1);

   public JointState()
   {
   }

   @Override
   public void clear()
   {
      availableStates.clear();
   }

   @Override
   public void setConfiguration(JointReadOnly joint)
   {
      availableStates.add(JointStateType.CONFIGURATION);
      configuration.reshape(joint.getConfigurationMatrixSize(), 1);
      joint.getJointConfiguration(0, configuration);
   }

   @Override
   public void setVelocity(JointReadOnly joint)
   {
      availableStates.add(JointStateType.VELOCITY);
      velocity.reshape(joint.getDegreesOfFreedom(), 1);
      joint.getJointVelocity(0, velocity);
   }

   @Override
   public void addVelocity(JointReadOnly joint)
   {
      if (!availableStates.contains(JointStateType.VELOCITY))
      {
         setVelocity(joint);
      }
      else
      {
         intermediateMatrix.reshape(joint.getDegreesOfFreedom(), 1);
         joint.getJointVelocity(0, intermediateMatrix);
         CommonOps.addEquals(velocity, intermediateMatrix);
      }
   }

   @Override
   public void setAcceleration(JointReadOnly joint)
   {
      availableStates.add(JointStateType.ACCELERATION);
      acceleration.reshape(joint.getDegreesOfFreedom(), 1);
      joint.getJointAcceleration(0, acceleration);
   }

   @Override
   public void addAcceleration(JointReadOnly joint)
   {
      if (!hasOutputFor(JointStateType.ACCELERATION))
      {
         setAcceleration(joint);
      }
      else
      {
         intermediateMatrix.reshape(joint.getDegreesOfFreedom(), 1);
         joint.getJointAcceleration(0, intermediateMatrix);
         CommonOps.addEquals(acceleration, intermediateMatrix);
      }
   }

   @Override
   public void setEffort(JointReadOnly joint)
   {
      availableStates.add(JointStateType.EFFORT);
      effort.reshape(joint.getDegreesOfFreedom(), 1);
      joint.getJointTau(0, effort);
   }

   @Override
   public void addEffort(JointReadOnly joint)
   {
      if (!hasOutputFor(JointStateType.ACCELERATION))
      {
         setEffort(joint);
      }
      else
      {
         intermediateMatrix.reshape(joint.getDegreesOfFreedom(), 1);
         joint.getJointTau(0, intermediateMatrix);
         CommonOps.addEquals(effort, intermediateMatrix);
      }
   }

   @Override
   public boolean hasOutputFor(JointStateType query)
   {
      return availableStates.contains(query);
   }

   @Override
   public int getConfiguration(int startRow, DenseMatrix64F configurationToPack)
   {
      CommonOps.insert(configuration, configurationToPack, startRow, 0);
      return startRow + configuration.getNumRows();
   }

   @Override
   public int getVelocity(int startRow, DenseMatrix64F velocityToPack)
   {
      CommonOps.insert(velocity, velocityToPack, startRow, 0);
      return startRow + velocity.getNumRows();
   }

   @Override
   public int getAcceleration(int startRow, DenseMatrix64F accelerationToPack)
   {
      CommonOps.insert(acceleration, accelerationToPack, startRow, 0);
      return startRow + acceleration.getNumRows();
   }

   @Override
   public int getEffort(int startRow, DenseMatrix64F effortToPack)
   {
      CommonOps.insert(effort, effortToPack, startRow, 0);
      return startRow + effort.getNumRows();
   }

   @Override
   public void getConfiguration(JointBasics jointToUpdate)
   {
      jointToUpdate.setJointConfiguration(0, configuration);
   }

   @Override
   public void getVelocity(JointBasics jointToUpdate)
   {
      jointToUpdate.setJointVelocity(0, velocity);
   }

   @Override
   public void getAcceleration(JointBasics jointToUpdate)
   {
      jointToUpdate.setJointAcceleration(0, acceleration);
   }

   @Override
   public void getEffort(JointBasics jointToUpdate)
   {
      jointToUpdate.setJointTau(0, effort);
   }
}
