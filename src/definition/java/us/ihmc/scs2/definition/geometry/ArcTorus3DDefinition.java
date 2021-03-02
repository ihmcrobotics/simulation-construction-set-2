package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;

/**
 * Definition for creating a partial 3D torus.
 * <p>
 * It is assumed that the torus' axis is aligned with the z-axis and its centroid at the origin.
 * </p>
 */
public class ArcTorus3DDefinition extends GeometryDefinition
{
   private double startAngle;
   private double endAngle;
   private double majorRadius;
   private double minorRadius;
   private int resolution = 64;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public ArcTorus3DDefinition()
   {
      setName("arcTorus");
   }

   /**
    * Creates and initializes a definition for a partial 3D torus.
    * 
    * @param startAngle  the angle at which the torus starts. The angle is in radians, it is expressed
    *                    with respect to the x-axis, and a positive angle corresponds to a
    *                    counter-clockwise rotation.
    * @param endAngle    the angle at which the torus ends. If {@code startAngle == endAngle} the torus
    *                    will be closed. The angle is in radians, it is expressed with respect to the
    *                    x-axis, and a positive angle corresponds to a counter-clockwise rotation.
    * @param majorRadius the radius from the torus centroid to the tube center.
    * @param minorRadius the radius of the tube.
    */
   public ArcTorus3DDefinition(double startAngle, double endAngle, double majorRadius, double minorRadius)
   {
      this();
      this.startAngle = startAngle;
      this.endAngle = endAngle;
      this.majorRadius = majorRadius;
      this.minorRadius = minorRadius;
   }

   /**
    * Creates and initializes a definition for a partial 3D torus.
    * 
    * @param startAngle  the angle at which the torus starts. The angle is in radians, it is expressed
    *                    with respect to the x-axis, and a positive angle corresponds to a
    *                    counter-clockwise rotation.
    * @param endAngle    the angle at which the torus ends. If {@code startAngle == endAngle} the torus
    *                    will be closed. The angle is in radians, it is expressed with respect to the
    *                    x-axis, and a positive angle corresponds to a counter-clockwise rotation.
    * @param majorRadius the radius from the torus centroid to the tube center.
    * @param minorRadius the radius of the tube.
    * @param resolution  used for discretizing the geometry.
    */
   public ArcTorus3DDefinition(double startAngle, double endAngle, double majorRadius, double minorRadius, int resolution)
   {
      this();
      this.startAngle = startAngle;
      this.endAngle = endAngle;
      this.majorRadius = majorRadius;
      this.minorRadius = minorRadius;
      this.resolution = resolution;
   }

   public ArcTorus3DDefinition(ArcTorus3DDefinition other)
   {
      this();
      setName(other.getName());
      startAngle = other.startAngle;
      endAngle = other.endAngle;
      majorRadius = other.majorRadius;
      minorRadius = other.minorRadius;
      resolution = other.resolution;
   }

   /**
    * Sets the angle at which the torus starts.
    * <p>
    * The angle is in radians, it is expressed with respect to the x-axis, and a positive angle
    * corresponds to a counter-clockwise rotation.
    * </p>
    * 
    * @param startAngle the angle at which the torus starts.
    */
   public void setStartAngle(double startAngle)
   {
      this.startAngle = startAngle;
   }

   /**
    * Sets the angle at which the torus ends.
    * <p>
    * If {@code startAngle == endAngle} the torus will be closed. The angle is in radians, it is
    * expressed with respect to the x-axis, and a positive angle corresponds to a counter-clockwise
    * rotation.
    * </p>
    * 
    * @param endAngle the angle at which the torus ends.
    */
   public void setEndAngle(double endAngle)
   {
      this.endAngle = endAngle;
   }

   /**
    * Sets the torus' major radius.
    * 
    * @param majorRadius the radius from the torus centroid to the tube center.
    */
   public void setMajorRadius(double majorRadius)
   {
      this.majorRadius = majorRadius;
   }

   /**
    * Sets the torus' minor radius.
    * 
    * @param minorRadius the radius of the tube.
    */
   public void setMinorRadius(double minorRadius)
   {
      this.minorRadius = minorRadius;
   }

   /**
    * Sets the torus' resolution used when discretizing it.
    * 
    * @param resolution the torus' resolution.
    */
   public void setResolution(int resolution)
   {
      this.resolution = resolution;
   }

   /**
    * Returns the angle at which the torus starts.
    * <p>
    * The angle is in radians, it is expressed with respect to the x-axis, and a positive angle
    * corresponds to a counter-clockwise rotation.
    * </p>
    * 
    * @return the start angle.
    */
   public double getStartAngle()
   {
      return startAngle;
   }

   /**
    * Returns the angle at which the torus ends.
    * <p>
    * If {@code startAngle == endAngle} the torus will be closed. The angle is in radians, it is
    * expressed with respect to the x-axis, and a positive angle corresponds to a counter-clockwise
    * rotation.
    * </p>
    * 
    * @return the end angle.
    */
   public double getEndAngle()
   {
      return endAngle;
   }

   /**
    * Returns the torus' major radius.
    * 
    * @return the radius from the torus centroid to the tube center.
    */
   public double getMajorRadius()
   {
      return majorRadius;
   }

   /**
    * Returns the torus' minor radius.
    * 
    * @return the radius of the tube.
    */
   public double getMinorRadius()
   {
      return minorRadius;
   }

   /**
    * Returns the torus' resolution which can be used for discretizing it.
    * 
    * @return the torus' resolution.
    */
   public int getResolution()
   {
      return resolution;
   }

   @Override
   public ArcTorus3DDefinition copy()
   {
      return new ArcTorus3DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, startAngle);
      bits = EuclidHashCodeTools.addToHashCode(bits, endAngle);
      bits = EuclidHashCodeTools.addToHashCode(bits, majorRadius);
      bits = EuclidHashCodeTools.addToHashCode(bits, minorRadius);
      bits = EuclidHashCodeTools.addToHashCode(bits, resolution);
      bits = EuclidHashCodeTools.addToHashCode(bits, getName());
      return EuclidHashCodeTools.toIntHashCode(bits);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof ArcTorus3DDefinition)
      {
         ArcTorus3DDefinition other = (ArcTorus3DDefinition) object;
         if (startAngle != other.startAngle)
            return false;
         if (endAngle != other.endAngle)
            return false;
         if (majorRadius != other.majorRadius)
            return false;
         if (minorRadius != other.minorRadius)
            return false;
         if (resolution != other.resolution)
            return false;
         return super.equals(object);
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "Arc-Torus: [name: " + getName() + EuclidCoreIOTools.getStringOf(", radii: (", ")", ", ", majorRadius, minorRadius) + ", start angle: "
            + String.format(EuclidCoreIOTools.DEFAULT_FORMAT, startAngle) + ", end angle: " + String.format(EuclidCoreIOTools.DEFAULT_FORMAT, endAngle)
            + ", resolution: " + resolution + "]";
   }
}
