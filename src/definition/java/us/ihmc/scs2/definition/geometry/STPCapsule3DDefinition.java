package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;

public class STPCapsule3DDefinition extends Capsule3DDefinition
{
   private double minimumMargin, maximumMargin;

   public STPCapsule3DDefinition()
   {
      super();
   }

   public STPCapsule3DDefinition(Capsule3DDefinition other)
   {
      super(other);
   }

   public STPCapsule3DDefinition(STPCapsule3DDefinition other)
   {
      super(other);
      setMargins(other.minimumMargin, other.maximumMargin);
   }

   public STPCapsule3DDefinition(double length, double radiusX, double radiusY, double radiusZ, int resolution)
   {
      super(length, radiusX, radiusY, radiusZ, resolution);
   }

   public STPCapsule3DDefinition(double length, double radiusX, double radiusY, double radiusZ)
   {
      super(length, radiusX, radiusY, radiusZ);
   }

   public STPCapsule3DDefinition(double length, double radius, int resolution)
   {
      super(length, radius, resolution);
   }

   public STPCapsule3DDefinition(double length, double radius)
   {
      super(length, radius);
   }

   public void setMargins(double minimumMargin, double maximumMargin)
   {
      setMinimumMargin(minimumMargin);
      setMaximumMargin(maximumMargin);
   }

   public void setMinimumMargin(double minimumMargin)
   {
      this.minimumMargin = minimumMargin;
   }

   public void setMaximumMargin(double maximumMargin)
   {
      this.maximumMargin = maximumMargin;
   }

   public double getMinimumMargin()
   {
      return minimumMargin;
   }

   public double getMaximumMargin()
   {
      return maximumMargin;
   }

   @Override
   public STPCapsule3DDefinition copy()
   {
      return new STPCapsule3DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, getLength());
      bits = EuclidHashCodeTools.addToHashCode(bits, getRadiusX());
      bits = EuclidHashCodeTools.addToHashCode(bits, getRadiusY());
      bits = EuclidHashCodeTools.addToHashCode(bits, getRadiusZ());
      bits = EuclidHashCodeTools.addToHashCode(bits, getResolution());
      bits = EuclidHashCodeTools.addToHashCode(bits, minimumMargin);
      bits = EuclidHashCodeTools.addToHashCode(bits, maximumMargin);
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
      else if (object instanceof STPCapsule3DDefinition)
      {
         STPCapsule3DDefinition other = (STPCapsule3DDefinition) object;
         if (minimumMargin != other.minimumMargin)
            return false;
         if (maximumMargin != other.maximumMargin)
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
      return "STP" + super.toString().replace("]", "") + EuclidCoreIOTools.getStringOf(", margins: (", ")]", ", ", minimumMargin, maximumMargin);
   }
}
