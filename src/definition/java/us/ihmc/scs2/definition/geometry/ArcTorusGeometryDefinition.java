package us.ihmc.scs2.definition.geometry;

public class ArcTorusGeometryDefinition implements GeometryDefinition
{
   private double majorRadius;
   private double minorRadius;
   private double startAngle;
   private double endAngle;

   public ArcTorusGeometryDefinition()
   {
   }

   public ArcTorusGeometryDefinition(double majorRadius, double minorRadius, double startAngle, double endAngle)
   {
      setMajorRadius(majorRadius);
      setMinorRadius(minorRadius);
      setStartAngle(startAngle);
      setEndAngle(endAngle);
   }

   public void setMajorRadius(double majorRadius)
   {
      this.majorRadius = majorRadius;
   }

   public void setMinorRadius(double minorRadius)
   {
      this.minorRadius = minorRadius;
   }

   public void setStartAngle(double startAngle)
   {
      this.startAngle = startAngle;
   }

   public void setEndAngle(double endAngle)
   {
      this.endAngle = endAngle;
   }

   public double getMajorRadius()
   {
      return majorRadius;
   }

   public double getMinorRadius()
   {
      return minorRadius;
   }

   public double getStartAngle()
   {
      return startAngle;
   }

   public double getEndAngle()
   {
      return endAngle;
   }
}
