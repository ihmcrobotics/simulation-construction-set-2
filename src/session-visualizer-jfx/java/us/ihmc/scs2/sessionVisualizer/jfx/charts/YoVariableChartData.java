package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.messager.Messager;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoBoolean;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;
import us.ihmc.scs2.sharedMemory.LinkedYoEnum;
import us.ihmc.scs2.sharedMemory.LinkedYoInteger;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.sharedMemory.YoBufferProperties;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoVariableChartData
{
   private final LinkedYoVariable<?> linkedYoVariable;

   private SessionMode lastSessionModeStatus = null;
   private final AtomicReference<SessionMode> currentSessionMode;
   private YoBufferPropertiesReadOnly lastProperties = null;
   private final AtomicReference<YoBufferPropertiesReadOnly> currentBufferPropertiesReference;

   @SuppressWarnings("rawtypes")
   private final AtomicReference<BufferSample> rawDataProperty = new AtomicReference<>(null);
   private final AtomicBoolean publishChartData = new AtomicBoolean(false);

   private int lastUpdateEndIndex = -1;
   private DoubleArray lastDataSet;
   private ChartDataUpdate lastChartDataUpdate;
   private final Queue<Object> callerIDs = new ConcurrentLinkedQueue<>();
   private final Map<Object, ChartDataUpdate> newChartDataUpdate = new ConcurrentHashMap<>();

   @SuppressWarnings("rawtypes")
   private final Function<BufferSample<double[]>, BufferSample> bufferConverterFunction;

   public YoVariableChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoVariable<?> linkedYoVariable)
   {
      this.linkedYoVariable = linkedYoVariable;
      currentSessionMode = messager.createInput(topics.getSessionCurrentMode(), SessionMode.PAUSE);
      currentBufferPropertiesReference = messager.createInput(topics.getYoBufferCurrentProperties(), new YoBufferProperties());

      if (linkedYoVariable instanceof LinkedYoBoolean)
         bufferConverterFunction = in -> booleanToDoubleBuffer(in);
      else if (linkedYoVariable instanceof LinkedYoDouble)
         bufferConverterFunction = in -> in;
      else if (linkedYoVariable instanceof LinkedYoEnum<?>)
         bufferConverterFunction = in -> byteToDoubleBuffer(in);
      else if (linkedYoVariable instanceof LinkedYoInteger)
         bufferConverterFunction = in -> integerToDoubleBuffer(in);
      else if (linkedYoVariable instanceof LinkedYoLong)
         bufferConverterFunction = in -> longToDoubleBuffer(in);
      else
         throw new UnsupportedOperationException("Unsupported YoVariable type: " + linkedYoVariable.getLinkedYoVariable().getClass().getSimpleName());
   }

   @SuppressWarnings("rawtypes")
   public void updateData()
   {
      // Always prepare new data
      BufferSample newRawData = linkedYoVariable.pollRequestedBufferSample();
      if (newRawData != null)
      {
         rawDataProperty.set(newRawData);
         publishChartData.set(true);
         lastUpdateEndIndex = newRawData.getBufferProperties().getOutPoint();
      }

      YoBufferPropertiesReadOnly currentBufferProperties = currentBufferPropertiesReference.get();

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
         else if (currentBufferProperties.getSize() != lastProperties.getSize())
         { // Buffer was either resized or cropped, data has been shifted around, need to get a complete update.
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
         else if (currentBufferProperties.getInPoint() != lastProperties.getInPoint() && currentBufferProperties.getOutPoint() != lastProperties.getOutPoint())
         { // When cropping without actually changing the size of the buffer, the data is still being shifted around.
            linkedYoVariable.requestEntireBuffer();
         }
      }

      lastSessionModeStatus = currentSessionMode.get();
      lastProperties = currentBufferProperties;

      publishForCharts();
   }

   private void publishForCharts()
   {
      if (!publishChartData.get())
         return;

      @SuppressWarnings("rawtypes")
      BufferSample rawData = rawDataProperty.get();
      if (rawData == null || rawData.getSampleLength() == 0)
         return;

      @SuppressWarnings("unchecked")
      BufferSample<double[]> newBufferSample = bufferConverterFunction.apply(rawData);
      if (lastDataSet != null && newBufferSample.getBufferProperties().getSize() != lastDataSet.size)
         lastDataSet = null;
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

      public void readUpdate(NumberSeries chartDataSet, int lastUpdateEndIndex)
      {
         chartDataSet.getLock().writeLock().lock();

         try
         {
            // Resizing the cart data
            while (chartDataSet.getData().size() < dataSet.size)
               chartDataSet.getData().add(new Point2D());
            while (chartDataSet.getData().size() > dataSet.size)
               chartDataSet.getData().remove(chartDataSet.getData().size() - 1);

            for (int i = 0; i < dataSet.size; i++)
            {
               chartDataSet.getData().get(i).set(i, dataSet.values[i]);
            }

            chartDataSet.bufferCurrentIndexProperty().set(bufferProperties.getCurrentIndex());
            chartDataSet.xBoundsProperty().setValue(new ChartIntegerBounds(0, dataSet.size));
            chartDataSet.yBoundsProperty().setValue(new ChartDoubleBounds(dataSet.valueMin, dataSet.valueMax));
         }
         finally
         {
            chartDataSet.getLock().writeLock().unlock();
            chartDataSet.markDirty();
         }
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

   private static BufferSample<double[]> booleanToDoubleBuffer(BufferSample<?> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray((boolean[]) yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }

   private static BufferSample<double[]> byteToDoubleBuffer(BufferSample<?> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray((byte[]) yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }

   private static BufferSample<double[]> integerToDoubleBuffer(BufferSample<?> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray((int[]) yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }

   private static BufferSample<double[]> longToDoubleBuffer(BufferSample<?> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray((long[]) yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }
}