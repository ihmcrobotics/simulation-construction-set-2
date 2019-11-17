package us.ihmc.scs2.definition.geometry;

public class CylinderGeometryDefinition implements GeometryDefinition
{
   private double length;
   private double radius;

   public CylinderGeometryDefinition()
   {
   }

   public CylinderGeometryDefinition(double length, double radius)
   {
      setLength(length);
      setRadius(radius);
   }

   public void setLength(double length)
   {
      this.length = length;
   }

   public void setRadius(double radius)
   {
      this.radius = radius;
   }

   public double getLength()
   {
      return length;
   }

   public double getRadius()
   {
      return radius;
   }
}
