package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tools.EuclidHashCodeTools;

/**
 * Definition for creating a regular 3D tetrahedron.
 * <p>
 * It is assumed that the tetrahedron is centered at the origin.
 * </p>
 */
public class Tetrahedron3DDefinition extends GeometryDefinition
{
   private double edgeLength;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public Tetrahedron3DDefinition()
   {
      setName("tetrahedron");
   }

   /**
    * Creates and initializes a definition for a 3D tetrahedron.
    *
    * @param edgeLength the length for all edges.
    */
   public Tetrahedron3DDefinition(double edgeLength)
   {
      this();
      this.edgeLength = edgeLength;
   }

   public Tetrahedron3DDefinition(Tetrahedron3DDefinition other)
   {
      setName(other.getName());
      edgeLength = other.edgeLength;
   }

   /**
    * Sets the length for all edges.
    *
    * @param edgeLength the length for all edges.
    */
   public void setEdgeLength(double edgeLength)
   {
      this.edgeLength = edgeLength;
   }

   /**
    * Returns the length for all edges.
    * 
    * @return the length for all edges.
    */
   public double getEdgeLength()
   {
      return edgeLength;
   }

   @Override
   public Tetrahedron3DDefinition copy()
   {
      return new Tetrahedron3DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, edgeLength);
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
      else if (object instanceof Tetrahedron3DDefinition)
      {
         Tetrahedron3DDefinition other = (Tetrahedron3DDefinition) object;
         if (edgeLength != other.edgeLength)
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
      return "Tetrahedron: [name: " + getName() + ", edge length: " + edgeLength + "]";
   }
}
