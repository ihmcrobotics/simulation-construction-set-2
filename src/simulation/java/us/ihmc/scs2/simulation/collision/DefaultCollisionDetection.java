package us.ihmc.scs2.simulation.collision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import us.ihmc.euclid.shape.collision.EuclidShape3DCollisionResult;
import us.ihmc.euclid.shape.collision.epa.ExpandingPolytopeAlgorithm;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.scs2.simulation.collision.shape.CollisionShape;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine.RobotPhysicsEngine;

public class DefaultCollisionDetection
{
   private final ExpandingPolytopeAlgorithm depthAlgorithm = new ExpandingPolytopeAlgorithm();
   private final Map<RobotPhysicsEngine, List<RigidBodyCollisionResult>> collisionResultMap = new HashMap<>();

   public DefaultCollisionDetection()
   {
   }

   public void evaluationCollision(List<RobotPhysicsEngine> robotPhysicsEngines, List<CollisionShape> staticCollisionShapes)
   {
      collisionResultMap.clear();

      for (RobotPhysicsEngine robotPhysicsEngine : robotPhysicsEngines)
      {
         for (CollidableRigidBody collidableRigidBody : robotPhysicsEngine.getCollidableRigidBodies())
         {
            List<RigidBodyCollisionResult> evaluateCollision = evaluateCollision(collidableRigidBody, staticCollisionShapes);
            if (!evaluateCollision.isEmpty())
               collisionResultMap.put(robotPhysicsEngine, evaluateCollision);
         }
      }
   }
   
   public Map<RobotPhysicsEngine, List<RigidBodyCollisionResult>> getCollisionResultMap()
   {
      return collisionResultMap;
   }

   public List<RigidBodyCollisionResult> getCollisionResults(RobotPhysicsEngine robotPhysicsEngine)
   {
      return collisionResultMap.get(robotPhysicsEngine);
   }

   public List<RigidBodyCollisionResult> evaluateCollision(CollidableRigidBody collidableRigidBody, List<CollisionShape> collisionShapes)
   {
      return collisionShapes.stream().flatMap(collisionShape -> evaluateCollision(collidableRigidBody, collisionShape).stream()).collect(Collectors.toList());
   }

   public List<RigidBodyCollisionResult> evaluateCollision(CollidableRigidBody collidableRigidBody, CollisionShape collisionShape)
   {
      RigidBodyTransform shapePoseA = collidableRigidBody.getCollisionShape().getTransformToWorld();
      return collidableRigidBody.getCollisionShape().getShapes().stream().flatMap(shapeA -> evaluateCollision(shapePoseA, shapeA, collisionShape).stream())
                                .map(result -> new RigidBodyCollisionResult(collidableRigidBody.getOwner(), null, result)).collect(Collectors.toList());
   }

   public List<EuclidShape3DCollisionResult> evaluateCollision(RigidBodyTransformReadOnly shapePoseA, Shape3DReadOnly shapeA, CollisionShape collisionShapeB)
   {
      RigidBodyTransform transformFromAToB = new RigidBodyTransform();
      RigidBodyTransform shapeBTransform = collisionShapeB.getTransformToWorld();
      transformFromAToB.setAndInvert(shapeBTransform);
      transformFromAToB.multiply(shapePoseA);

      return collisionShapeB.getShapes().stream().map(shapeB -> evaluateCollision(transformFromAToB, shapeA, shapeBTransform, shapeB))
                            .filter(EuclidShape3DCollisionResult::areShapesColliding).collect(Collectors.toList());
   }

   public EuclidShape3DCollisionResult evaluateCollision(RigidBodyTransformReadOnly transformFromAToB, Shape3DReadOnly shapeA, RigidBodyTransform shapeBTransform, Shape3DReadOnly shapeB)
   {
      EuclidShape3DCollisionResult result = depthAlgorithm.evaluateCollision(CollisionTools.cloneAndTransformShape3D(transformFromAToB, shapeA), shapeB);
      if (result.areShapesColliding())
      { // Changing the result to be expressed in world
         shapeBTransform.transform(result.getPointOnA());
         shapeBTransform.transform(result.getPointOnB());
      }
      return result;
   }
}
