package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.List;

import us.ihmc.euclid.tuple2D.Point2D;

public class DataEntry
{
   private final ChartIntegerBounds xBounds;
   private final ChartDoubleBounds yBounds;
   private final List<Point2D> data;

   public DataEntry(ChartIntegerBounds xBounds, ChartDoubleBounds yBounds, List<Point2D> data)
   {
      this.xBounds = xBounds;
      this.yBounds = yBounds;
      this.data = data;
   }

   public ChartIntegerBounds getXBounds()
   {
      return xBounds;
   }

   public ChartDoubleBounds getYBounds()
   {
      return yBounds;
   }

   public List<Point2D> getData()
   {
      return data;
   }
}
