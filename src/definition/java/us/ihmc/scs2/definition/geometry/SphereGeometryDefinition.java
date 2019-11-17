package us.ihmc.scs2.definition.geometry;

public class SphereGeometryDefinition implements GeometryDefinition
{
   private double radius;

   public SphereGeometryDefinition()
   {
   }

   public SphereGeometryDefinition(double radius)
   {
      setRadius(radius);
   }

   public void setRadius(double radius)
   {
      this.radius = radius;
   }

   public double getRadius()
   {
      return radius;
   }
}
