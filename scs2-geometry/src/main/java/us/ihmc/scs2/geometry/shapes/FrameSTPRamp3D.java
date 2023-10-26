package us.ihmc.scs2.geometry.shapes;

import static us.ihmc.euclid.tools.EuclidCoreIOTools.DEFAULT_FORMAT;

import java.util.List;

import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.referenceFrame.FrameRamp3D;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.interfaces.FixedFrameShape3DPoseBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FixedFrameVector3DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FrameOrientation3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FramePoint3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FramePose3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FrameRamp3DReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FrameRampPolytope3DView;
import us.ihmc.euclid.referenceFrame.interfaces.FrameShape3DPoseReadOnly;
import us.ihmc.euclid.referenceFrame.interfaces.FrameVector3DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FrameVector3DReadOnly;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameShapeIOTools;
import us.ihmc.euclid.shape.primitives.interfaces.IntermediateVariableSupplier;
import us.ihmc.euclid.shape.primitives.interfaces.Ramp3DReadOnly;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DChangeListener;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.scs2.geometry.shapes.STPShape3DTools.STPRamp3DSupportingVertexCalculator;
import us.ihmc.scs2.geometry.shapes.interfaces.FrameSTPRamp3DBasics;
import us.ihmc.scs2.geometry.shapes.interfaces.FrameSTPRamp3DReadOnly;
import us.ihmc.scs2.geometry.shapes.interfaces.STPRamp3DReadOnly;

public class FrameSTPRamp3D implements FrameSTPRamp3DBasics
{
   private double minimumMargin, maximumMargin;
   private double largeRadius, smallRadius;
   private final FrameRamp3D rawRamp3D = new FrameRamp3D();
   private final STPRamp3DSupportingVertexCalculator supportingVertexCalculator = new STPRamp3DSupportingVertexCalculator();

   private boolean stpRadiiDirty = true;

   /**
    * Creates a new ramp 3D and initializes its length, width, and height to {@code 1.0} and
    * initializes its reference frame to {@link ReferenceFrame#getWorldFrame()}.
    */
   public FrameSTPRamp3D()
   {
      this(ReferenceFrame.getWorldFrame());
   }

