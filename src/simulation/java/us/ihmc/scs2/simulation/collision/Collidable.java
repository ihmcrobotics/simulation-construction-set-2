package us.ihmc.scs2.simulation.collision;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.shape.primitives.Capsule3D;
import us.ihmc.euclid.shape.primitives.Sphere3D;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.RigidBodyBasics;

/**
 * Defines a collision shape that represents entirely or partially the geometry of a rigid-body.
 * <p>
 * Multiple {@link Collidable}s can be used to more accurately represent a rigid-body.
 * </p>
 * 
 * @author Sylvain Bertrand
 */
public class Collidable
{
   /** The rigid-body this collidable represents. */
   private final RigidBodyBasics rigidBody;
   /**
    * Collision identifier for this collidable. Use {@link CollidableHelper} to compute
    * collision masks and groups.
    */
   private final int collisionMask;
   /**
    * Collision identifiers of other collidables that this collidable is allowed to collide with. Use
    * {@link CollidableHelper} to compute collision masks and groups.
    */
   private final int collisionGroup;
   /**
    * The shape of this collidable. It is strongly recommended to use only {@link Sphere3D} and
    * {@link Capsule3D} as collision evaluations are extremely fast with these shapes.
    */
   private final Shape3DReadOnly shape;
   /**
    * The frame the shape is expressed in. Usually the body-fixed frame of {@code rigidBody} or the
    * frame after it parent joint.
    */
   private final ReferenceFrame shapeFrame;

   /**
    * Creates a new collidable that represents entirely or partially the geometry of the given
    * {@code rigidBody}.
    * 
    * @param rigidBody      the rigid-body this collidable represents.
    * @param collisionMask  collision identifier for this collidable. Use
    *                       {@link CollidableHelper} to compute collision masks and groups.
    * @param collisionGroup collision identifiers of other collidables that this collidable is allowed
    *                       to collide with. Use {@link CollidableHelper} to compute
    *                       collision masks and groups.
    * @param shape          the shape of this collidable. It is strongly recommended to use only
    *                       {@link Sphere3D} and {@link Capsule3D} as collision evaluations are
    *                       extremely fast with these shapes.
    * @param shapeFrame     the frame the shape is expressed in. Usually the body-fixed frame of
    *                       {@code rigidBody} or the frame after it parent joint.
    */
   public Collidable(RigidBodyBasics rigidBody, int collisionMask, int collisionGroup, Shape3DReadOnly shape, ReferenceFrame shapeFrame)
   {
      this.rigidBody = rigidBody;
      this.collisionMask = collisionMask;
      this.collisionGroup = collisionGroup;
      this.shape = shape;
      this.shapeFrame = shapeFrame;
   }

   /**
    * Performs a quick test to check if this collidable and {@code other} are allowed to collide with
    * each other regarding their respective identifiers.
    * 
    * @param other the query.
    * @return {@code true} if the 2 collidables are allowed to collide, {@code false} ortherwise.
    */
   public boolean isCollidableWith(Collidable other)
   {
      if (other == this)
         return false;
      if (collisionGroup == -1 && collisionMask == -1)
         return true;
      if ((collisionGroup & other.collisionMask) == 0x00)
         return false;
      if ((other.collisionGroup & collisionMask) == 0x00)
         return false;
      return true;
   }

   /**
    * Performs a collision evaluation between this collidable and {@code other} in order to calculate
    * their closest point, separating/penetration distance, etc.
    * 
    * @param other the query.
    * @return the result of the evaluation.
    */
   public CollisionResult evaluateCollision(Collidable other)
   {
      CollisionResult result = new CollisionResult();
      evaluateCollision(other, result);
      return result;
   }

   /**
    * Performs a collision evaluation between this collidable and {@code other} in order to calculate
    * their closest point, separating/penetration distance, etc.
    * 
    * @param other        the query. Not modified.
    * @param resultToPack where the result of the evaluation is stored. Modified.
    */
   public void evaluateCollision(Collidable other, CollisionResult resultToPack)
   {
      CollisionTools.evaluateShape3DShape3DCollision(shape, shapeFrame, other.shape, other.shapeFrame, resultToPack);
      resultToPack.setCollidableA(this);
      resultToPack.setCollidableB(other);
   }

   /**
    * Get the rigid-body this collidable represents.
    * 
    * @return the rigid-body this collidable represents.
    */
   public RigidBodyBasics getRigidBody()
   {
      return rigidBody;
   }

   /**
    * Collision identifier for this collidable used with {@link #isCollidableWith(Collidable)}.
    * 
    * @return the mask's value.
    */
   public int getCollisionMask()
   {
      return collisionMask;
   }

   /**
    * Collision identifiers of other collidables that this collidable is allowed to collide with. It is
    * used with {@link #isCollidableWith(Collidable)}.
    * 
    * @return the group's value.
    */
   public int getCollisionGroup()
   {
      return collisionGroup;
   }

   /**
    * The shape of this collidable.
    * 
    * @return the shape.
    */
   public Shape3DReadOnly getShape()
   {
      return shape;
   }

   /**
    * The frame the shape is expressed in.
    * 
    * @return the shape frame.
    */
   public ReferenceFrame getShapeFrame()
   {
      return shapeFrame;
   }
}
