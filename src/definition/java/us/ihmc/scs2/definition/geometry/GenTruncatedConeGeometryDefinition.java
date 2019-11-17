package us.ihmc.scs2.definition.geometry;

public class GenTruncatedConeGeometryDefinition implements GeometryDefinition
{
   private double height;
   private double topRadiusX;
   private double topRadiusY;
   private double baseRadiusX;
   private double baseRadiusY;

   public GenTruncatedConeGeometryDefinition()
   {
   }

   public GenTruncatedConeGeometryDefinition(double height, double topRadiusX, double topRadiusY, double baseRadiusX, double baseRadiusY)
   {
      setHeight(height);
      setTopRadiusX(topRadiusX);
      setTopRadiusY(topRadiusY);
      setBaseRadiusX(baseRadiusX);
      setBaseRadiusY(baseRadiusY);
   }

   public void setHeight(double height)
   {
      this.height = height;
   }

   public void setTopRadiusX(double topRadiusX)
   {
      this.topRadiusX = topRadiusX;
   }

   public void setTopRadiusY(double topRadiusY)
   {
      this.topRadiusY = topRadiusY;
   }

   public void setBaseRadiusX(double baseRadiusX)
   {
      this.baseRadiusX = baseRadiusX;
   }

   public void setBaseRadiusY(double baseRadiusY)
   {
      this.baseRadiusY = baseRadiusY;
   }

   public double getHeight()
   {
      return height;
   }

   public double getTopRadiusX()
   {
      return topRadiusX;
   }

   public double getTopRadiusY()
   {
      return topRadiusY;
   }

   public double getBaseRadiusX()
   {
      return baseRadiusX;
   }

   public double getBaseRadiusY()
   {
      return baseRadiusY;
   }
}
