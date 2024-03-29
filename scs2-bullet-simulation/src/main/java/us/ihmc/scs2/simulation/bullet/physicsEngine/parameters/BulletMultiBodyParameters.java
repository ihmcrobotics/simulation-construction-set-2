package us.ihmc.scs2.simulation.bullet.physicsEngine.parameters;

public class BulletMultiBodyParameters
{
   private boolean canSleep;
   private boolean hasSelfCollision;
   private boolean useGyroTerm;
   private boolean useGlobalVelocities;
   private boolean useRK4Integration;
   private double linearDamping;
   private double angularDamping;
   private double maxAppliedImpulse;
   private double maxCoordinateVelocity;

   public static BulletMultiBodyParameters defaultBulletMultiBodyParameters()
   {
      BulletMultiBodyParameters multiBodyParameters = new BulletMultiBodyParameters();
      multiBodyParameters.setCanSleep(false);
      multiBodyParameters.setHasSelfCollision(true);
      multiBodyParameters.setUseGyroTerm(true);
      multiBodyParameters.setUseGlobalVelocities(false);
      multiBodyParameters.setUseRK4Integration(false);
      // These are declared as floats because Bullet source code declares them as floats
      // and we assert they are equal
      multiBodyParameters.setLinearDamping(0.04f);
      multiBodyParameters.setAngularDamping(0.04f);
      multiBodyParameters.setMaxAppliedImpulse(1000.0f);
      multiBodyParameters.setMaxCoordinateVelocity(100.0f);
      return multiBodyParameters;
   }

   public BulletMultiBodyParameters()
   {
   }

   public BulletMultiBodyParameters(boolean canSleep,
                                    boolean hasSelfCollision,
                                    boolean useGyroTerm,
                                    boolean useGlobalVelocities,
                                    boolean useRK4Integration,
                                    double linearDamping,
                                    double angularDamping,
                                    double maxAppliedImpulse,
                                    double maxCoordinatedVelocity)
   {
      this.canSleep = canSleep;
      this.hasSelfCollision = hasSelfCollision;
      this.useGyroTerm = useGyroTerm;
      this.useGlobalVelocities = useGlobalVelocities;
      this.useRK4Integration = useRK4Integration;
      this.linearDamping = linearDamping;
      this.angularDamping = angularDamping;
      this.maxAppliedImpulse = maxAppliedImpulse;
      this.maxCoordinateVelocity = maxCoordinatedVelocity;
   }

   public void setCanSleep(boolean canSleep)
   {
      this.canSleep = canSleep;
   }

   public void setHasSelfCollision(boolean hasSelfCollision)
   {
      this.hasSelfCollision = hasSelfCollision;
   }

   public void setUseGyroTerm(boolean useGyroTerm)
   {
      this.useGyroTerm = useGyroTerm;
   }

   public void setUseGlobalVelocities(boolean useGlobalVelocities)
   {
      this.useGlobalVelocities = useGlobalVelocities;
   }

   public void setUseRK4Integration(boolean useRK4Integration)
   {
      this.useRK4Integration = useRK4Integration;
   }

   public void setLinearDamping(double linearDamping)
   {
      this.linearDamping = linearDamping;
   }

   public void setAngularDamping(double angularDamping)
   {
      this.angularDamping = angularDamping;
   }

   public void setMaxAppliedImpulse(double maxAppliedImpulse)
   {
      this.maxAppliedImpulse = maxAppliedImpulse;
   }

   public void setMaxCoordinateVelocity(double maxCoordinateVelocity)
   {
      this.maxCoordinateVelocity = maxCoordinateVelocity;
   }

   public boolean getCanSleep()
   {
      return canSleep;
   }

   public boolean getHasSelfCollision()
   {
      return hasSelfCollision;
   }

   public boolean getUseGyroTerm()
   {
      return useGyroTerm;
   }

   public boolean getUseGlobalVelocities()
   {
      return useGlobalVelocities;
   }

   public boolean getUseRK4Integration()
   {
      return useRK4Integration;
   }

   public double getLinearDamping()
   {
      return linearDamping;
   }

   public double getAngularDamping()
   {
      return angularDamping;
   }

   public double getMaxAppliedImpulse()
   {
      return maxAppliedImpulse;
   }

   public double getMaxCoordinateVelocity()
   {
      return maxCoordinateVelocity;
   }
}
