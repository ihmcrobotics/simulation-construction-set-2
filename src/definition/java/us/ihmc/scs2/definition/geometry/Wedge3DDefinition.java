package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

/**
 * Definition for creating a 3D wedge.
 * <p>
 * It is assumed that the wedge's bottom face is centered at the origin.
 * </p>
 */
public class Wedge3DDefinition extends GeometryDefinition
{
   private double sizeX;
   private double sizeY;
   private double sizeZ;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public Wedge3DDefinition()
   {
      setName("wedge");
   }

   /**
    * Creates and initializes a definition for a 3D wedge.
    * 
    * @param sizeX the length of the wedge along the x-axis.
    * @param sizeY the width of the wedge along the y-axis.
    * @param sizeZ the height of the wedge along the z-axis.
    */
   public Wedge3DDefinition(double sizeX, double sizeY, double sizeZ)
   {
      this();
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
   }

   /**
    * Creates and initializes a definition for a 3D wedge.
    * 
    * @param size the size of the wedge along the 3 axes.
    */
   public Wedge3DDefinition(Tuple3DReadOnly size)
   {
      this(size.getX(), size.getY(), size.getZ());
   }

   public Wedge3DDefinition(Wedge3DDefinition other)
   {
      setName(other.getName());
      sizeX = other.sizeX;
      sizeY = other.sizeY;
      sizeZ = other.sizeZ;
   }

   /**
    * Sets the length of the wedge along the x-axis.
    * 
    * @param sizeX the length of the wedge along the x-axis.
    */
   public void setSizeX(double sizeX)
   {
      this.sizeX = sizeX;
   }

   /**
    * Sets the width of the wedge along the y-axis.
    * 
    * @param sizeY the width of the wedge along the y-axis.
    */
   public void setSizeY(double sizeY)
   {
      this.sizeY = sizeY;
   }

   /**
    * Sets the height of the wedge along the z-axis.
    * 
    * @param sizeZ the height of the wedge along the z-axis.
    */
   public void setSizeZ(double sizeZ)
   {
      this.sizeZ = sizeZ;
   }

   /**
    * Sets the size of the wedge.
    * 
    * @param sizeX the length of the wedge along the x-axis.
    * @param sizeY the width of the wedge along the y-axis.
    * @param sizeZ the height of the wedge along the z-axis.
    */
   public void setSize(double sizeX, double sizeY, double sizeZ)
   {
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
   }

   /**
    * Returns the length of the wedge along the x-axis.
    * 
    * @return the length of the wedge along the x-axis.
    */
   public double getSizeX()
   {
      return sizeX;
   }

   /**
    * Returns the width of the wedge along the y-axis.
    * 
    * @return the width of the wedge along the y-axis.
    */
   public double getSizeY()
   {
      return sizeY;
   }

   /**
    * Returns the height of the wedge along the z-axis.
    * 
    * @return the height of the wedge along the z-axis.
    */
   public double getSizeZ()
   {
      return sizeZ;
   }

   @Override
   public Wedge3DDefinition copy()
   {
      return new Wedge3DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, sizeX);
      bits = EuclidHashCodeTools.addToHashCode(bits, sizeY);
      bits = EuclidHashCodeTools.addToHashCode(bits, sizeZ);
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
      else if (object instanceof Wedge3DDefinition)
      {
         Wedge3DDefinition other = (Wedge3DDefinition) object;
         if (sizeX != other.sizeX)
            return false;
         if (sizeY != other.sizeY)
            return false;
         if (sizeZ != other.sizeZ)
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
      return EuclidCoreIOTools.getStringOf("Wedge: [name: " + getName() + ", size: (", ")]", ", ", sizeX, sizeY, sizeZ);
   }
}
