package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.List;

import us.ihmc.euclid.tuple2D.Point2D;

public class DataEntry
{
   private final ChartIntegerBounds xBounds;
   private final ChartDoubleBounds yBounds;
   private final List<Point2D> data;
   private final int bufferCurrentIndex;

   public DataEntry(ChartIntegerBounds xBounds, ChartDoubleBounds yBounds, List<Point2D> data, int bufferCurrentIndex)
   {
      this.xBounds = xBounds;
      this.yBounds = yBounds;
      this.data = data;
      this.bufferCurrentIndex = bufferCurrentIndex;
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

   public int getBufferCurrentIndex()
   {
      return bufferCurrentIndex;
   }
}
