package us.ihmc.scs2.simulation.shapes;

import static us.ihmc.euclid.referenceFrame.tools.EuclidFrameFactories.newFixedFrameUnitVector3DBasics;
import static us.ihmc.euclid.referenceFrame.tools.EuclidFrameFactories.newObservableFixedFramePoint3DBasics;
import static us.ihmc.euclid.referenceFrame.tools.EuclidFrameFactories.newObservableFixedFrameUnitVector3DBasics;
import static us.ihmc.euclid.tools.EuclidCoreIOTools.DEFAULT_FORMAT;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.Axis3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FixedFramePoint3DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FixedFrameUnitVector3DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FrameCapsule3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FramePoint3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FrameVector3DReadOnly;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameFactories;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameShapeIOTools;
import us.ihmc.euclid.shape.primitives.interfaces.Capsule3DReadOnly;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DChangeListener;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.scs2.simulation.shapes.STPShape3DTools.STPCapsule3DSupportingVertexCalculator;
import us.ihmc.scs2.simulation.shapes.interfaces.FrameSTPCapsule3DBasics;
import us.ihmc.scs2.simulation.shapes.interfaces.FrameSTPCapsule3DReadOnly;
import us.ihmc.scs2.simulation.shapes.interfaces.STPCapsule3DReadOnly;

public class FrameSTPCapsule3D implements FrameSTPCapsule3DBasics
{
   /** The reference frame in which this shape is expressed. */
   private ReferenceFrame referenceFrame;
   private final List<Shape3DChangeListener> changeListeners = new ArrayList<>();
   private double minimumMargin, maximumMargin;
   private double largeRadius, smallRadius;
   private final STPCapsule3DSupportingVertexCalculator supportingVertexCalculator = new STPCapsule3DSupportingVertexCalculator();

   /** Position of this capsule's center. */
   private final FixedFramePoint3DBasics position = newObservableFixedFramePoint3DBasics(this, (axis, value) -> notifyChangeListeners(), null);
   /** Axis of revolution of this capsule. */
   private final FixedFrameUnitVector3DBasics axis = newObservableFixedFrameUnitVector3DBasics((axis, value) -> notifyChangeListeners(),
                                                                                               null,
                                                                                               newFixedFrameUnitVector3DBasics(this, Axis3D.Z));

   /** This capsule radius. */
   private double radius;
   /** This capsule length. */
   private double length;
   /** This capsule half-length. */
   private double halfLength;

   /** Position of the top half-sphere center linked to this capsule properties. */
   private final FramePoint3DReadOnly topCenter = EuclidFrameFactories.newLinkedFramePoint3DReadOnly(this,
                                                                                                     () -> halfLength * axis.getX() + position.getX(),
                                                                                                     () -> halfLength * axis.getY() + position.getY(),
                                                                                                     () -> halfLength * axis.getZ() + position.getZ());
   /** Position of the bottom half-sphere center linked to this capsule properties. */
   private final FramePoint3DReadOnly bottomCenter = EuclidFrameFactories.newLinkedFramePoint3DReadOnly(this,
                                                                                                        () -> -halfLength * axis.getX() + position.getX(),
                                                                                                        () -> -halfLength * axis.getY() + position.getY(),
                                                                                                        () -> -halfLength * axis.getZ() + position.getZ());

   private boolean stpRadiiDirty = true;

   /**
    * Creates a new capsule which axis is along the z-axis, a length of 1, and radius of 0.5 and
    * initializes its reference frame to {@link ReferenceFrame#getWorldFrame()}.
    */
   public FrameSTPCapsule3D()
   {
      this(ReferenceFrame.getWorldFrame());
   }

   /**
    * Creates a new capsule which axis is along the z-axis, a length of 1, and radius of 0.5 and
    * initializes its reference frame.
    *
    * @param referenceFrame this shape initial reference frame.
    */
   public FrameSTPCapsule3D(ReferenceFrame referenceFrame)
   {
      this(referenceFrame, 1.0, 0.5);
   }

