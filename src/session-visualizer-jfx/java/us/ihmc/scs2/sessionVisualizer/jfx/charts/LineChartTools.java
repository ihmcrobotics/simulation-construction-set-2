package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DouglasPeuckerReduction;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.NumberFormatTools;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class LineChartTools
{
   public static String defaultYoVariableValueFormatter(YoVariable yoVariable)
   {
      if (yoVariable instanceof YoDouble)
         return NumberFormatTools.doubleToString(yoVariable.getValueAsDouble(), 3);
      else
         return LineChartTools.getYoVariableValueAsString(yoVariable, EuclidCoreIOTools.getStringFormat(11, 8));
   }

   public static String getYoVariableValueAsString(YoVariable yoVariable, String format)
   {
      if (yoVariable instanceof YoDouble)
         return String.format(format, ((YoDouble) yoVariable).getValue());
      if (yoVariable instanceof YoBoolean)
         return Boolean.toString(((YoBoolean) yoVariable).getValue());
      if (yoVariable instanceof YoInteger)
         return Integer.toString(((YoInteger) yoVariable).getValue());
      if (yoVariable instanceof YoLong)
         return Long.toString(((YoLong) yoVariable).getValue());
      if (yoVariable instanceof YoEnum<?>)
         return ((YoEnum<?>) yoVariable).getStringValue();

      throw new UnsupportedOperationException("Unsupported YoVariable type: " + yoVariable.getClass().getSimpleName());
   }

   public static DataEntry fromBooleanBufferSampleToLineChartData(BufferSample<boolean[]> bufferSample, int startIndex, int endIndex)
   {
      if (bufferSample == null || bufferSample.getSample().length == 0 || endIndex >= bufferSample.getSample().length)
         return null;

      int sampleLength = endIndex - startIndex;

      ChartIntegerBounds xBounds = new ChartIntegerBounds(startIndex, endIndex);
      ChartDoubleBounds yBounds;
      List<Point2D> newData = new ArrayList<>();

      boolean lastValue = bufferSample.getSample()[startIndex];
      Point2D dataPoint = new Point2D(startIndex, toDouble(lastValue));
      newData.add(dataPoint);
      yBounds = new ChartDoubleBounds(dataPoint.getY(), dataPoint.getY());

      if (sampleLength == 1)
         return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());

      boolean nextValue = bufferSample.getSample()[startIndex + 1];

      for (int i = startIndex + 1; i < endIndex; i++)
      {
         boolean value = nextValue;
         nextValue = bufferSample.getSample()[i + 1];

         if (value == lastValue && value == nextValue)
            continue;

         lastValue = value;
         dataPoint = new Point2D(i, toDouble(lastValue));
         newData.add(dataPoint);
         yBounds = yBounds.include(dataPoint.getY());
      }

      dataPoint = new Point2D(endIndex, toDouble(bufferSample.getSample()[endIndex]));
      newData.add(dataPoint);
      yBounds = yBounds.include(dataPoint.getY());

      return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());
   }

   public static double toDouble(boolean value)
   {
      return value ? 1.0 : 0.0;
   }

   public static DataEntry fromDoubleBufferSampleToLineChartData(BufferSample<double[]> bufferSample, int startIndex, int endIndex, double epsilon)
   {
      if (bufferSample == null || bufferSample.getSample().length == 0 || endIndex >= bufferSample.getSample().length)
         return null;

      int sampleLength = endIndex - startIndex;

      ChartIntegerBounds xBounds = new ChartIntegerBounds(startIndex, endIndex);
      ChartDoubleBounds yBounds;
      List<Point2D> newData = new ArrayList<>(sampleLength);

      double lastValue = bufferSample.getSample()[startIndex];
      if (!Double.isFinite(lastValue)) // TODO Kinda hackish but it appears that JavaFX chart doesn't handle them properly.
         lastValue = 0.0;
      Point2D dataPoint = new Point2D(startIndex, lastValue);
      newData.add(dataPoint);
      yBounds = new ChartDoubleBounds(dataPoint.getY(), dataPoint.getY());

      if (sampleLength > 1)
      {
         double nextValue = bufferSample.getSample()[startIndex + 1];
         nextValue = bufferSample.getSample()[startIndex + 1];
         if (!Double.isFinite(nextValue)) // TODO Kinda hackish but it appears that JavaFX chart doesn't handle them properly.
            nextValue = 0.0;

         for (int i = startIndex + 1; i < endIndex; i++)
         {
            double value = nextValue;
            nextValue = bufferSample.getSample()[i + 1];
            if (!Double.isFinite(nextValue)) // TODO Kinda hackish but it appears that JavaFX chart doesn't handle them properly.
               nextValue = 0.0;
            if (value == lastValue && value == nextValue)
               continue;
            lastValue = value;
            dataPoint = new Point2D(i, value);
            newData.add(dataPoint);
            yBounds = yBounds.include(value);
         }
         double value = bufferSample.getSample()[endIndex];
         if (!Double.isFinite(value)) // TODO Kinda hackish but it appears that JavaFX chart doesn't handle them properly.
            value = 0.0;
         dataPoint = new Point2D(endIndex, value);
         newData.add(dataPoint);
         yBounds = yBounds.include(value);

//         newData = DouglasPeuckerReduction.reduce(newData, epsilon * (yBounds.getUpper() - yBounds.getLower()));
      }

      return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());
   }

   public static DataEntry fromIntegerBufferSampleToLineChartData(BufferSample<int[]> bufferSample, int startIndex, int endIndex, double epsilon)
   {
      if (bufferSample == null || bufferSample.getSample().length == 0 || endIndex >= bufferSample.getSample().length)
         return null;

      int sampleLength = endIndex - startIndex;

      ChartIntegerBounds xBounds = new ChartIntegerBounds(startIndex, endIndex);
      ChartDoubleBounds yBounds;
      List<Point2D> newData = new ArrayList<>(sampleLength);

      int lastValue = bufferSample.getSample()[startIndex];
      Point2D dataPoint = new Point2D(startIndex, lastValue);
      newData.add(dataPoint);
      yBounds = new ChartDoubleBounds(dataPoint.getY(), dataPoint.getY());

      if (sampleLength > 1)
      {
         int nextValue = bufferSample.getSample()[startIndex + 1];

         for (int i = startIndex + 1; i < endIndex; i++)
         {
            int value = nextValue;
            nextValue = bufferSample.getSample()[i + 1];
            if (value == lastValue && value == nextValue)
               continue;
            lastValue = value;
            dataPoint = new Point2D(i, value);
            newData.add(dataPoint);
            yBounds = yBounds.include(value);
         }
         int value = bufferSample.getSample()[endIndex];
         dataPoint = new Point2D(endIndex, value);
         newData.add(dataPoint);
         yBounds = yBounds.include(value);

         // FIXME Filter outputs unexpected plot for data like timestamp that has been partially loaded such that a part of the chart is still at zero.
         //         newData = DouglasPeuckerReduction.reduce(newData, epsilon * (extrema.getMaxY() - extrema.getMinY()));
      }

      return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());
   }

   // FIXME Casting the longs to double will fail for large values.
   public static DataEntry fromLongBufferSampleToLineChartData(BufferSample<long[]> bufferSample, int startIndex, int endIndex, double epsilon)
   {
      if (bufferSample == null || bufferSample.getSample().length == 0 || endIndex >= bufferSample.getSample().length)
         return null;

      int sampleLength = endIndex - startIndex;

      ChartIntegerBounds xBounds = new ChartIntegerBounds(startIndex, endIndex);
      ChartDoubleBounds yBounds;
      List<Point2D> newData = new ArrayList<>(sampleLength);

      long lastValue = bufferSample.getSample()[startIndex];
      Point2D dataPoint = new Point2D(startIndex, lastValue);
      newData.add(dataPoint);
      yBounds = new ChartDoubleBounds(dataPoint.getY(), dataPoint.getY());

      if (sampleLength > 1)
      {
         long nextValue = bufferSample.getSample()[startIndex + 1];

         for (int i = startIndex + 1; i < endIndex; i++)
         {
            long value = nextValue;
            nextValue = bufferSample.getSample()[i + 1];
            if (value == lastValue && value == nextValue)
               continue;
            lastValue = value;
            dataPoint = new Point2D(i, value);
            newData.add(dataPoint);
            yBounds = yBounds.include(value);
         }
         long value = bufferSample.getSample()[endIndex];
         dataPoint = new Point2D(endIndex, value);
         newData.add(dataPoint);
         yBounds = yBounds.include(value);

         // FIXME Filter outputs unexpected plot for data like timestamp that has been partially loaded such that a part of the chart is still at zero.
         //         newData = DouglasPeuckerReduction.reduce(newData, epsilon * (extrema.getMaxY() - extrema.getMinY()));
      }

      return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());
   }

   public static DataEntry fromByteBufferSampleToLineChartData(BufferSample<byte[]> bufferSample, int startIndex, int endIndex, double epsilon)
   {
      if (bufferSample == null || bufferSample.getSample().length == 0 || endIndex >= bufferSample.getSample().length)
         return null;

      int sampleLength = endIndex - startIndex;

      ChartIntegerBounds xBounds = new ChartIntegerBounds(startIndex, endIndex);
      ChartDoubleBounds yBounds;
      List<Point2D> newData = new ArrayList<>(sampleLength);

      byte lastValue = bufferSample.getSample()[startIndex];
      Point2D dataPoint = new Point2D(startIndex, lastValue);
      newData.add(dataPoint);
      yBounds = new ChartDoubleBounds(dataPoint.getY(), dataPoint.getY());

      if (sampleLength > 1)
      {
         byte nextValue = bufferSample.getSample()[startIndex + 1];

         for (int i = startIndex + 1; i < endIndex; i++)
         {
            byte value = nextValue;
            nextValue = bufferSample.getSample()[i + 1];
            if (value == lastValue && value == nextValue)
               continue;
            lastValue = value;
            dataPoint = new Point2D(i, value);
            newData.add(dataPoint);
            yBounds = yBounds.include(value);
         }
         byte value = bufferSample.getSample()[endIndex];
         dataPoint = new Point2D(endIndex, value);
         newData.add(dataPoint);
         yBounds = yBounds.include(value);

         newData = DouglasPeuckerReduction.reduce(newData, epsilon * (yBounds.getUpper() - yBounds.getLower()));
      }

      return new DataEntry(xBounds, yBounds, newData, bufferSample.getBufferProperties().getCurrentIndex());
   }
}
