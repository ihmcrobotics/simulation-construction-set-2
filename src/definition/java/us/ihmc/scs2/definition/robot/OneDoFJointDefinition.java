package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.Vector3D;

public abstract class OneDoFJointDefinition extends JointDefinition
{
   private final Vector3D axis = new Vector3D();

   private double positionLowerLimit = Double.NEGATIVE_INFINITY, positionUpperLimit = Double.POSITIVE_INFINITY;
   private double velocityLowerLimit = Double.NEGATIVE_INFINITY, velocityUpperLimit = Double.POSITIVE_INFINITY;
   private double effortLowerLimit = Double.NEGATIVE_INFINITY, effortUpperLimit = Double.POSITIVE_INFINITY;

   public OneDoFJointDefinition()
   {
   }

   public OneDoFJointDefinition(String name)
   {
      setName(name);
   }

   public Vector3D getAxis()
   {
      return axis;
   }

   public void setPositionLowerLimit(double positionLowerLimit)
   {
      this.positionLowerLimit = positionLowerLimit;
   }

   public void setPositionUpperLimit(double positionUpperLimit)
   {
      this.positionUpperLimit = positionUpperLimit;
   }

   public void setPositionLimits(double lowerLimit, double upperLimit)
   {
      setPositionLowerLimit(lowerLimit);
      setPositionUpperLimit(upperLimit);
   }

   public double getPositionLowerLimit()
   {
      return positionLowerLimit;
   }

   public double getPositionUpperLimit()
   {
      return positionUpperLimit;
   }

   public void setVelocityLowerLimit(double velocityLowerLimit)
   {
      this.velocityLowerLimit = velocityLowerLimit;
   }

   public void setVelocityUpperLimit(double velocityUpperLimit)
   {
      this.velocityUpperLimit = velocityUpperLimit;
   }

   public void setVelocityLimits(double lowerLimit, double upperLimit)
   {
      setVelocityLowerLimit(lowerLimit);
      setVelocityUpperLimit(upperLimit);
   }

   public void setVelocityLimits(double limit)
   {
      setVelocityLowerLimit(-limit);
      setVelocityUpperLimit(limit);
   }

   public double getVelocityLowerLimit()
   {
      return velocityLowerLimit;
   }

   public double getVelocityUpperLimit()
   {
      return velocityUpperLimit;
   }

   public void setEffortLowerLimit(double effortLowerLimit)
   {
      this.effortLowerLimit = effortLowerLimit;
   }

   public void setEffortUpperLimit(double effortUpperLimit)
   {
      this.effortUpperLimit = effortUpperLimit;
   }

   public void setEffortLimits(double lowerLimit, double upperLimit)
   {
      setEffortLowerLimit(lowerLimit);
      setEffortUpperLimit(upperLimit);
   }

   public void setEffortLimits(double limit)
   {
      setEffortLowerLimit(-limit);
      setEffortUpperLimit(limit);
   }

   public double getEffortLowerLimit()
   {
      return effortLowerLimit;
   }

   public double getEffortUpperLimit()
   {
      return effortUpperLimit;
   }
}
