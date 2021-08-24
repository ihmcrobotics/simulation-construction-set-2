package us.ihmc.scs2.definition.robot;

import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;

public abstract class OneDoFJointDefinition extends JointDefinition
{
   private final Vector3D axis = new Vector3D();

   private double positionLowerLimit = Double.NEGATIVE_INFINITY, positionUpperLimit = Double.POSITIVE_INFINITY;
   private double velocityLowerLimit = Double.NEGATIVE_INFINITY, velocityUpperLimit = Double.POSITIVE_INFINITY;
   private double effortLowerLimit = Double.NEGATIVE_INFINITY, effortUpperLimit = Double.POSITIVE_INFINITY;
   private double damping = -1.0;
   private double stiction = -1.0;

   private double kpSoftLimitStop = -1.0;
   private double kdSoftLimitStop = -1.0;

   public OneDoFJointDefinition()
   {
   }

   public OneDoFJointDefinition(String name)
   {
      super(name);
   }

   public OneDoFJointDefinition(String name, Tuple3DReadOnly offsetFromParent, Vector3DReadOnly axis)
   {
      super(name, offsetFromParent);
      setAxis(axis);
   }

   public void setAxis(Vector3DReadOnly axis)
   {
      this.axis.set(axis);
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

   public void setStiction(double stiction)
   {
      this.stiction = stiction;
   }

   public double getStiction()
   {
      return stiction;
   }

   public void setDamping(double damping)
   {
      this.damping = damping;
   }

   public double getDamping()
   {
      return damping;
   }

   public void setKpSoftLimitStop(double kpSoftLimitStop)
   {
      this.kpSoftLimitStop = kpSoftLimitStop;
   }

   public void setKdSoftLimitStop(double kdSoftLimitStop)
   {
      this.kdSoftLimitStop = kdSoftLimitStop;
   }

   public void setGainsSoftLimitStop(double kpSoftLimitStop, double kdSoftLimitStop)
   {
      this.kpSoftLimitStop = kpSoftLimitStop;
      this.kdSoftLimitStop = kdSoftLimitStop;
   }

   public double getKpSoftLimitStop()
   {
      return kpSoftLimitStop;
   }

   public double getKdSoftLimitStop()
   {
      return kdSoftLimitStop;
   }
}
