package us.ihmc.scs2.simulation.collision;

import us.ihmc.euclid.interfaces.EpsilonComparable;
import us.ihmc.euclid.interfaces.GeometricallyComparable;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameVector3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.exceptions.ReferenceFrameMismatchException;
import us.ihmc.euclid.shape.collision.interfaces.EuclidShape3DCollisionResultBasics;
import us.ihmc.euclid.shape.collision.interfaces.EuclidShape3DCollisionResultReadOnly;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.shape.tools.EuclidShapeIOTools;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.mecano.spatial.Wrench;

/**
 * Class for holding the result of a collision query between two shapes and their respective
 * reference frame information.
 * 
 * @author Sylvain Bertrand
 */
public class CollisionResult implements EuclidShape3DCollisionResultBasics, EpsilonComparable<CollisionResult>, GeometricallyComparable<CollisionResult>
{
   /** Whether the shapes are colliding. */
   private boolean shapesAreColliding;
   /** The collision distance, either separation distance or penetration depth. */
   private double signedDistance;

   private Collidable collidableA;
   private Collidable collidableB;

   /** The first shape in the collision. */
   private Shape3DReadOnly shapeA;
   /** The second shape in the collision. */
   private Shape3DReadOnly shapeB;

   /** The reference frame in which {@code shapeA} is expressed. */
   private ReferenceFrame frameA;
   /** The reference frame in which {@code shapeB} is expressed. */
   private ReferenceFrame frameB;

   /** The key point on the shape A. */
   private final FramePoint3D pointOnA = new FramePoint3D();
   /** The surface normal at {@code pointOnA}. */
   private final FrameVector3D normalOnA = new FrameVector3D();

   /** The key point on the shape B. */
   private final FramePoint3D pointOnB = new FramePoint3D();
   /** The surface normal at {@code pointOnB}. */
   private final FrameVector3D normalOnB = new FrameVector3D();

   /** The resulting wrench from the collision to apply on shape A. */
   private final Wrench wrenchOnA = new Wrench();
   /** The resulting wrench from the collision to apply on shape B. */
   private final Wrench wrenchOnB = new Wrench();

   /**
    * Creates a new empty collision result.
    */
   public CollisionResult()
   {
   }

   /** {@inheritDoc} */
   @Override
   public void setShapesAreColliding(boolean shapesAreColliding)
   {
      this.shapesAreColliding = shapesAreColliding;
   }

   /** {@inheritDoc} */
   @Override
   public void setSignedDistance(double distance)
   {
      this.signedDistance = distance;
   }

   public void setCollidableA(Collidable collidableA)
   {
      this.collidableA = collidableA;
   }

   public void setCollidableB(Collidable collidableB)
   {
      this.collidableB = collidableB;
   }

   /** {@inheritDoc} */
   @Override
   public void setShapeA(Shape3DReadOnly shapeA)
   {
      this.shapeA = shapeA;
   }

   /** {@inheritDoc} */
   @Override
   public void setShapeB(Shape3DReadOnly shapeB)
   {
      this.shapeB = shapeB;
   }

   /**
    * Sets the reference frame in which {@code shapeA} is expressed.
    * <p>
    * This does <b>not</b> change the reference frame of {@code pointOnA} nor {@code normalOnA}.
    * </p>
    * 
    * @param frameA the reference frame for {@code shapeA}.
    */
   public void setFrameA(ReferenceFrame frameA)
   {
      this.frameA = frameA;
   }

   /**
    * Sets the reference frame in which {@code shapeB} is expressed.
    * <p>
    * This does <b>not</b> change the reference frame of {@code pointOnB} nor {@code normalOnB}.
    * </p>
    * 
    * @param frameB the reference frame for {@code shapeB}.
    */
   public void setFrameB(ReferenceFrame frameB)
   {
      this.frameB = frameB;
   }

