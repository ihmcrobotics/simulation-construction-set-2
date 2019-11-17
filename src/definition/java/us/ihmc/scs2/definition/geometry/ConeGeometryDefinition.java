package us.ihmc.scs2.definition.geometry;

public class ConeGeometryDefinition implements GeometryDefinition
{
   private double height;
   private double radius;
   
   public ConeGeometryDefinition()
   {
   }

   public ConeGeometryDefinition(double height, double radius)
   {
      setHeight(height);
      setRadius(radius);
   }
   
   public void setHeight(double height)
   {
      this.height = height;
   }
   
   public void setRadius(double radius)
   {
      this.radius = radius;
   }

   public double getHeight()
   {
      return height;
   }

   public double getRadius()
   {
      return radius;
   }
}
