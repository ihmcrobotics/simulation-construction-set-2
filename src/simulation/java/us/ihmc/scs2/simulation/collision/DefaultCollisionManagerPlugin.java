package us.ihmc.scs2.simulation.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FramePoint3DReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.Wrench;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.scs2.simulation.physicsEngine.EnvironmentPhysicsEnginePlugin;
import us.ihmc.scs2.simulation.physicsEngine.ExternalInteractionProvider;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine.RobotPhysicsEngine;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class DefaultCollisionManagerPlugin implements EnvironmentPhysicsEnginePlugin
{
   private final YoVariableRegistry registry = new YoVariableRegistry(getClass().getSimpleName());
   private final int numberOfCollisionsToVisualize = 5;
   private final List<YoCollisionResult> yoCollisionResults = new ArrayList<>();

   private final double kp = 10000.0;
   private final double kd = 1000.0;

   private final ReferenceFrame rootFrame;
   private final DefaultCollisionDetection collisionDetection = new DefaultCollisionDetection();
   private final Map<RobotPhysicsEngine, List<CollisionResult>> collisionResultMap = new HashMap<>();
   private final Map<RobotPhysicsEngine, InteractionProvider> interactionProviderMap = new HashMap<>();

   public DefaultCollisionManagerPlugin(ReferenceFrame rootFrame)
   {
      this.rootFrame = rootFrame;
      for (int i = 0; i < numberOfCollisionsToVisualize; i++)
      {
         YoCollisionResult yoCollisionResult = new YoCollisionResult("_" + i, rootFrame, registry);
         yoCollisionResults.add(yoCollisionResult);
      }
   }

   @Override
   public void submitWorldElements(List<RobotPhysicsEngine> robotPhysicsEngines, List<Collidable> staticCollidables)
   {
      collisionDetection.evaluationCollision(robotPhysicsEngines, staticCollidables);
   }

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      interactionProviderMap.clear();

      int index = 0;

      for (Entry<RobotPhysicsEngine, List<CollisionResult>> entry : collisionDetection.getCollisionResultMap().entrySet())
      {
         RobotPhysicsEngine robotPhysicsEngine = entry.getKey();
         List<CollisionResult> collisions = entry.getValue().stream().filter(this::computeCollisionWrenches).collect(Collectors.toList());

         for (CollisionResult result : collisions)
         {
            if (index >= numberOfCollisionsToVisualize)
               break;

            yoCollisionResults.get(index).update(result);
            index++;
         }

         collisionResultMap.put(robotPhysicsEngine, collisions);
         interactionProviderMap.put(robotPhysicsEngine, new InteractionProvider(collisions));
      }

      for (int i = index; i < numberOfCollisionsToVisualize; i++)
         yoCollisionResults.get(i).setToNaN();
   }

   public boolean computeCollisionWrenches(CollisionResult collision)
   {
      if (!collision.areShapesColliding())
      {
         collision.getWrenchOnA().setToZero();
         collision.getWrenchOnB().setToZero();
         return false;
      }

      Collidable collidableA = collision.getCollidableA();
      Collidable collidableB = collision.getCollidableB();

      FramePoint3DReadOnly pointOnA = collision.getPointOnA();
      FramePoint3DReadOnly pointOnB = collision.getPointOnB();
      Vector3D collisionPositionTerm = new Vector3D();
      collisionPositionTerm.sub(pointOnB, pointOnA);
      collisionPositionTerm.scale(kp);

      Vector3D collisionVelocityTerm = computeCollisionDerivativeTerm(collision, kd, rootFrame);

      Vector3D collisionForce = new Vector3D();
      collisionForce.add(collisionPositionTerm, collisionVelocityTerm);

      if (collidableA != null && collidableA.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableA.getRigidBody().getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         collision.getWrenchOnA().setToZero(bodyFixedFrame, rootFrame);
         collision.getWrenchOnA().getLinearPart().set(collisionForce);
      }
      else
      {
         collision.getWrenchOnA().setToZero(rootFrame, rootFrame);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         collision.getWrenchOnB().setToZero(bodyFixedFrame, rootFrame);
         collision.getWrenchOnB().getLinearPart().setAndNegate(collisionForce);
      }
      else
      {
         collision.getWrenchOnB().setToZero(rootFrame, rootFrame);
      }

      return true;
   }

   private static Vector3D computeCollisionDerivativeTerm(CollisionResult collisionResult, double kd, ReferenceFrame rootFrame)
   {
      Collidable collidableA = collisionResult.getCollidableA();
      Collidable collidableB = collisionResult.getCollidableB();

      Vector3D collisionVelocityTermOnA = new Vector3D();
      FrameVector3D linearVelocityOfA = new FrameVector3D(rootFrame);
      FrameVector3D linearVelocityOfB = new FrameVector3D(rootFrame);

      if (collidableA != null && collidableA.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableA.getRigidBody().getBodyFixedFrame();
         FramePoint3D framePointOnA = new FramePoint3D(collisionResult.getPointOnA());
         framePointOnA.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnA, linearVelocityOfA);
         linearVelocityOfA.changeFrame(rootFrame);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         FramePoint3D framePointOnB = new FramePoint3D(collisionResult.getPointOnB());
         framePointOnB.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnB, linearVelocityOfB);
         linearVelocityOfB.changeFrame(rootFrame);
      }

      collisionVelocityTermOnA.sub(linearVelocityOfA);
      collisionVelocityTermOnA.add(linearVelocityOfB);
      FrameVector3D collisionAxis = new FrameVector3D();

      if (!collisionResult.getNormalOnA().containsNaN())
      {
         collisionAxis.setIncludingFrame(collisionResult.getNormalOnA());
         if (collisionResult.areShapesColliding())
            collisionAxis.negate();
      }
      else if (!collisionResult.getNormalOnB().containsNaN())
      {
         collisionAxis.setIncludingFrame(collisionResult.getNormalOnB());
         if (!collisionResult.areShapesColliding())
            collisionAxis.negate();
      }
      else
      {
         collisionResult.getPointOnA().changeFrame(rootFrame);
         collisionResult.getPointOnB().changeFrame(rootFrame);
         collisionAxis.sub(collisionResult.getPointOnB(), collisionResult.getPointOnA());
         collisionAxis.normalize();
      }

      collisionAxis.changeFrame(rootFrame);

      // TODO Review the following
