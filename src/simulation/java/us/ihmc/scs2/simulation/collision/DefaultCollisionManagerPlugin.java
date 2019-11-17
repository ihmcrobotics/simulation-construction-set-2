package us.ihmc.scs2.simulation.collision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.frames.MovingReferenceFrame;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.mecano.spatial.Wrench;
import us.ihmc.mecano.spatial.interfaces.WrenchReadOnly;
import us.ihmc.scs2.simulation.collision.shape.CollisionShape;
import us.ihmc.scs2.simulation.physicsEngine.EnvironmentPhysicsEnginePlugin;
import us.ihmc.scs2.simulation.physicsEngine.ExternalInteractionProvider;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine.RobotPhysicsEngine;

public class DefaultCollisionManagerPlugin implements EnvironmentPhysicsEnginePlugin
{
   private final double kp = 10000.0;
   private final double kd = 10000.0;

   private final DefaultCollisionDetection collisionDetection = new DefaultCollisionDetection();
   private final Map<RobotPhysicsEngine, List<CollisionResult>> collisionResultMap = new HashMap<>();
   private final Map<RobotPhysicsEngine, InteractionProvider> interactionProviderMap = new HashMap<>();
   
   public DefaultCollisionManagerPlugin()
   {
   }
   
   @Override
   public void submitWorldElements(List<RobotPhysicsEngine> robotPhysicsEngines, List<CollisionShape> staticCollisionShapes)
   {
      collisionDetection.evaluationCollision(robotPhysicsEngines, staticCollisionShapes);
   }

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      interactionProviderMap.clear();

      for (Entry<RobotPhysicsEngine, List<RigidBodyCollisionResult>> entry : collisionDetection.getCollisionResultMap().entrySet())
      {
         List<CollisionResult> collisions = entry.getValue().stream().map(this::computeCollisionResult).collect(Collectors.toList());
         collisionResultMap.put(entry.getKey(), collisions);
         interactionProviderMap.put(entry.getKey(), new InteractionProvider(collisions));
      }
   }

   public CollisionResult computeCollisionResult(RigidBodyCollisionResult input)
   {
      CollisionResult output = new CollisionResult();
      RigidBodyBasics bodyA = input.getBodyA();
      RigidBodyBasics bodyB = input.getBodyB();

      Point3DReadOnly pointOnA = input.getShape3DCollisionResult().getPointOnA();
      Point3DReadOnly pointOnB = input.getShape3DCollisionResult().getPointOnB();
      Vector3D collisionPositionTerm = new Vector3D();
      collisionPositionTerm.sub(pointOnB, pointOnA);
      collisionPositionTerm.scale(kp);

      Vector3D collisionVelocityTerm = new Vector3D();

      if (bodyA != null)
      {
         MovingReferenceFrame bodyFixedFrame = bodyA.getBodyFixedFrame();
         FrameVector3D linearVelocityAtA = new FrameVector3D();
         FramePoint3D framePointOnA = new FramePoint3D(bodyFixedFrame.getRootFrame(), pointOnA);
         framePointOnA.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnA, linearVelocityAtA);
         linearVelocityAtA.changeFrame(bodyFixedFrame.getRootFrame());
         collisionVelocityTerm.sub(linearVelocityAtA);
      }
      
      if (bodyB != null)
      {
         MovingReferenceFrame bodyFixedFrame = bodyB.getBodyFixedFrame();
         FrameVector3D linearVelocityAtB = new FrameVector3D();
         FramePoint3D framePointOnB = new FramePoint3D(bodyFixedFrame.getRootFrame(), pointOnB);
         framePointOnB.changeFrame(bodyFixedFrame);
         bodyFixedFrame.getTwistOfFrame().getLinearVelocityAt(framePointOnB, linearVelocityAtB);
         linearVelocityAtB.changeFrame(bodyFixedFrame.getRootFrame());
         collisionVelocityTerm.add(linearVelocityAtB);
      }

      collisionVelocityTerm.scale(kd);

      Vector3D collisionForce = new Vector3D();
      collisionForce.add(collisionPositionTerm, collisionVelocityTerm);
      
      if (bodyA != null)
      {
         output.setBodyA(bodyA);
         MovingReferenceFrame bodyFixedFrame = bodyA.getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         output.getWrenchOnA().setToZero(bodyFixedFrame, rootFrame);
         output.getWrenchOnA().getLinearPart().set(collisionForce);
      }
      
      if (bodyB != null)
      {
         output.setBodyB(bodyB);
         MovingReferenceFrame bodyFixedFrame = bodyB.getBodyFixedFrame();
         ReferenceFrame rootFrame = bodyFixedFrame.getRootFrame();
         output.getWrenchOnB().setToZero(bodyFixedFrame, rootFrame);
         output.getWrenchOnB().getLinearPart().setAndNegate(collisionForce);
      }
      
      return output;
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
         rigidBodies = collisionResults.stream().map(CollisionResult::getBodyA).collect(Collectors.toList());
         wrenchMap = collisionResults.stream().collect(Collectors.toMap(CollisionResult::getBodyA, CollisionResult::getWrenchOnA));
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
