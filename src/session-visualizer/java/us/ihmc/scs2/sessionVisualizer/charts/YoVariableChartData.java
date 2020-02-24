package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.*;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.variable.YoVariable;

public abstract class YoVariableChartData<L extends LinkedYoVariable<?>, B>
{
   private final L linkedYoVariable;

   private SessionMode lastSessionModeStatus = null;
   private final AtomicReference<SessionMode> currentSessionMode;
   private YoBufferPropertiesReadOnly lastProperties = null;
   private final AtomicReference<YoBufferPropertiesReadOnly> currentBufferProperties;

   @SuppressWarnings("rawtypes")
   private final Property<BufferSample> rawDataProperty = new SimpleObjectProperty<BufferSample>(this, "chartRawData", null);
   private final BooleanProperty publishChartData = new SimpleBooleanProperty(this, "publishChartData", false);

   private int lastUpdateEndIndex = -1;
   private DoubleArray2D lastDataSet;
   private ChartDataUpdate lastChartDataUpdate;
   private final Queue<Object> callerIDs = new ConcurrentLinkedQueue<>();
   private final Map<Object, ChartDataUpdate> newChartDataUpdate = new ConcurrentHashMap<>();

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static YoVariableChartData<?, ?> newYoVariableChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoVariable<?> linkedYoVariable)
   {
      if (linkedYoVariable instanceof LinkedYoBoolean)
         return new YoBooleanChartData(messager, topics, (LinkedYoBoolean) linkedYoVariable);
      if (linkedYoVariable instanceof LinkedYoDouble)
         return new YoDoubleChartData(messager, topics, (LinkedYoDouble) linkedYoVariable);
      if (linkedYoVariable instanceof LinkedYoInteger)
         return new YoIntegerChartData(messager, topics, (LinkedYoInteger) linkedYoVariable);
      if (linkedYoVariable instanceof LinkedYoLong)
         return new YoLongChartData(messager, topics, (LinkedYoLong) linkedYoVariable);
      if (linkedYoVariable instanceof LinkedYoEnum)
         return new YoEnumChartData<>(messager, topics, (LinkedYoEnum) linkedYoVariable);

      throw new UnsupportedOperationException("Unsupported YoVariable type: " + linkedYoVariable.getLinkedYoVariable().getClass().getSimpleName());
   }

   public YoVariableChartData(JavaFXMessager messager, SessionVisualizerTopics topics, L linkedYoVariable)
   {
      this.linkedYoVariable = linkedYoVariable;
      currentSessionMode = messager.createInput(topics.getSessionCurrentMode(), SessionMode.PAUSE);
      currentBufferProperties = messager.createInput(topics.getYoBufferCurrentProperties(), new YoBufferProperties());

      rawDataProperty.addListener((o, oldValue, newValue) -> publishChartData.set(true));
   }

   @SuppressWarnings("rawtypes")
   public void updateData()
   {
      // Always prepare new data
      BufferSample newRawData = linkedYoVariable.pollRequestedBufferSample();
      if (newRawData != null)
      {
         rawDataProperty.setValue(newRawData);
         lastUpdateEndIndex = newRawData.getBufferProperties().getOutPoint();
      }

      // Now check if a new request should be submitted.
      if (callerIDs.stream().anyMatch(callerID -> !hasNewChartData(callerID)))
      {// Only request data if JFX is keeping up with the rendering.
         if (lastProperties == null)
         { // First time requesting data.
            linkedYoVariable.requestEntireBuffer();
         }
         else if (lastSessionModeStatus == SessionMode.RUNNING && currentSessionMode.get() != SessionMode.RUNNING)
         { // The session just stopped running, need to ensure we have all the data up to the out-point.
            linkedYoVariable.requestBufferStartingFrom(lastUpdateEndIndex);
         }
         else if (lastSessionModeStatus != SessionMode.RUNNING && currentSessionMode.get() == SessionMode.RUNNING)
         { // The session just start running, need to ensure we have all the data since it started running.
            linkedYoVariable.requestActiveBufferOnly();
         }
         else if (currentSessionMode.get() == SessionMode.RUNNING)
         { // Request data from the last update point to the most recent out-point.
            if (lastUpdateEndIndex == -1)
               linkedYoVariable.requestActiveBufferOnly();
            else
               linkedYoVariable.requestBufferStartingFrom(lastUpdateEndIndex);
         }
         else if (currentBufferProperties.get().getSize() != lastProperties.getSize())
         { // Buffer was either resized or cropped, data has been shifted around, need to get a complete update.
            linkedYoVariable.requestEntireBuffer();
         }
         else if (currentBufferProperties.get().getInPoint() != lastProperties.getInPoint()
               && currentBufferProperties.get().getOutPoint() != lastProperties.getOutPoint())
         { // When cropping without actually changing the size of the buffer, the data is still being shifted around.
            linkedYoVariable.requestEntireBuffer();
         }
      }

      lastSessionModeStatus = currentSessionMode.get();
      lastProperties = currentBufferProperties.get();

      publishForCharts();
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private void publishForCharts()
   {
      if (!publishChartData.get())
         return;

      BufferSample rawData = rawDataProperty.getValue();
      if (rawData == null || rawData.getSampleLength() == 0)
         return;

      BufferSample newBufferSample = toDoubleBuffer(rawData);
      DoubleArray2D newDataSet = updateDataSet(lastDataSet, newBufferSample);
      boolean isPartialUpdate = rawData.getSampleLength() < rawData.getBufferProperties().getSize();
      ChartDataUpdate chartDataUpdate = new ChartDataUpdate(newDataSet, isPartialUpdate, rawData.getBufferProperties());
      lastChartDataUpdate = chartDataUpdate;

      if (newDataSet != null)
      {
         callerIDs.forEach(callerID -> newChartDataUpdate.put(callerID, chartDataUpdate));
         lastDataSet = newDataSet;
      }

      publishChartData.set(false);
   }

   protected abstract BufferSample<double[]> toDoubleBuffer(BufferSample<B> yoVariableBuffer);

   public void registerCaller(Object callerID)
   {
      callerIDs.add(callerID);
      if (lastDataSet != null)
         newChartDataUpdate.put(callerID, lastChartDataUpdate);
   }

   public void removeCaller(Object callerID)
   {
      callerIDs.remove(callerID);
      newChartDataUpdate.remove(callerID);
   }

   public boolean hasNewChartData(Object callerID)
   {
      return newChartDataUpdate.get(callerID) != null;
   }

   public ChartDataUpdate pollChartData(Object callerID)
   {
      return newChartDataUpdate.remove(callerID);
   }

   public YoVariable<?> getYoVariable()
   {
      return linkedYoVariable.getLinkedYoVariable();
   }

   public boolean isCurrentlyInUse()
   {
      return !callerIDs.isEmpty();
   }

   public static DoubleArray2D updateDataSet(DoubleArray2D lastDataSet, BufferSample<double[]> bufferSample)
   {
      double[] sample = bufferSample.getSample();
      int sampleLength = bufferSample.getSampleLength();
      int bufferSize = bufferSample.getBufferProperties().getSize();

      if (bufferSample == null || sampleLength == 0)
         return null;

      DoubleArray2D dataSet = new DoubleArray2D(bufferSize);
      dataSet.initializeX();

      int sampleStart = bufferSample.getFrom();
      int sampleEnd = bufferSample.getTo();

      double yMin = Double.POSITIVE_INFINITY;
      double yMax = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < bufferSize; i++)
      {
         double y = 0.0;

         if (sampleStart <= sampleEnd)
         {
            if (i >= sampleStart && i <= sampleEnd)
               y = sample[i - sampleStart];
            else if (lastDataSet != null)
               y = lastDataSet.y[i];
         }
         else
         {
            if (i <= sampleEnd)
               y = sample[i - sampleStart + bufferSize];
            else if (i >= sampleStart)
               y = sample[i - sampleStart];
            else if (lastDataSet != null)
               y = lastDataSet.y[i];
         }

         // TODO Need to check if chart-fx handles NaN.
         if (!Double.isFinite(y))
            y = 0.0;

         dataSet.y[i] = y;

         yMin = Math.min(yMin, y);
         yMax = Math.max(yMax, y);
      }

      dataSet.yMin = yMin;
      dataSet.yMax = yMax;

      return dataSet;
   }

   public static class ChartDataUpdate
   {
      private final DoubleArray2D dataSet;
      private final boolean isPartialUpdate;
      private final YoBufferPropertiesReadOnly bufferProperties;

      public ChartDataUpdate(DoubleArray2D dataSet, boolean isPartialUpdate, YoBufferPropertiesReadOnly bufferProperties)
      {
         this.dataSet = dataSet;
         this.isPartialUpdate = isPartialUpdate;
         this.bufferProperties = bufferProperties;
      }

      public void readUpdate(YoDoubleDataSet chartDataSet, int lastUpdateEndIndex)
      {
         readUpdate(chartDataSet.getRawDataSet(), lastUpdateEndIndex);
         chartDataSet.setRange(dataSet.xMin, dataSet.xMax, dataSet.yMin, dataSet.yMax);
         chartDataSet.getAxisDescriptions().forEach(AxisDescription::clear);
      }

      public void readUpdate(DoubleDataSet chartDataSet, int lastUpdateEndIndex)
      {
         if (!isPartialUpdate || lastUpdateEndIndex == -1)
         { // Refresh the entire dataSet
            chartDataSet.set(0, dataSet.x, dataSet.y);
         }
         else
         { // Refresh only a small portion of the dataSet: ]lastUpdateEndIndex, updateEndIndex].
            int updateEndIndex = bufferProperties.getOutPoint();

            if (lastUpdateEndIndex == updateEndIndex)
            {
               return;
            }

            if (lastUpdateEndIndex < updateEndIndex)
            { // Simple case, can be done in a single copy.
               double[] xValues = Arrays.copyOfRange(dataSet.x, lastUpdateEndIndex + 1, updateEndIndex + 1);
               double[] yValues = Arrays.copyOfRange(dataSet.y, lastUpdateEndIndex + 1, updateEndIndex + 1);
               chartDataSet.set(lastUpdateEndIndex + 1, xValues, yValues);
            }
            else
            { // The update is wrapping over the buffer.
               double[] xValues = Arrays.copyOfRange(dataSet.x, lastUpdateEndIndex + 1, dataSet.x.length);
               double[] yValues = Arrays.copyOfRange(dataSet.y, lastUpdateEndIndex + 1, dataSet.y.length);
               chartDataSet.set(lastUpdateEndIndex + 1, xValues, yValues);

               xValues = Arrays.copyOfRange(dataSet.x, 0, updateEndIndex + 1);
               yValues = Arrays.copyOfRange(dataSet.y, 0, updateEndIndex + 1);
               chartDataSet.set(0, xValues, yValues);
            }
         }
      }

      public int getUpdateEndIndex()
      {
         return bufferProperties.getOutPoint();
      }
   }

   private static class DoubleArray2D
   {
      private final double[] x, y;
      private double xMin, xMax, yMin, yMax;

      public DoubleArray2D(int size)
      {
         x = new double[size];
         y = new double[size];
      }

      public void initializeX()
      {
         for (int i = 0; i < x.length; i++)
         {
            x[i] = i;
         }
         xMin = 0;
         xMax = x.length - 1;
      }
   }
}
