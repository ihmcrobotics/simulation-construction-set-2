package us.ihmc.scs2.simulation.physicsEngine;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public interface PhysicsEnginePlugin
{
   default void initialize()
   {
   }

   void doScience(double dt, Vector3DReadOnly gravity);

   default String getPluginName()
   {
      return getClass().getSimpleName();
   }

   default YoVariableRegistry getYoVariableRegistry()
   {
      return null;
   }
}