   /**
    * Creates a new ramp 3D and initializes its length, width, and height to {@code 1.0} and
    * initializes its reference frame.
    *
    * @param referenceFrame this shape initial reference frame.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame)
   {
      this(referenceFrame, 1.0, 1.0, 1.0);
   }

   /**
    * Creates a new ramp 3D and initializes its size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param sizeX          the size of this ramp along the x-axis.
    * @param sizeY          the size of this ramp along the y-axis.
    * @param sizeZ          the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, double sizeX, double sizeY, double sizeZ)
   {
      setReferenceFrame(referenceFrame);
      getSize().set(sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param size           the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Vector3DReadOnly size)
   {
      setReferenceFrame(referenceFrame);
      getSize().set(size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param position       the position of this ramp. Not modified.
    * @param orientation    the orientation of this ramp. Not modified.
    * @param sizeX          the size of this ramp along the x-axis.
    * @param sizeY          the size of this ramp along the y-axis.
    * @param sizeZ          the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Point3DReadOnly position, Orientation3DReadOnly orientation, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(referenceFrame, position, orientation, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param position       the position of this ramp. Not modified.
    * @param orientation    the orientation of this ramp. Not modified.
    * @param size           the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Point3DReadOnly position, Orientation3DReadOnly orientation, Vector3DReadOnly size)
   {
      setIncludingFrame(referenceFrame, position, orientation, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param position    the position of this ramp. Not modified.
    * @param orientation the orientation of this ramp. Not modified.
    * @param sizeX       the size of this ramp along the x-axis.
    * @param sizeY       the size of this ramp along the y-axis.
    * @param sizeZ       the size of this ramp along the z-axis.
    * @throws IllegalArgumentException        if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ}
    *                                         is negative.
    * @throws ReferenceFrameMismatchException if any of the frame arguments are not expressed in the
    *                                         same reference frame.
    */
   public FrameSTPRamp3D(FramePoint3DReadOnly position, FrameOrientation3DReadOnly orientation, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(position, orientation, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param position    the position of this ramp. Not modified.
    * @param orientation the orientation of this ramp. Not modified.
    * @param size        the size of this ramp. Not modified.
    * @throws IllegalArgumentException        if any of the size components is negative.
    * @throws ReferenceFrameMismatchException if the arguments are not expressed in the same reference
    *                                         frame.
    */
   public FrameSTPRamp3D(FramePoint3DReadOnly position, FrameOrientation3DReadOnly orientation, Vector3DReadOnly size)
   {
      setIncludingFrame(position, orientation, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param position    the position of this ramp. Not modified.
    * @param orientation the orientation of this ramp. Not modified.
    * @param size        the size of this ramp. Not modified.
    * @throws IllegalArgumentException        if any of the size components is negative.
    * @throws ReferenceFrameMismatchException if the arguments are not expressed in the same reference
    *                                         frame.
    */
   public FrameSTPRamp3D(FramePoint3DReadOnly position, FrameOrientation3DReadOnly orientation, FrameVector3DReadOnly size)
   {
      setIncludingFrame(position, orientation, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param pose           the position and orientation for this ramp. Not modified.
    * @param sizeX          the size of this ramp along the x-axis.
    * @param sizeY          the size of this ramp along the y-axis.
    * @param sizeZ          the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Pose3DReadOnly pose, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(referenceFrame, pose, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param pose           the position and orientation for this ramp. Not modified.
    * @param size           the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Pose3DReadOnly pose, Vector3DReadOnly size)
   {
      setIncludingFrame(referenceFrame, pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose  the position and orientation for this ramp. Not modified.
    * @param sizeX the size of this ramp along the x-axis.
    * @param sizeY the size of this ramp along the y-axis.
    * @param sizeZ the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(FramePose3DReadOnly pose, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(pose, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose the position and orientation for this ramp. Not modified.
    * @param size the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(FramePose3DReadOnly pose, Vector3DReadOnly size)
   {
      setIncludingFrame(pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose the position and orientation for this ramp. Not modified.
    * @param size the size of this ramp. Not modified.
    * @throws IllegalArgumentException        if any of the size components is negative.
    * @throws ReferenceFrameMismatchException if the arguments are not expressed in the same reference
    *                                         frame.
    */
   public FrameSTPRamp3D(FramePose3DReadOnly pose, FrameVector3DReadOnly size)
   {
      setIncludingFrame(pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param pose           the position and orientation for this ramp. Not modified.
    * @param sizeX          the size of this ramp along the x-axis.
    * @param sizeY          the size of this ramp along the y-axis.
    * @param sizeZ          the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, RigidBodyTransformReadOnly pose, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(referenceFrame, pose, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param pose           the position and orientation for this ramp. Not modified.
    * @param size           the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, RigidBodyTransformReadOnly pose, Vector3DReadOnly size)
   {
      setIncludingFrame(referenceFrame, pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose  the position and orientation for this ramp. Not modified.
    * @param sizeX the size of this ramp along the x-axis.
    * @param sizeY the size of this ramp along the y-axis.
    * @param sizeZ the size of this ramp along the z-axis.
    * @throws IllegalArgumentException if any of {@code sizeX}, {@code sizeY}, or {@code sizeZ} is
    *                                  negative.
    */
   public FrameSTPRamp3D(FrameShape3DPoseReadOnly pose, double sizeX, double sizeY, double sizeZ)
   {
      setIncludingFrame(pose, sizeX, sizeY, sizeZ);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose the position and orientation for this ramp. Not modified.
    * @param size the size of this ramp. Not modified.
    * @throws IllegalArgumentException if any of the size components is negative.
    */
   public FrameSTPRamp3D(FrameShape3DPoseReadOnly pose, Vector3DReadOnly size)
   {
      setIncludingFrame(pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D and initializes its pose and size.
    *
    * @param pose the position and orientation for this ramp. Not modified.
    * @param size the size of this ramp. Not modified.
    * @throws IllegalArgumentException        if any of the size components is negative.
    * @throws ReferenceFrameMismatchException if the arguments are not expressed in the same reference
    *                                         frame.
    */
   public FrameSTPRamp3D(FrameShape3DPoseReadOnly pose, FrameVector3DReadOnly size)
   {
      setIncludingFrame(pose, size);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D identical to {@code other}.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param other          the other ramp to copy. Not modified.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, Ramp3DReadOnly other)
   {
      setIncludingFrame(referenceFrame, other);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D identical to {@code other}.
    *
    * @param referenceFrame this shape initial reference frame.
    * @param other          the other ramp to copy. Not modified.
    */
   public FrameSTPRamp3D(ReferenceFrame referenceFrame, STPRamp3DReadOnly other)
   {
      setIncludingFrame(referenceFrame, other);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D identical to {@code other}.
    *
    * @param other the other ramp to copy. Not modified.
    */
   public FrameSTPRamp3D(FrameRamp3DReadOnly other)
   {
      setIncludingFrame(other);
      setupListeners();
   }

   /**
    * Creates a new ramp 3D identical to {@code other}.
    *
    * @param other the other ramp to copy. Not modified.
    */
   public FrameSTPRamp3D(FrameSTPRamp3DReadOnly other)
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
      rawRamp3D.setReferenceFrame(referenceFrame);
   }

   /** {@inheritDoc} */
   @Override
   public ReferenceFrame getReferenceFrame()
   {
      return rawRamp3D.getReferenceFrame();
   }

   /** {@inheritDoc} */
   @Override
   public FixedFrameShape3DPoseBasics getPose()
   {
      return rawRamp3D.getPose();
   }

   /** {@inheritDoc} */
   @Override
   public FixedFrameVector3DBasics getSize()
   {
      return rawRamp3D.getSize();
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
         smallRadius = minimumMargin;
         largeRadius = STPShape3DTools.computeLargeRadiusFromMargins(minimumMargin,
                                                                     maximumMargin,
                                                                     STPShape3DTools.computeRamp3DMaximumEdgeLengthSquared(getSize()));
      }
   }

   /** {@inheritDoc} */
   @Override
   public boolean getSupportingVertex(Vector3DReadOnly supportDirection, Point3DBasics supportingVertexToPack)
   {
      return supportingVertexCalculator.getSupportingVertex(rawRamp3D, getSmallRadius(), getLargeRadius(), supportDirection, supportingVertexToPack);
   }

   /** {@inheritDoc} */
   @Override
   public FramePoint3DReadOnly getCentroid()
   {
      return rawRamp3D.getCentroid();
   }

   /** {@inheritDoc} */
   @Override
   public FrameVector3DReadOnly getRampSurfaceNormal()
   {
      return rawRamp3D.getRampSurfaceNormal();
   }

   /** {@inheritDoc} */
   @Override
   public void getRampSurfaceNormal(Vector3DBasics surfaceNormalToPack)
   {
      rawRamp3D.getRampSurfaceNormal(surfaceNormalToPack);
   }

   /** {@inheritDoc} */
   @Override
   public void getRampSurfaceNormal(FixedFrameVector3DBasics surfaceNormalToPack)
   {
      rawRamp3D.getRampSurfaceNormal(surfaceNormalToPack);
   }

   /** {@inheritDoc} */
   @Override
   public void getRampSurfaceNormal(FrameVector3DBasics surfaceNormalToPack)
   {
      rawRamp3D.getRampSurfaceNormal(surfaceNormalToPack);
   }

   /**
    * Notifies the internal listeners that this shape has changed.
    */
   public void notifyChangeListeners()
   {
      rawRamp3D.notifyChangeListeners();
   }

   /**
    * Registers a list of listeners to be notified when this shape changes.
    *
    * @param listeners the listeners to register.
    */
   public void addChangeListeners(List<? extends Shape3DChangeListener> listeners)
   {
      rawRamp3D.addChangeListeners(listeners);
   }

   /**
    * Registers a listener to be notified when this shape changes.
    *
    * @param listener the listener to register.
    */
   public void addChangeListener(Shape3DChangeListener listener)
   {
      rawRamp3D.addChangeListener(listener);
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
      return rawRamp3D.removeChangeListener(listener);
   }

   /** {@inheritDoc} */
   @Override
   public IntermediateVariableSupplier getIntermediateVariableSupplier()
   {
      return rawRamp3D.getIntermediateVariableSupplier();
   }

   /** {@inheritDoc} */
   @Override
   public void setIntermediateVariableSupplier(IntermediateVariableSupplier newSupplier)
   {
      rawRamp3D.setIntermediateVariableSupplier(newSupplier);
   }

   /**
    * Gets the length of this ramp's slope part.
    * <p>
    * Note that this is different than {@link #getSizeX()}. The returned value is equal to:
    * &radic;(this.length<sup>2</sup> + this.height<sup>2</sup>)
    * </p>
    *
    * @return the length of the slope.
    */
   @Override
   public double getRampLength()
   {
      return rawRamp3D.getRampLength();
   }

   /**
    * Gets the angle formed by the slope and the bottom face.
    * <p>
    * The angle is positive and in [0, <i>pi</i>].
    * </p>
    *
    * @return the slope angle.
    */
   @Override
   public double getRampIncline()
   {
      return rawRamp3D.getRampIncline();
   }

   @Override
   public FrameSTPRamp3D copy()
   {
      return new FrameSTPRamp3D(this);
   }

   @Override
   public FrameRampPolytope3DView asConvexPolytope()
   {
      return rawRamp3D.asConvexPolytope();
   }

   /**
    * Tests if the given {@code object}'s class is the same as this, in which case the method returns
    * {@link #equals(FrameSTPRamp3DReadOnly)}, it returns {@code false} otherwise.
    * <p>
    * If the two ramp have different frames, this method returns {@code false}.
    * </p>
    *
    * @param object the object to compare against this. Not modified.
    * @return {@code true} if {@code object} and this are exactly equal and are expressed in the same
    *         reference frame, {@code false} otherwise.
    */
   @Override
   public boolean equals(Object object)
   {
      if (object instanceof FrameSTPRamp3DReadOnly)
         return FrameSTPRamp3DBasics.super.equals((FrameSTPRamp3DReadOnly) object);
      else
         return false;
   }

   /**
    * Calculates and returns a hash code value from the value of each component of this ramp 3D.
    *
    * @return the hash code value for this ramp 3D.
    */
   @Override
   public int hashCode()
   {
      long hash = EuclidHashCodeTools.combineHashCode(rawRamp3D.hashCode(), EuclidHashCodeTools.toLongHashCode(minimumMargin, maximumMargin));
      return EuclidHashCodeTools.toIntHashCode(hash);
   }

   /**
    * Provides a {@code String} representation of this ramp 3D as follows:
    * 
    * <pre>
    * STP Ramp 3D: [position: ( 0.540, 0.110, 0.319 ), yaw-pitch-roll: (-2.061, -0.904, -1.136), size: (0.191, 0.719, 0.479 ), small radius: 0.001, large radius: 1.000]
    * </pre>
    *
    * @return the {@code String} representing this ramp 3D.
    */
   @Override
   public String toString()
   {
      String stpSuffix = String.format(", small radius: " + DEFAULT_FORMAT + ", large radius: " + DEFAULT_FORMAT + "]", getSmallRadius(), getLargeRadius());
      return "STP " + EuclidFrameShapeIOTools.getFrameRamp3DString(this).replace("]", stpSuffix);
   }
}
