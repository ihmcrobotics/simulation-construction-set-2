package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.tools.MultiBodySystemStateIntegrator;

public class FirstOrderMultiBodyStateIntegratorPlugin implements RobotPhysicsEnginePlugin
{
   private MultiBodySystemBasics input;
   private final MultiBodySystemStateIntegrator integrator = new MultiBodySystemStateIntegrator();

   public FirstOrderMultiBodyStateIntegratorPlugin()
   {
   }

   @Override
   public void setMultiBodySystem(MultiBodySystemBasics multiBodySystem)
   {
      input = multiBodySystem;
   }

   @Override
   public void initialize()
   {
   }

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      integrator.setIntegrationDT(dt);
      integrator.doubleIntegrateFromAccelerationSubtree(input.getRootBody());
   }

   @Override
   public String getPluginName()
   {
      return getClass().getSimpleName();
   }
}
