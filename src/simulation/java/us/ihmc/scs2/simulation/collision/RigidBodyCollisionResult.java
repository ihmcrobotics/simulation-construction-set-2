package us.ihmc.scs2.simulation.collision;

import us.ihmc.euclid.shape.collision.interfaces.EuclidShape3DCollisionResultReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

public class RigidBodyCollisionResult
{
   private final RigidBodyBasics bodyA;
   private final RigidBodyBasics bodyB;
   private final EuclidShape3DCollisionResultReadOnly shape3DCollisionResult;

   public RigidBodyCollisionResult(RigidBodyBasics bodyA, RigidBodyBasics bodyB, EuclidShape3DCollisionResultReadOnly shape3DCollisionResult)
   {
      this.bodyA = bodyA;
      this.bodyB = bodyB;
      this.shape3DCollisionResult = shape3DCollisionResult;
   }

   public RigidBodyBasics getBodyA()
   {
      return bodyA;
   }

   public RigidBodyBasics getBodyB()
   {
      return bodyB;
   }

   public EuclidShape3DCollisionResultReadOnly getShape3DCollisionResult()
   {
      return shape3DCollisionResult;
   }
}
