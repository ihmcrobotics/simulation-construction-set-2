package us.ihmc.scs2.sessionVisualizer.charts;

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

public abstract class YoVariableChartData<L extends LinkedYoVariable, B>
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
   private DoubleArray lastDataSet;
   private ChartDataUpdate lastChartDataUpdate;
   private final Queue<Object> callerIDs = new ConcurrentLinkedQueue<>();
   private final Map<Object, ChartDataUpdate> newChartDataUpdate = new ConcurrentHashMap<>();

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static YoVariableChartData<?, ?> newYoVariableChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoVariable linkedYoVariable)
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
      if (lastSessionModeStatus == SessionMode.RUNNING && currentSessionMode.get() != SessionMode.RUNNING)
      { // The session just stopped running, need to ensure we have all the data up to the out-point.
         linkedYoVariable.requestBufferStartingFrom(lastUpdateEndIndex);
      }
      else if (callerIDs.stream().anyMatch(callerID -> !hasNewChartData(callerID)))
      {// Only request data if JFX is keeping up with the rendering.
         if (lastProperties == null)
         { // First time requesting data.
            linkedYoVariable.requestEntireBuffer();
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
      DoubleArray newDataSet = updateDataSet(lastDataSet, newBufferSample);
      ChartDataUpdate chartDataUpdate = new ChartDataUpdate(newDataSet, rawData.getBufferProperties());
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
      if (newChartDataUpdate.isEmpty())
         return null;
      else
         return newChartDataUpdate.remove(callerID);
   }

   public YoVariable getYoVariable()
   {
      return linkedYoVariable.getLinkedYoVariable();
   }

   public boolean isCurrentlyInUse()
   {
      return !callerIDs.isEmpty();
   }

   public static DoubleArray updateDataSet(DoubleArray lastDataSet, BufferSample<double[]> bufferSample)
   {
      int sampleLength = bufferSample.getSampleLength();
      int bufferSize = bufferSample.getBufferProperties().getSize();

      if (bufferSample == null || sampleLength == 0)
         return null;

      DoubleArray dataSet = new DoubleArray(bufferSize);

      double yCurrent = getValueAt(0, lastDataSet, bufferSample);
      dataSet.values[0] = yCurrent;
      double yMin = yCurrent;
      double yMax = yCurrent;

      for (int i = 1; i < bufferSize; i++)
      {
         yCurrent = getValueAt(i, lastDataSet, bufferSample);
         dataSet.values[i] = yCurrent;
         yMin = Math.min(yMin, yCurrent);
         yMax = Math.max(yMax, yCurrent);
      }

      dataSet.size = bufferSize;
      dataSet.valueMin = yMin;
      dataSet.valueMax = yMax;

      return dataSet;
   }

   private static double getValueAt(int index, DoubleArray completeDataSet, BufferSample<double[]> partialBufferSample)
   {
      double[] sample = partialBufferSample.getSample();
      int sampleStart = partialBufferSample.getFrom();
      int sampleEnd = partialBufferSample.getTo();
      int bufferSize = partialBufferSample.getBufferProperties().getSize();

      double y = 0.0;

      if (sampleStart <= sampleEnd)
      {
         if (index >= sampleStart && index <= sampleEnd)
            y = sample[index - sampleStart];
         else if (completeDataSet != null)
            y = completeDataSet.values[index];
      }
      else
      {
         if (index <= sampleEnd)
            y = sample[index - sampleStart + bufferSize];
         else if (index >= sampleStart)
            y = sample[index - sampleStart];
         else if (completeDataSet != null)
            y = completeDataSet.values[index];
      }

      // TODO Need to check if chart-fx handles NaN.
      if (!Double.isFinite(y))
         y = 0.0;

      return y;
   }

   public static class ChartDataUpdate
   {
      private final DoubleArray dataSet;
      private final YoBufferPropertiesReadOnly bufferProperties;

      public ChartDataUpdate(DoubleArray dataSet, YoBufferPropertiesReadOnly bufferProperties)
      {
         this.dataSet = dataSet;
         this.bufferProperties = bufferProperties;
      }

      public void readUpdate(YoDoubleDataSet chartDataSet, int lastUpdateEndIndex)
      {
         readUpdate(chartDataSet.getRawDataSet(), lastUpdateEndIndex);
         chartDataSet.setRange(0, dataSet.size - 1, dataSet.valueMin, dataSet.valueMax);
      }

      public void readUpdate(DoubleDataSet chartDataSet, int lastUpdateEndIndex)
      {
         if (chartDataSet.getDataCount() != dataSet.size)
            chartDataSet.resize(dataSet.size);

         double[] xValues = chartDataSet.getXValues();
         double[] yValues = chartDataSet.getYValues();

         for (int i = 0; i < dataSet.size; i++)
            xValues[i] = i;
         System.arraycopy(dataSet.values, 0, yValues, 0, dataSet.size);
         chartDataSet.getAxisDescriptions().forEach(AxisDescription::clear);
      }

      public int getUpdateEndIndex()
      {
         return bufferProperties.getOutPoint();
      }
   }

   private static class DoubleArray
   {
      private int size;
      private final double[] values;
      private double valueMin, valueMax;

      public DoubleArray(int size)
      {
         this.size = size;
         values = new double[size];
      }
   }
}
