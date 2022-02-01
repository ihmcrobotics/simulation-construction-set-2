package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.ejml.data.DMatrixRMaj;

import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.state.interfaces.OneDoFJointStateBasics;
import us.ihmc.scs2.definition.state.interfaces.OneDoFJointStateReadOnly;

public class OneDoFJointState extends JointStateBase implements OneDoFJointStateBasics
{
   private final Set<JointStateType> availableStates = EnumSet.noneOf(JointStateType.class);
   private double configuration = 0.0;
   private double velocity = 0.0;
   private double acceleration = 0.0;
   private double effort = 0.0;

   private final DMatrixRMaj temp = new DMatrixRMaj(1, 1);

   public OneDoFJointState()
   {
   }

   public OneDoFJointState(double q)
   {
      setConfiguration(q);
   }

   public OneDoFJointState(double q, double qd)
   {
      setConfiguration(q);
      setVelocity(qd);
   }

   public OneDoFJointState(double q, double qd, double tau)
   {
      setConfiguration(q);
      setVelocity(qd);
      setEffort(tau);
   }

   public OneDoFJointState(JointStateReadOnly other)
   {
      set(other);
   }

   @Override
   public void clear()
   {
      availableStates.clear();
   }

   public void set(OneDoFJointState other)
   {
      configuration = other.configuration;
      velocity = other.velocity;
      acceleration = other.acceleration;
      effort = other.effort;
      availableStates.addAll(other.availableStates);
   }

   @Override
   public void set(JointStateReadOnly jointStateReadOnly)
   {
      if (jointStateReadOnly instanceof OneDoFJointState)
      {
         set((OneDoFJointState) jointStateReadOnly);
      }
      else if (jointStateReadOnly instanceof OneDoFJointStateReadOnly)
      {
         OneDoFJointStateBasics.super.set((OneDoFJointStateReadOnly) jointStateReadOnly);
      }
      else
      {
         if (jointStateReadOnly.getConfigurationSize() != getConfigurationSize() || jointStateReadOnly.getDegreesOfFreedom() != getDegreesOfFreedom())
            throw new IllegalArgumentException("Dimension mismatch");
         clear();
         if (jointStateReadOnly.hasOutputFor(JointStateType.CONFIGURATION))
         {
            jointStateReadOnly.getConfiguration(0, temp);
            setConfiguration(0, temp);
         }
         if (jointStateReadOnly.hasOutputFor(JointStateType.VELOCITY))
         {
            jointStateReadOnly.getVelocity(0, temp);
            setVelocity(0, temp);
         }
         if (jointStateReadOnly.hasOutputFor(JointStateType.ACCELERATION))
         {
            jointStateReadOnly.getAcceleration(0, temp);
            setAcceleration(0, temp);
         }
         if (jointStateReadOnly.hasOutputFor(JointStateType.EFFORT))
         {
            jointStateReadOnly.getEffort(0, temp);
            setEffort(0, temp);
         }
      }
   }

   @Override
   public void setConfiguration(double q)
   {
      availableStates.add(JointStateType.CONFIGURATION);
      configuration = q;
   }

   @Override
   public void setVelocity(double qd)
   {
      availableStates.add(JointStateType.VELOCITY);
      velocity = qd;
   }

   @Override
   public void setAcceleration(double qdd)
   {
      availableStates.add(JointStateType.ACCELERATION);
      acceleration = qdd;
   }

   @Override
   public void setEffort(double tau)
   {
      availableStates.add(JointStateType.EFFORT);
      effort = tau;
   }

   @Override
   public boolean hasOutputFor(JointStateType query)
   {
      return availableStates.contains(query);
   }

   @Override
   public double getConfiguration()
   {
      return configuration;
   }

   @Override
   public double getVelocity()
   {
      return velocity;
   }

   @Override
   public double getAcceleration()
   {
      return acceleration;
   }

   @Override
   public double getEffort()
   {
      return effort;
   }

   @Override
   public OneDoFJointState copy()
   {
      return new OneDoFJointState(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, availableStates);
      if (availableStates.contains(JointStateType.CONFIGURATION))
         bits = EuclidHashCodeTools.addToHashCode(bits, configuration);
      if (availableStates.contains(JointStateType.VELOCITY))
         bits = EuclidHashCodeTools.addToHashCode(bits, velocity);
      if (availableStates.contains(JointStateType.ACCELERATION))
         bits = EuclidHashCodeTools.addToHashCode(bits, acceleration);
      if (availableStates.contains(JointStateType.EFFORT))
         bits = EuclidHashCodeTools.addToHashCode(bits, effort);
      return EuclidHashCodeTools.toIntHashCode(bits);
   }

   @Override
   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null)
         return false;
      if (getClass() != object.getClass())
         return false;

      OneDoFJointState other = (OneDoFJointState) object;

      if (!Objects.equals(availableStates, other.availableStates))
         return false;
      if (availableStates.contains(JointStateType.CONFIGURATION) && Double.doubleToLongBits(configuration) != Double.doubleToLongBits(other.configuration))
         return false;
      if (availableStates.contains(JointStateType.VELOCITY) && Double.doubleToLongBits(velocity) != Double.doubleToLongBits(other.velocity))
         return false;
      if (availableStates.contains(JointStateType.ACCELERATION) && Double.doubleToLongBits(acceleration) != Double.doubleToLongBits(other.acceleration))
         return false;
      if (availableStates.contains(JointStateType.EFFORT) && Double.doubleToLongBits(effort) != Double.doubleToLongBits(other.effort))
         return false;

      return true;
   }
}
