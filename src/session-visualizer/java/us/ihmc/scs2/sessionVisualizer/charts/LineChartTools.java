package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Axis;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.scs2.sessionVisualizer.tools.DouglasPeuckerReduction;
import us.ihmc.scs2.sessionVisualizer.tools.NumberFormatTools;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class LineChartTools
{
   public static String defaultYoVariableValueFormatter(YoVariable<?> yoVariable)
   {
      if (yoVariable instanceof YoDouble)
         return NumberFormatTools.doubleToString(yoVariable.getValueAsDouble(), 3);
      else
         return LineChartTools.getYoVariableValueAsString(yoVariable, EuclidCoreIOTools.getStringFormat(11, 8));
   }

   public static String getYoVariableValueAsString(YoVariable<?> yoVariable, String format)
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
         return new DataEntry(xBounds, yBounds, newData);

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

      return new DataEntry(xBounds, yBounds, newData);
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

      return new DataEntry(xBounds, yBounds, newData);
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

      return new DataEntry(xBounds, yBounds, newData);
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

      return new DataEntry(xBounds, yBounds, newData);
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

      return new DataEntry(xBounds, yBounds, newData);
   }

   public static void computePath(List<Point2D> data, Axis<Number> xAxis, Axis<Number> yAxis, Path pathToCompute)
   {
      pathToCompute.getElements().setAll(computePath(data, xAxis, yAxis).getElements());
   }

   public static Path computePath(List<Point2D> data, Axis<Number> xAxis, Axis<Number> yAxis)
   {
      List<PathElement> pathElements = new ArrayList<>(data.size());

      if (!data.isEmpty())
      {
         pathElements.add(newMoveTo(data.get(0), xAxis, yAxis));

         for (Point2D dataPoint : data.subList(1, data.size()))
         {
            pathElements.add(newLineTo(dataPoint, xAxis, yAxis));
         }
      }

      return new Path(pathElements);
   }

   public static LineTo newLineTo(Point2D dataPoint, Axis<Number> xAxis, Axis<Number> yAxis)
   {
      double x = xAxis.getDisplayPosition(dataPoint.getX());
      double y = yAxis.getDisplayPosition(dataPoint.getY());
      return new LineTo(x, y);
   }

   public static MoveTo newMoveTo(Point2D dataPoint, Axis<Number> xAxis, Axis<Number> yAxis)
   {
      double x = xAxis.getDisplayPosition(dataPoint.getX());
      double y = yAxis.getDisplayPosition(dataPoint.getY());
      return new MoveTo(x, y);
   }

   public static Canvas newCanvas(DataEntry dataEntry, Axis<Number> xAxis, Axis<Number> yAxis)
   {
      return newCanvas(dataEntry == null ? null : dataEntry.getData(), xAxis, yAxis);
   }

   public static Canvas newCanvas(List<Point2D> data, Axis<Number> xAxis, Axis<Number> yAxis)
   {
      Canvas canvas = new Canvas(xAxis.getWidth(), yAxis.getHeight());
      GraphicsContext gc = canvas.getGraphicsContext2D();
      gc.setFill(Color.TRANSPARENT);

      if (data == null || data.isEmpty())
         return canvas;

      double x, y;

      if (!data.isEmpty())
      {
         x = xAxis.getDisplayPosition(data.get(0).getX());
         y = yAxis.getDisplayPosition(data.get(0).getY());
         gc.moveTo(x, y);

         for (Point2D dataPoint : data.subList(1, data.size()))
         {
            x = xAxis.getDisplayPosition(dataPoint.getX());
            y = yAxis.getDisplayPosition(dataPoint.getY());
            gc.lineTo(x, y);
         }
      }

      gc.stroke();

      java.awt.Color awtColor = new java.awt.Color(new Random().nextInt());
      gc.setStroke(Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
      gc.setLineWidth(1.5);
      return canvas;
   }
}
