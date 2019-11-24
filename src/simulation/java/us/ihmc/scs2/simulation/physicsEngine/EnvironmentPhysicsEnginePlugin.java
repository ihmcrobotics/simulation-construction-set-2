package us.ihmc.scs2.simulation.physicsEngine;

import java.util.List;

import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.scs2.simulation.collision.Collidable;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine.RobotPhysicsEngine;

public interface EnvironmentPhysicsEnginePlugin extends PhysicsEnginePlugin
{
   void submitWorldElements(List<RobotPhysicsEngine> robotPhysicsEngines, List<Collidable> staticCollidables);

   ExternalInteractionProvider getRobotInteractions(RobotPhysicsEngine robotPhysicsEngine);

   public static EnvironmentPhysicsEnginePlugin emptyPlugin()
   {
      return new EnvironmentPhysicsEnginePlugin()
      {
         @Override
         public void doScience(double dt, Vector3DReadOnly gravity)
         {
         }

         @Override
         public void submitWorldElements(List<RobotPhysicsEngine> robotPhysicsEngines, List<Collidable> staticCollidables)
         {
         }

         @Override
         public ExternalInteractionProvider getRobotInteractions(RobotPhysicsEngine robotPhysicsEngine)
         {
            return ExternalInteractionProvider.emptyExternalInteractionProvider();
         }
      };
   }
}