   @Override
   public void swapShapes()
   {
      EuclidShape3DCollisionResultBasics.super.swapShapes();
      ReferenceFrame tempFrame = frameA;
      frameA = frameB;
      frameB = tempFrame;

      tempFrame = wrenchOnA.getReferenceFrame();
      ReferenceFrame tempBodyFrame = wrenchOnA.getBodyFrame();
      double tempAngularX = wrenchOnA.getAngularPartX();
      double tempAngularY = wrenchOnA.getAngularPartY();
      double tempAngularZ = wrenchOnA.getAngularPartZ();
      double tempLinearX = wrenchOnA.getLinearPartX();
      double tempLinearY = wrenchOnA.getLinearPartY();
      double tempLinearZ = wrenchOnA.getLinearPartZ();
      wrenchOnA.setIncludingFrame(wrenchOnB);
      wrenchOnB.setReferenceFrame(tempFrame);
      wrenchOnB.setBodyFrame(tempBodyFrame);
      wrenchOnB.getAngularPart().set(tempAngularX, tempAngularY, tempAngularZ);
      wrenchOnB.getLinearPart().set(tempLinearX, tempLinearY, tempLinearZ);
   }

   /** {@inheritDoc} */
   @Override
   public boolean areShapesColliding()
   {
      return shapesAreColliding;
   }

   /** {@inheritDoc} */
   @Override
   public double getSignedDistance()
   {
      return signedDistance;
   }

   public Collidable getCollidableA()
   {
      return collidableA;
   }

   public Collidable getCollidableB()
   {
      return collidableB;
   }

   /** {@inheritDoc} */
   @Override
   public Shape3DReadOnly getShapeA()
   {
      return shapeA;
   }

   /** {@inheritDoc} */
   @Override
   public Shape3DReadOnly getShapeB()
   {
      return shapeB;
   }

   /**
    * Gets the reference frame in which {@code shapeA} is expressed.
    * 
    * @return the reference frame for {@code shapeA}.
    */
   public ReferenceFrame getFrameA()
   {
      return frameA;
   }

   /**
    * Gets the reference frame in which {@code shapeB} is expressed.
    * 
    * @return the reference frame for {@code shapeB}.
    */
   public ReferenceFrame getFrameB()
   {
      return frameB;
   }

   /** {@inheritDoc} */
   @Override
   public FramePoint3D getPointOnA()
   {
      return pointOnA;
   }

   /** {@inheritDoc} */
   @Override
   public FrameVector3D getNormalOnA()
   {
      return normalOnA;
   }

   /** {@inheritDoc} */
   @Override
   public FramePoint3D getPointOnB()
   {
      return pointOnB;
   }

   /** {@inheritDoc} */
   @Override
   public FrameVector3D getNormalOnB()
   {
      return normalOnB;
   }

   /**
    * Gets, if available, the wrench to apply to shape A at {@code pointOnA}.
    * 
    * @return the wrench at {@code pointOnA}.
    */
   public Wrench getWrenchOnA()
   {
      return wrenchOnA;
   }

   /**
    * Gets, if available, the wrench to apply to shape B at {@code pointOnB}.
    * 
    * @return the wrench at {@code pointOnB}.
    */
   public Wrench getWrenchOnB()
   {
      return wrenchOnB;
   }

   /**
    * Tests on a per component basis if {@code other} and {@code this} are equal to an {@code epsilon}.
    * <p>
    * Two instances of collision frame results are not considered equal when their respective frames
    * are different.
    * </p>
    * 
    * @param other   the other collision result to compare against this. Not modified.
    * @param epsilon tolerance to use when comparing each component.
    * @return {@code true} if the two collision results are equal component-wise, {@code false}
    *         otherwise.
    */
   @Override
   public boolean epsilonEquals(CollisionResult other, double epsilon)
   {
      if (frameA == null ? other.frameA != null : frameA != other.frameA)
         return false;
      if (frameB == null ? other.frameB != null : frameB != other.frameB)
         return false;
      if (!wrenchOnA.epsilonEquals(other.wrenchOnA, epsilon))
         return false;
      if (!wrenchOnB.epsilonEquals(other.wrenchOnB, epsilon))
         return false;
      return EuclidShape3DCollisionResultBasics.super.epsilonEquals(other, epsilon);
   }