   /**
    * Creates a new capsule which axis is along the z-axis and initializes its size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param length         the length of this capsule.
    * @param radius         the radius of this capsule.
    * @throws IllegalArgumentException if {@code length} or {@code radius} is negative.
    */
   public FrameSTPCapsule3D(ReferenceFrame referenceFrame, double length, double radius)
   {
      setReferenceFrame(referenceFrame);
      setSize(length, radius);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param position       the position of the center. Not modified.
    * @param axis           the axis of revolution. Not modified.
    * @param length         the length of this capsule.
    * @param radius         the radius of this capsule.
    * @throws IllegalArgumentException if {@code length} or {@code radius} is negative.
    */
   public FrameSTPCapsule3D(ReferenceFrame referenceFrame, Point3DReadOnly position, Vector3DReadOnly axis, double length, double radius)
   {
      setIncludingFrame(referenceFrame, position, axis, length, radius);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D and initializes its pose and size.
    *
    * @param position the position of the center. Not modified.
    * @param axis     the axis of revolution. Not modified.
    * @param length   the length of this capsule.
    * @param radius   the radius of this capsule.
    * @throws IllegalArgumentException        if {@code length} or {@code radius} is negative.
    * @throws ReferenceFrameMismatchException if the frame argument are not expressed in the same
    *                                         reference frame.
    */
   public FrameSTPCapsule3D(FramePoint3DReadOnly position, FrameVector3DReadOnly axis, double length, double radius)
   {
      setIncludingFrame(position, axis, length, radius);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D identical to {@code other}.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param other          the other capsule to copy. Not modified.
    */
   public FrameSTPCapsule3D(ReferenceFrame referenceFrame, Capsule3DReadOnly other)
   {
      setIncludingFrame(referenceFrame, other);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D identical to {@code other}.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param other          the other capsule to copy. Not modified.
    */
   public FrameSTPCapsule3D(ReferenceFrame referenceFrame, STPCapsule3DReadOnly other)
   {
      setIncludingFrame(referenceFrame, other);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D identical to {@code other}.
    *
    * @param other the other capsule to copy. Not modified.
    */
   public FrameSTPCapsule3D(FrameCapsule3DReadOnly other)
   {
      setIncludingFrame(other);
      setupListeners();
   }

   /**
    * Creates a new capsule 3D identical to {@code other}.
    *
    * @param other the other capsule to copy. Not modified.
    */
   public FrameSTPCapsule3D(FrameSTPCapsule3DReadOnly other)
   {
      setIncludingFrame(other);
      setupListeners();
   }

   private void setupListeners()
   {
      addChangeListener(() -> stpRadiiDirty = true);
   }

   /** {@inheritDoc} */
   @Override
   public void setReferenceFrame(ReferenceFrame referenceFrame)
   {
      this.referenceFrame = referenceFrame;
   }

   /** {@inheritDoc} */
   @Override
   public void setRadius(double radius)
   {
      if (radius < 0.0)
         throw new IllegalArgumentException("The radius of a Capsule3D cannot be negative: " + radius);
      this.radius = radius;
      notifyChangeListeners();
   }

   /** {@inheritDoc} */
   @Override
   public void setLength(double length)
   {
      if (length < 0.0)
         throw new IllegalArgumentException("The length of a Capsule3D cannot be negative: " + length);
      this.length = length;
      halfLength = 0.5 * length;
      notifyChangeListeners();
   }

   /** {@inheritDoc} */
   @Override
   public ReferenceFrame getReferenceFrame()
   {
      return referenceFrame;
   }

   /** {@inheritDoc} */
   @Override
   public double getRadius()
   {
      return radius;
   }

   /** {@inheritDoc} */
   @Override
   public double getLength()
   {
      return length;
   }

   /** {@inheritDoc} */
   @Override
   public double getHalfLength()
   {
      return halfLength;
   }

   /** {@inheritDoc} */
   @Override
   public FixedFramePoint3DBasics getPosition()
   {
      return position;
   }

   /** {@inheritDoc} */
   @Override
   public FixedFrameUnitVector3DBasics getAxis()
   {
      return axis;
   }

   /** {@inheritDoc} */
   @Override
   public FramePoint3DReadOnly getTopCenter()
   {
      return topCenter;
   }

   /** {@inheritDoc} */
   @Override
   public FramePoint3DReadOnly getBottomCenter()
   {
      return bottomCenter;
   }

   @Override
   public double getMinimumMargin()
   {
      return minimumMargin;
   }

   @Override
   public double getMaximumMargin()
   {
      return maximumMargin;
   }

   @Override
   public double getSmallRadius()
   {
      updateRadii();
      return smallRadius;
   }

   @Override
   public double getLargeRadius()
   {
      updateRadii();
      return largeRadius;
   }

   @Override
   public void setMargins(double minimumMargin, double maximumMargin)
   {
      if (maximumMargin <= minimumMargin)
         throw new IllegalArgumentException("The maximum margin has to be strictly greater that the minimum margin, max margin: " + maximumMargin
               + ", min margin: " + minimumMargin);
      this.minimumMargin = minimumMargin;
      this.maximumMargin = maximumMargin;
      stpRadiiDirty = true;
   }

   /**
    * <pre>
    * r = h
    *      r^2 - g^2 - 0.25 * l<sub>max</sub>
    * R = ------------------------
    *           2 * (r - g)
    * </pre>
    *
    * where:
    * <ul>
    * <li><tt>R</tt> is {@link #largeRadius}
    * <li><tt>r</tt> is {@link #smallRadius}
    * <li><tt>h</tt> is {@link #minimumMargin}
    * <li><tt>g</tt> is {@link #maximumMargin}
    * <li><tt>l<sub>max</max></tt> is the maximum edge length that needs to be covered by the large
    * bounding sphere.
    * </ul>
    */
   protected void updateRadii()
   {
      if (!stpRadiiDirty)
         return;

      stpRadiiDirty = false;

      if (minimumMargin == 0.0 && maximumMargin == 0.0)
      {
         smallRadius = Double.NaN;
         largeRadius = Double.NaN;
      }
      else
      {
         smallRadius = radius + minimumMargin;
         largeRadius = radius + STPShape3DTools.computeLargeRadiusFromMargins(minimumMargin, maximumMargin, EuclidCoreTools.square(length));
      }
   }

   @Override
   public boolean getSupportingVertex(Vector3DReadOnly supportDirection, Point3DBasics supportingVertexToPack)
   {
      return supportingVertexCalculator.getSupportingVertex(this, getSmallRadius(), getLargeRadius(), supportDirection, supportingVertexToPack);
   }

   /**
    * Notifies the internal listeners that this shape has changed.
    */
   public void notifyChangeListeners()
   {
      for (int i = 0; i < changeListeners.size(); i++)
      {
         changeListeners.get(i).changed();
      }
   }

   /**
    * Registers a list of listeners to be notified when this shape changes.
    *
    * @param listeners the listeners to register.
    */
   public void addChangeListeners(List<? extends Shape3DChangeListener> listeners)
   {
      for (int i = 0; i < listeners.size(); i++)
      {
         addChangeListener(listeners.get(i));
      }
   }

   /**
    * Registers a listener to be notified when this shape changes.
    *
    * @param listener the listener to register.
    */
   public void addChangeListener(Shape3DChangeListener listener)
   {
      changeListeners.add(listener);
   }

   /**
    * Removes a previously registered listener.
    * <p>
    * This listener will no longer be notified of changes from this pose.
    * </p>
    *
    * @param listener the listener to remove.
    * @return {@code true} if the listener was removed successful, {@code false} if the listener could
    *         not be found.
    */
   public boolean removeChangeListener(Shape3DChangeListener listener)
   {
      return changeListeners.remove(listener);
   }

   @Override
   public FrameSTPCapsule3D copy()
   {
      return new FrameSTPCapsule3D(this);
   }

   /**
    * Tests if the given {@code object}'s class is the same as this, in which case the method returns
    * {@link #equals(FrameSTPCapsule3DReadOnly)}, it returns {@code false} otherwise.
    *
    * @param object the object to compare against this. Not modified.
    * @return {@code true} if {@code object} and this are exactly equal, {@code false} otherwise.
    */
   @Override
   public boolean equals(Object object)
   {
      if (object instanceof FrameSTPCapsule3DReadOnly)
         return FrameSTPCapsule3DBasics.super.equals((FrameSTPCapsule3DReadOnly) object);
      else
         return false;
   }

   /**
    * Calculates and returns a hash code value from the value of each component of this capsule 3D.
    *
    * @return the hash code value for this capsule 3D.
    */
   @Override
   public int hashCode()
   {
      long hash = 1L;
      hash = EuclidHashCodeTools.toLongHashCode(length, radius);
      hash = EuclidHashCodeTools.combineHashCode(hash, EuclidHashCodeTools.toLongHashCode(position, axis));
      hash = EuclidHashCodeTools.combineHashCode(hash, EuclidHashCodeTools.toLongHashCode(minimumMargin, maximumMargin));
      return EuclidHashCodeTools.toIntHashCode(hash);
   }

   /**
    * Provides a {@code String} representation of this capsule 3D as follows:
    *
    * <pre>
    * STP Capsule 3D: [position: (-0.362, -0.617,  0.066 ), axis: ( 0.634, -0.551, -0.543 ), length:  0.170, radius:  0.906, small radius: 0.001, large radius: 1.000] - worldFrame
    * </pre>
    *
    * @return the {@code String} representing this capsule 3D.
    */
   @Override
   public String toString()
   {
      String stpSuffix = String.format(", small radius: " + DEFAULT_FORMAT + ", large radius: " + DEFAULT_FORMAT + "]", getSmallRadius(), getLargeRadius());
      return "STP " + EuclidFrameShapeIOTools.getFrameCapsule3DString(this).replace("]", stpSuffix);
   }
}
