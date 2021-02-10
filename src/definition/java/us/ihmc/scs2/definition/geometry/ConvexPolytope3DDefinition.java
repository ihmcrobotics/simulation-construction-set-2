package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.shape.convexPolytope.ConvexPolytope3D;
import us.ihmc.euclid.shape.convexPolytope.interfaces.ConvexPolytope3DReadOnly;
import us.ihmc.euclid.tools.EuclidHashCodeTools;

/**
 * Definition for creating a 3D convex polytope.
 */
public class ConvexPolytope3DDefinition extends GeometryDefinition
{
   private ConvexPolytope3DReadOnly convexPolytope;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public ConvexPolytope3DDefinition()
   {
      setName("convexPolytope3D");
   }

   /**
    * Creates and initializes a definition for a 3D convex polytope.
    * 
    * @param convexPolytope the convex polytope for this definition.
    */
   public ConvexPolytope3DDefinition(ConvexPolytope3DReadOnly convexPolytope)
   {
      this();
      this.convexPolytope = convexPolytope;
   }

   public ConvexPolytope3DDefinition(ConvexPolytope3DDefinition other)
   {
      setName(other.getName());
      if (other.convexPolytope != null)
         convexPolytope = new ConvexPolytope3D(other.convexPolytope);
   }

   /**
    * Sets the convex polytope to associate with this definition.
    * 
    * @param convexPolytope the convex polytope for this definition.
    */
   public void setConvexPolytope(ConvexPolytope3DReadOnly convexPolytope)
   {
      this.convexPolytope = convexPolytope;
   }

   /**
    * Returns the convex polytope associated with this definition.
    * 
    * @return the convex polytope associated with this definition.
    */
   public ConvexPolytope3DReadOnly getConvexPolytope()
   {
      return convexPolytope;
   }

   @Override
   public ConvexPolytope3DDefinition copy()
   {
      return new ConvexPolytope3DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, convexPolytope);
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
      else if (object instanceof ConvexPolytope3DDefinition)
      {
         ConvexPolytope3DDefinition other = (ConvexPolytope3DDefinition) object;
         if (convexPolytope == null ? other.convexPolytope != null : !convexPolytope.equals(other.convexPolytope))
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
      return "Convex Polytope: [name: " + getName() + ", " + convexPolytope + "]";
   }
}
