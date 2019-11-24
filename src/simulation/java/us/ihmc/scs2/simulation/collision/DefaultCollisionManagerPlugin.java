package us.ihmc.scs2.simulation.collision;

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

public class DefaultCollisionManagerPlugin implements EnvironmentPhysicsEnginePlugin
{
   private final double kp = 100.0;
   private final double kd = 0.0;

   private final DefaultCollisionDetection collisionDetection = new DefaultCollisionDetection();
   private final Map<RobotPhysicsEngine, List<CollisionResult>> collisionResultMap = new HashMap<>();
   private final Map<RobotPhysicsEngine, InteractionProvider> interactionProviderMap = new HashMap<>();

   public DefaultCollisionManagerPlugin()
   {
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

      for (Entry<RobotPhysicsEngine, List<CollisionResult>> entry : collisionDetection.getCollisionResultMap().entrySet())
      {
         List<CollisionResult> collisions = entry.getValue().stream().peek(this::computeCollisionWrenches).collect(Collectors.toList());
         collisionResultMap.put(entry.getKey(), collisions);
         interactionProviderMap.put(entry.getKey(), new InteractionProvider(collisions));
      }
   }

   public void computeCollisionWrenches(CollisionResult collision)
   {
      Collidable collidableA = collision.getCollidableA();
      Collidable collidableB = collision.getCollidableB();

      FramePoint3DReadOnly pointOnA = collision.getPointOnA();
      FramePoint3DReadOnly pointOnB = collision.getPointOnB();
      Vector3D collisionPositionTerm = new Vector3D();
      collisionPositionTerm.sub(pointOnB, pointOnA);
      collisionPositionTerm.scale(kp);

      Vector3D collisionVelocityTerm = new Vector3D();

      if (collidableA != null && collidableA.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableA.getRigidBody().getBodyFixedFrame();
         FrameVector3D linearVelocityAtA = new FrameVector3D();
         FramePoint3D framePointOnA = new FramePoint3D(pointOnA);
         framePointOnA.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnA, linearVelocityAtA);
         linearVelocityAtA.changeFrame(bodyFixedFrame.getRootFrame());
         collisionVelocityTerm.sub(linearVelocityAtA);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         FrameVector3D linearVelocityAtB = new FrameVector3D();
         FramePoint3D framePointOnB = new FramePoint3D(pointOnB);
         framePointOnB.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnB, linearVelocityAtB);
         linearVelocityAtB.changeFrame(bodyFixedFrame.getRootFrame());
         collisionVelocityTerm.add(linearVelocityAtB);
      }

      collisionVelocityTerm.scale(kd);

      Vector3D collisionForce = new Vector3D();
      collisionForce.add(collisionPositionTerm, collisionVelocityTerm);

      if (collidableA != null && collidableA.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableA.getRigidBody().getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         collision.getWrenchOnA().setToZero(bodyFixedFrame, rootFrame);
         collision.getWrenchOnA().getLinearPart().set(collisionForce);
      }

      if (collidableB != null && collidableB.getRigidBody() != null)
      {
         MovingReferenceFrame bodyFixedFrame = collidableB.getRigidBody().getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         collision.getWrenchOnB().setToZero(bodyFixedFrame, rootFrame);
         collision.getWrenchOnB().getLinearPart().setAndNegate(collisionForce);
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
}
