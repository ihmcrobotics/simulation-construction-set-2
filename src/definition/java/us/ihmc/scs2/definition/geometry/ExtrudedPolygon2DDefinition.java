package us.ihmc.scs2.definition.geometry;

import java.util.List;
import java.util.stream.Collectors;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.euclid.tuple2D.Point2D;

/**
 * Definition for creating an extruded 2D polygon.
 * <p>
 * It is assumed that the polygon is convex and is described in the xy-plane and extruded along the
 * z-axis.
 * </p>
 */
public class ExtrudedPolygon2DDefinition extends GeometryDefinition
{
   private List<Point2D> polygonVertices;
   private boolean counterClockwiseOrdered;
   private double topZ, bottomZ;

   /**
    * Creates an empty definition. The parameters have to be all set before this definition can be
    * used.
    */
   public ExtrudedPolygon2DDefinition()
   {
      setName("extrudedPolygon2D");
   }

   /**
    * Creates and initializes a definition for an extruded 2D polygon.
    * 
    * @param polygonVertices         the 2D polygon's vertices.
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    * @param extrusionHeight         the thickness of the extrusion along the z-axis.
    */
   public ExtrudedPolygon2DDefinition(List<Point2D> polygonVertices, boolean counterClockwiseOrdered, double extrusionHeight)
   {
      this();
      this.polygonVertices = polygonVertices;
      this.counterClockwiseOrdered = counterClockwiseOrdered;
      this.topZ = extrusionHeight;
      this.bottomZ = 0.0;
   }

   /**
    * Creates and initializes a definition for an extruded 2D polygon.
    * 
    * @param polygonVertices         the 2D polygon's vertices.
    * @param counterClockwiseOrdered indicate the winding of the polygon: {@code true} if the polygon
    *                                is counter clockwise, {@code false} if clockwise.
    * @param topZ                    the z-coordinate of the top face of the extrusion.
    * @param bottomZ                 the z-coordinate of the bottom face of the extrusion.
    */
   public ExtrudedPolygon2DDefinition(List<Point2D> polygonVertices, boolean counterClockwiseOrdered, double topZ, double bottomZ)
   {
      this();
      this.polygonVertices = polygonVertices;
      this.counterClockwiseOrdered = counterClockwiseOrdered;
      this.topZ = topZ;
      this.bottomZ = bottomZ;
   }

   public ExtrudedPolygon2DDefinition(ExtrudedPolygon2DDefinition other)
   {
      setName(other.getName());
      if (other.polygonVertices != null)
         polygonVertices = other.polygonVertices.stream().map(Point2D::new).collect(Collectors.toList());
      counterClockwiseOrdered = other.counterClockwiseOrdered;
      topZ = other.topZ;
      bottomZ = other.bottomZ;
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
    * Sets the z-coordinate for the top face of the extrusion.
    * 
    * @param topZ the z-coordinate of the top face of the extrusion.
    */
   public void setTopZ(double topZ)
   {
      this.topZ = topZ;
   }

   /**
    * Sets the z-coordinate for the bottom face of the extrusion.
    * 
    * @param bottomZ the z-coordinate of the bottom face of the extrusion.
    */
   public void setBottomZ(double bottomZ)
   {
      this.bottomZ = bottomZ;
   }

   /**
    * Sets the thickness of the extrusion and sets the z-coordinate of the bottom face of the extrusion
    * to zero.
    * 
    * @param extrusionHeight the thickness of the extrusion along the z-axis.
    */
   public void setExtrusionHeight(double extrusionHeight)
   {
      topZ = extrusionHeight;
      bottomZ = 0.0;
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

   /**
    * Returns the z-coordinate of the top face of the extrusion.
    * 
    * @return the z-coordinate of the top face of the extrusion.
    */
   public double getTopZ()
   {
      return topZ;
   }

   /**
    * Returns the z-coordinate of the bottom face of the extrusion.
    * 
    * @return the z-coordinate of the bottom face of the extrusion.
    */
   public double getBottomZ()
   {
      return bottomZ;
   }

   @Override
   public ExtrudedPolygon2DDefinition copy()
   {
      return new ExtrudedPolygon2DDefinition(this);
   }

   @Override
   public int hashCode()
   {
      long bits = 1L;
      bits = EuclidHashCodeTools.addToHashCode(bits, polygonVertices);
      bits = EuclidHashCodeTools.addToHashCode(bits, counterClockwiseOrdered);
      bits = EuclidHashCodeTools.addToHashCode(bits, topZ);
      bits = EuclidHashCodeTools.addToHashCode(bits, bottomZ);
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
      else if (object instanceof ExtrudedPolygon2DDefinition)
      {
         ExtrudedPolygon2DDefinition other = (ExtrudedPolygon2DDefinition) object;
         if (polygonVertices == null ? other.polygonVertices != null : !polygonVertices.equals(other.polygonVertices))
            return false;
         if (counterClockwiseOrdered != other.counterClockwiseOrdered)
            return false;
         if (topZ != other.topZ)
            return false;
         if (bottomZ != other.bottomZ)
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
      return "Extruded Polygon 2D: [name: " + getName() + ", " + ordering + ", vertices: "
            + EuclidCoreIOTools.getCollectionString("[", "]", ", ", polygonVertices, Object::toString);
   }
}
