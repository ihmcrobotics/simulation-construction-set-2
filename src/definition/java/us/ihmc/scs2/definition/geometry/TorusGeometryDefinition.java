package us.ihmc.scs2.definition.geometry;

public class TorusGeometryDefinition implements GeometryDefinition
{
   private double majorRadius;
   private double minorRadius;

   public TorusGeometryDefinition()
   {
   }

   public TorusGeometryDefinition(double majorRadius, double minorRadius)
   {
      setMajorRadius(majorRadius);
      setMinorRadius(minorRadius);
   }

   public void setMajorRadius(double majorRadius)
   {
      this.majorRadius = majorRadius;
   }

   public void setMinorRadius(double minorRadius)
   {
      this.minorRadius = minorRadius;
   }

   public double getMajorRadius()
   {
      return majorRadius;
   }

   public double getMinorRadius()
   {
      return minorRadius;
   }
}
