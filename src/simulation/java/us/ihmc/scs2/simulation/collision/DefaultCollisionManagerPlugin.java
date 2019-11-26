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
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
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

   private final double kp = 100000.0;
   private final double kd = 1000.0;
   private final double coefficientOfFriction = 0.7; // Equals to 35 degrees

   private final ReferenceFrame rootFrame;
   private final DefaultCollisionDetection collisionDetection = new DefaultCollisionDetection();
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

         interactionProviderMap.put(robotPhysicsEngine, new InteractionProvider(collisions));
      }

      for (int i = index; i < numberOfCollisionsToVisualize; i++)
         yoCollisionResults.get(i).setToNaN();
   }

   private static void computeCollisionAxis(CollisionResult collisionResult, ReferenceFrame rootFrame)
   {
      FrameVector3D collisionAxis = collisionResult.getCollisionAxisForA();

      if (!collisionResult.getNormalOnA().containsNaN())
      {
         collisionAxis.setIncludingFrame(collisionResult.getNormalOnA());
         collisionAxis.negate();
      }
      else if (!collisionResult.getNormalOnB().containsNaN())
      {
         collisionAxis.setIncludingFrame(collisionResult.getNormalOnB());
      }
      else
      {
         collisionResult.getPointOnA().changeFrame(rootFrame);
         collisionResult.getPointOnB().changeFrame(rootFrame);
         collisionAxis.sub(collisionResult.getPointOnB(), collisionResult.getPointOnA());
      }

      collisionAxis.normalize();
      collisionAxis.changeFrame(rootFrame);
   }

   public boolean computeCollisionWrenches(CollisionResult collision)
   {
      Wrench wrenchOnA = collision.getWrenchOnA();
      Wrench wrenchOnB = collision.getWrenchOnB();

      if (!collision.areShapesColliding())
      {
         wrenchOnA.setToZero();
         wrenchOnB.setToZero();
         return false;
      }

      computeCollisionAxis(collision, rootFrame);

      Collidable collidableA = collision.getCollidableA();
      Collidable collidableB = collision.getCollidableB();

      FramePoint3D pointOnA = collision.getPointOnA();
      FramePoint3D pointOnB = collision.getPointOnB();
      FrameVector3D collisionPositionTerm = new FrameVector3D(rootFrame);
      pointOnA.changeFrame(rootFrame);
      pointOnB.changeFrame(rootFrame);
      collisionPositionTerm.sub(pointOnB, pointOnA);
      collisionPositionTerm.scale(kp);

      Vector3D collisionVelocityTerm = computeCollisionDerivativeTerm(collision, kd, rootFrame);

      FrameVector3D collisionForce = new FrameVector3D(rootFrame);
      collisionForce.add(collisionPositionTerm, collisionVelocityTerm);
      enforceFrictionCone(collisionForce, collision, rootFrame, coefficientOfFriction);

      if (collidableA != null && collidableA.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableA.getRigidBody().getBodyFixedFrame();
         pointOnA.changeFrame(bodyFixedFrame);
         collisionForce.changeFrame(bodyFixedFrame);
         wrenchOnA.setIncludingFrame(bodyFixedFrame, bodyFixedFrame, null, collisionForce, pointOnA);
      }
      else
      {
         wrenchOnA.setToZero(rootFrame, rootFrame);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         pointOnB.changeFrame(bodyFixedFrame);
         collisionForce.changeFrame(bodyFixedFrame);
         wrenchOnB.setIncludingFrame(bodyFixedFrame, bodyFixedFrame, null, collisionForce, pointOnB);
         wrenchOnB.negate();
      }
      else
      {
         wrenchOnB.setToZero(rootFrame, rootFrame);
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
         FramePoint3D pointOnACopy = new FramePoint3D(collisionResult.getPointOnA());
         pointOnACopy.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(pointOnACopy, linearVelocityOfA);
         linearVelocityOfA.changeFrame(rootFrame);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         FramePoint3D pointOnBCopy = new FramePoint3D(collisionResult.getPointOnB());
         pointOnBCopy.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(pointOnBCopy, linearVelocityOfB);
         linearVelocityOfB.changeFrame(rootFrame);
      }

      collisionVelocityTermOnA.sub(linearVelocityOfA);
      collisionVelocityTermOnA.add(linearVelocityOfB);

      FrameVector3D collisionAxis = collisionResult.getCollisionAxisForA();

      double dot = collisionVelocityTermOnA.dot(collisionAxis);

      if (dot < 0.0)
      { // The damping would result in pulling the objects toward each other, we need to cancel that effect.
         collisionVelocityTermOnA.scaleAdd(-dot, collisionAxis, collisionVelocityTermOnA);
      }

      collisionVelocityTermOnA.scale(kd);

      return collisionVelocityTermOnA;
   }

   private static void enforceFrictionCone(Vector3DBasics collisionForce, CollisionResult collisionResult, ReferenceFrame rootFrame,
                                           double coefficientOfFriction)
   {
      FrameVector3D collisionAxis = collisionResult.getCollisionAxisForA();

      Vector3D forceNormal = new Vector3D();
      Vector3D forceTangential = new Vector3D();

      double forceNormalMagnitude = collisionForce.dot(collisionAxis);
      forceNormal.setAndScale(forceNormalMagnitude, collisionAxis);
      forceTangential.sub(collisionForce, forceNormal);
      double forceTangentialMagnitudeSquared = forceTangential.lengthSquared();

      double maxForceTangential = coefficientOfFriction * forceNormalMagnitude;

      if (forceTangentialMagnitudeSquared > maxForceTangential * maxForceTangential)
      {
         forceTangential.scale(maxForceTangential / Math.sqrt(forceTangentialMagnitudeSquared));
         collisionForce.add(forceNormal, forceTangential);
      }
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
