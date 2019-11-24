package us.ihmc.scs2.simulation.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine.RobotPhysicsEngine;

public class DefaultCollisionDetection
{
   private final Map<RobotPhysicsEngine, List<CollisionResult>> collisionResultMap = new HashMap<>();

   public DefaultCollisionDetection()
   {
   }

   public void evaluationCollision(List<RobotPhysicsEngine> robotPhysicsEngines, List<Collidable> staticCollidables)
   {
      collisionResultMap.clear();

      for (RobotPhysicsEngine robotPhysicsEngine : robotPhysicsEngines)
      {
         for (Collidable collidableRigidBody : robotPhysicsEngine.getCollidables())
         {
            List<CollisionResult> collisionResults = new ArrayList<>();

            for (Collidable staticCollidable : staticCollidables)
            {
               if (!collidableRigidBody.isCollidableWith(staticCollidable))
                  continue;

               collisionResults.add(collidableRigidBody.evaluateCollision(staticCollidable));
            }

            if (!collisionResults.isEmpty())
               collisionResultMap.put(robotPhysicsEngine, collisionResults);
         }
      }
   }

   public Map<RobotPhysicsEngine, List<CollisionResult>> getCollisionResultMap()
   {
      return collisionResultMap;
   }

   public List<CollisionResult> getCollisionResults(RobotPhysicsEngine robotPhysicsEngine)
   {
      return collisionResultMap.get(robotPhysicsEngine);
   }
}