   /**
    * Tests each feature of {@code this} against {@code other} for geometric similarity.
    * 
    * @param other   the other collision result to compare against this. Not modified.
    * @param epsilon tolerance to use when comparing each feature.
    * @return {@code true} if the two collision results are considered geometrically similar,
    *         {@code false} otherwise.
    * @throws ReferenceFrameMismatchException if {@code other} does not hold the same reference frames
    *                                         as {@code this}.
    */
   @Override
   public boolean geometricallyEquals(CollisionResult other, double epsilon)
   {
      if (areShapesColliding() != other.areShapesColliding())
         return false;

      if (!EuclidCoreTools.epsilonEquals(getSignedDistance(), other.getSignedDistance(), epsilon))
         return false;

      if (getShapeA() != null || getShapeB() != null || other.getShapeA() != null || other.getShapeB() != null)
      {
         boolean swap = getShapeA() != other.getShapeA();
         Shape3DReadOnly otherShapeA = swap ? other.getShapeB() : other.getShapeA();
         Shape3DReadOnly otherShapeB = swap ? other.getShapeA() : other.getShapeB();
         Point3DReadOnly otherPointOnA = swap ? other.getPointOnB() : other.getPointOnA();
         Point3DReadOnly otherPointOnB = swap ? other.getPointOnA() : other.getPointOnB();
         Vector3DReadOnly otherNormalOnA = swap ? other.getNormalOnB() : other.getNormalOnA();
         Vector3DReadOnly otherNormalOnB = swap ? other.getNormalOnA() : other.getNormalOnB();
         ReferenceFrame otherFrameA = swap ? other.getFrameB() : other.getFrameA();
         ReferenceFrame otherFrameB = swap ? other.getFrameA() : other.getFrameB();
         Wrench otherWrenchOnA = swap ? other.getWrenchOnB() : other.getWrenchOnA();
         Wrench otherWrenchOnB = swap ? other.getWrenchOnA() : other.getWrenchOnB();

         if (getShapeA() != otherShapeA)
            return false;
         if (getShapeB() != otherShapeB)
            return false;

         if (getPointOnA().containsNaN() ? !otherPointOnA.containsNaN() : !getPointOnA().geometricallyEquals(otherPointOnA, epsilon))
            return false;
         if (getPointOnB().containsNaN() ? !otherPointOnB.containsNaN() : !getPointOnB().geometricallyEquals(otherPointOnB, epsilon))
            return false;

         if (getNormalOnA().containsNaN() ? !otherNormalOnA.containsNaN() : !getNormalOnA().geometricallyEquals(otherNormalOnA, epsilon))
            return false;
         if (getNormalOnB().containsNaN() ? !otherNormalOnB.containsNaN() : !getNormalOnB().geometricallyEquals(otherNormalOnB, epsilon))
            return false;

         if (getFrameA() != null)
            getFrameA().checkReferenceFrameMatch(otherFrameA);
         else if (otherFrameA != null)
            return false;
         if (getFrameB() != null)
            getFrameB().checkReferenceFrameMatch(otherFrameA);
         else if (otherFrameB != null)
            return false;

         if (getWrenchOnA().containsNaN() ? !otherWrenchOnA.containsNaN() : !getWrenchOnA().geometricallyEquals(otherWrenchOnA, epsilon))
            return false;
         if (getWrenchOnB().containsNaN() ? !otherWrenchOnB.containsNaN() : !getWrenchOnB().geometricallyEquals(otherWrenchOnB, epsilon))
            return false;

         return true;
      }
      else
      {
         boolean swap = !getPointOnA().geometricallyEquals(other.getPointOnA(), epsilon);
         Point3DReadOnly otherPointOnA = swap ? other.getPointOnB() : other.getPointOnA();
         Point3DReadOnly otherPointOnB = swap ? other.getPointOnA() : other.getPointOnB();
         Vector3DReadOnly otherNormalOnA = swap ? other.getNormalOnB() : other.getNormalOnA();
         Vector3DReadOnly otherNormalOnB = swap ? other.getNormalOnA() : other.getNormalOnB();
         ReferenceFrame otherFrameA = swap ? other.getFrameB() : other.getFrameA();
         ReferenceFrame otherFrameB = swap ? other.getFrameA() : other.getFrameB();
         Wrench otherWrenchOnA = swap ? other.getWrenchOnB() : other.getWrenchOnA();
         Wrench otherWrenchOnB = swap ? other.getWrenchOnA() : other.getWrenchOnB();

         if (getPointOnA().containsNaN() ? !otherPointOnA.containsNaN() : !getPointOnA().geometricallyEquals(otherPointOnA, epsilon))
            return false;
         if (getPointOnB().containsNaN() ? !otherPointOnB.containsNaN() : !getPointOnB().geometricallyEquals(otherPointOnB, epsilon))
            return false;

         if (getNormalOnA().containsNaN() ? !otherNormalOnA.containsNaN() : !getNormalOnA().geometricallyEquals(otherNormalOnA, epsilon))
            return false;
         if (getNormalOnB().containsNaN() ? !otherNormalOnB.containsNaN() : !getNormalOnB().geometricallyEquals(otherNormalOnB, epsilon))
            return false;

         if (getFrameA() != null)
            getFrameA().checkReferenceFrameMatch(otherFrameA);
         else if (otherFrameA != null)
            return false;
         if (getFrameB() != null)
            getFrameB().checkReferenceFrameMatch(otherFrameA);
         else if (otherFrameB != null)
            return false;

         if (getWrenchOnA().containsNaN() ? !otherWrenchOnA.containsNaN() : !getWrenchOnA().geometricallyEquals(otherWrenchOnA, epsilon))
            return false;
         if (getWrenchOnB().containsNaN() ? !otherWrenchOnB.containsNaN() : !getWrenchOnB().geometricallyEquals(otherWrenchOnB, epsilon))
            return false;

         return true;
      }
   }

