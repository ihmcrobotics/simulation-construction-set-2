package us.ihmc.scs2.definition.geometry;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple2D.Point2D;

/**
 * Definition for creating a 2D polygon.
 * <p>
 * It is assumed that the polygon is convex.
 * </p>
 */
public class Polygon2DDefinition extends GeometryDefinition
{
   private List<Point2D> polygonVertices;
   private boolean counterClockwiseOrdered;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public Polygon2DDefinition()
   {
      setName("polygon2D");
   }

   /**
    * Creates and initializes a definition for a 2D polygon.
    * 
    * @param polygonVertices         the 2D polygon's vertices.
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    */
   public Polygon2DDefinition(List<Point2D> polygonVertices, boolean counterClockwiseOrdered)
   {
      this();
      this.polygonVertices = polygonVertices;
      this.counterClockwiseOrdered = counterClockwiseOrdered;
   }

   public Polygon2DDefinition(Polygon2DDefinition other)
   {
      setName(other.getName());
      if (other.polygonVertices != null)
         polygonVertices = other.polygonVertices.stream().map(Point2D::new).collect(Collectors.toList());
      counterClockwiseOrdered = other.counterClockwiseOrdered;
   }

   /**
    * Sets the polygon's vertices.
    * 
    * @param polygonVertices the polygon's vertices.
    */
   public void setPolygonVertices(List<Point2D> polygonVertices)
   {
      this.polygonVertices = polygonVertices;
   }

   /**
    * Sets the flag for indicating the winding of the polygon.
    * 
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    */
   public void setCounterClockwiseOrdered(boolean counterClockwiseOrdered)
   {
      this.counterClockwiseOrdered = counterClockwiseOrdered;
   }

   /**
    * Returns the polygon's vertices.
    * 
    * @return the polygon's vertices.
    */
   public List<Point2D> getPolygonVertices()
   {
      return polygonVertices;
   }

   /**
    * Returns the winding of the polygon.
    * 
    * @return {@code true} if the polygon is counter clockwise, {@code false} if clockwise.
    */
   public boolean isCounterClockwiseOrdered()
   {
      return counterClockwiseOrdered;
   }

   @Override
   public Polygon2DDefinition copy()
   {
      return new Polygon2DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, polygonVertices);
      bits = EuclidHashCodeTools.addToHashCode(bits, counterClockwiseOrdered);
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
      else if (object instanceof Polygon2DDefinition)
      {
         Polygon2DDefinition other = (Polygon2DDefinition) object;
         if (polygonVertices == null ? other.polygonVertices != null : !polygonVertices.equals(other.polygonVertices))
            return false;
         if (counterClockwiseOrdered != other.counterClockwiseOrdered)
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
      String ordering = counterClockwiseOrdered ? "counter-clockwise" : "clockwise";
      return "Polygon 2D: [name: " + getName() + ", " + ordering + ", vertices: "
            + EuclidCoreIOTools.getCollectionString("[", "]", ", ", polygonVertices, Object::toString);
   }
}