//      double dot = collisionVelocityTermOnA.dot(collisionAxis);
//
//      if (dot < 0.0)
//      { // The damping would result in pulling the objects toward each other, we need to cancel that effect.
//         collisionAxis.scale(dot);
//         collisionVelocityTermOnA.sub(collisionAxis);
//      }

      collisionVelocityTermOnA.scale(kd);

      return collisionVelocityTermOnA;
   }

   @Override
   public ExternalInteractionProvider getRobotInteractions(RobotPhysicsEngine robotPhysicsEngine)
   {
      return interactionProviderMap.get(robotPhysicsEngine);
   }

   private static class InteractionProvider implements ExternalInteractionProvider
   {
      private final List<RigidBodyBasics> rigidBodies;
      private final Map<RigidBodyBasics, Wrench> wrenchMap;

      public InteractionProvider(List<CollisionResult> collisionResults)
      {
         rigidBodies = collisionResults.stream().map(collisionResult -> collisionResult.getCollidableA().getRigidBody()).collect(Collectors.toList());
         wrenchMap = collisionResults.stream().collect(Collectors.toMap(collisionResult -> collisionResult.getCollidableA().getRigidBody(),
                                                                        CollisionResult::getWrenchOnA));
      }

      @Override
      public List<? extends RigidBodyBasics> getRigidBodies()
      {
         return rigidBodies;
      }

      @Override
      public WrenchReadOnly getExternalWrench(RigidBodyBasics rigidBody)
      {
         return wrenchMap.get(rigidBody);
      }
   }

   @Override
   public YoVariableRegistry getYoVariableRegistry()
   {
      return registry;
   }
}
