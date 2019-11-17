package us.ihmc.scs2.simulation.collision;

import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;
import us.ihmc.scs2.simulation.collision.shape.CollisionShape;

public class CollidableRigidBody
{
   private final RigidBodyBasics owner;
   private final CollisionShape collisionShape;

   public CollidableRigidBody(RigidBodyBasics owner, CollisionShape collisionShape)
   {
      this.owner = owner;
      this.collisionShape = collisionShape;
   }

   public void updateCollisionShape()
   {
      if (owner.isRootBody())
         collisionShape.getTransformToWorld().set(owner.getBodyFixedFrame().getTransformToRoot());
      else
         collisionShape.getTransformToWorld().set(owner.getParentJoint().getFrameAfterJoint().getTransformToRoot());
   }

   public CollisionShape getCollisionShape()
   {
      return collisionShape;
   }

   public RigidBodyBasics getOwner()
   {
      return owner;
   }
}
