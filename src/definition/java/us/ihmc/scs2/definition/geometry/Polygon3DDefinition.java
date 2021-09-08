package us.ihmc.scs2.definition.geometry;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple3D.Point3D;

/**
 * Definition for creating a 3D polygon.
 * <p>
 * It is assumed that the polygon is convex.
 * </p>
 */
public class Polygon3DDefinition extends GeometryDefinition
{
   private List<Point3D> polygonVertices;
   private boolean counterClockwiseOrdered;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public Polygon3DDefinition()
   {
      setName("polygon3D");
   }

   /**
    * Creates and initializes a definition for a 3D polygon.
    * 
    * @param polygonVertices         the 3D polygon's vertices.
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    */
   public Polygon3DDefinition(List<Point3D> polygonVertices, boolean counterClockwiseOrdered)
   {
      this();
      this.polygonVertices = polygonVertices;
      this.counterClockwiseOrdered = counterClockwiseOrdered;
   }

   public Polygon3DDefinition(Polygon3DDefinition other)
   {
      setName(other.getName());
      if (other.polygonVertices != null)
         polygonVertices = other.polygonVertices.stream().map(Point3D::new).collect(Collectors.toList());
      counterClockwiseOrdered = other.counterClockwiseOrdered;
   }

   /**
    * Sets the polygon's vertices.
    * 
    * @param polygonVertices the polygon's vertices.
    */
   @XmlElement
   public void setPolygonVertices(List<Point3D> polygonVertices)
   {
      this.polygonVertices = polygonVertices;
   }

   /**
    * Sets the flag for indicating the winding of the polygon.
    * 
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    */
   @XmlElement
   public void setCounterClockwiseOrdered(boolean counterClockwiseOrdered)
   {
      this.counterClockwiseOrdered = counterClockwiseOrdered;
   }

   /**
    * Returns the polygon's vertices.
    * 
    * @return the polygon's vertices.
    */
   public List<Point3D> getPolygonVertices()
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
   public Polygon3DDefinition copy()
   {
      return new Polygon3DDefinition(this);
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
      else if (object instanceof Polygon3DDefinition)
      {
         Polygon3DDefinition other = (Polygon3DDefinition) object;
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
      return "Polygon 3D: [name: " + getName() + ", " + ordering + ", vertices: "
            + EuclidCoreIOTools.getCollectionString("[", "]", ", ", polygonVertices, Object::toString);
   }
}