   /**
    * Tests if the given {@code object}'s class is the same as this, in which case the method returns
    * {@link #equals(EuclidShape3DCollisionResultReadOnly)}, it returns {@code false} otherwise.
    *
    * @param object the object to compare against this. Not modified.
    * @return {@code true} if {@code object} and this are exactly equal, {@code false} otherwise.
    */
   @Override
   public boolean equals(Object object)
   {
      if (object instanceof CollisionResult)
         return equals((CollisionResult) object);
      if (object instanceof EuclidShape3DCollisionResultReadOnly)
         return EuclidShape3DCollisionResultBasics.super.equals((EuclidShape3DCollisionResultReadOnly) object);
      else
         return false;
   }

   /**
    * Tests on a per component basis, if this collision result is exactly equal to {@code other}.
    * <p>
    * Two instances of collision frame results are not considered equal when their respective frames
    * are different.
    * </p>
    *
    * @param other the other collision result to compare against this. Not modified.
    * @return {@code true} if the two collision results are exactly equal component-wise, {@code false}
    *         otherwise.
    */
   public boolean equals(CollisionResult other)
   {
      if (other == this)
         return true;
      if (other == null)
         return false;

      if (frameA == null ? other.frameA != null : frameA != other.frameA)
         return false;
      if (frameB == null ? other.frameB != null : frameB != other.frameB)
         return false;
      if (!wrenchOnA.equals(other.wrenchOnA))
         return false;
      if (!wrenchOnB.equals(other.wrenchOnB))
         return false;

      return EuclidShape3DCollisionResultBasics.super.equals(other);
   }

   /**
    * Provides a {@code String} representation of this collision result as follows:<br>
    * When shapes are colliding:
    * 
    * <pre>
    * Collision test result: colliding, depth: 0.539
    * Shape A: Box3D, location: ( 0.540,  0.110,  0.319 ), normal: ( 0.540,  0.110,  0.319 )
    * Shape B: Capsule3D, location: ( 0.540,  0.110,  0.319 ), normal: ( 0.540,  0.110,  0.319 )
    * </pre>
    * 
    * When shapes are not colliding:
    * 
    * <pre>
    * Collision test result: non-colliding, separating distance: 0.539
    * Shape A: Box3D, location: ( 0.540,  0.110,  0.319 ), normal: ( 0.540,  0.110,  0.319 )
    * Shape B: Capsule3D, location: ( 0.540,  0.110,  0.319 ), normal: ( 0.540,  0.110,  0.319 )
    * </pre>
    * 
    * @return the {@code String} representing this collision result.
    */
   @Override
   public String toString()
   {
      return EuclidShapeIOTools.getEuclidShape3DCollisionResultString(this);
   }
}
