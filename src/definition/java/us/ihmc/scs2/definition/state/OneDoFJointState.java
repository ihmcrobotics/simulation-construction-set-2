package us.ihmc.scs2.definition.state;

import java.util.EnumSet;
import java.util.Set;

import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.state.interfaces.OneDoFJointStateBasics;

public class OneDoFJointState implements OneDoFJointStateBasics
{
   private final Set<JointStateType> availableStates = EnumSet.noneOf(JointStateType.class);
   private double configuration = 0.0;
   private double velocity = 0.0;
   private double acceleration = 0.0;
   private double effort = 0.0;

   public OneDoFJointState()
   {
   }

   @Override
   public void clear()
   {
      availableStates.clear();
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
}
