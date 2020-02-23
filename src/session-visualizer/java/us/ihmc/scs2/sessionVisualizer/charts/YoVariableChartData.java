package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import de.gsi.dataset.DataSet2D;
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
   private final Property<ChartIntegerBounds> chartBoundsProperty = new SimpleObjectProperty<ChartIntegerBounds>(this, "chartBounds", null);
   private final BooleanProperty publishChartData = new SimpleBooleanProperty(this, "publishChartData", false);

   private int lastUpdateEndIndex = -1;
   private DataSet2D lastChartData;
   private final Queue<Object> callerIDs = new ConcurrentLinkedQueue<>();
   private final Map<Object, DataSet2D> newChartData = new ConcurrentHashMap<>();

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
      chartBoundsProperty.addListener((o, oldValue, newValue) -> publishChartData.set(true));
   }

   @SuppressWarnings("rawtypes")
   public void updateData()
   {
      // Always prepare new data
      BufferSample newRawData = linkedYoVariable.pollRequestedBufferSample();
      if (newRawData != null)
      {
         rawDataProperty.setValue(newRawData);
         if (newRawData.getSampleLength() == newRawData.getBufferProperties().getSize())
            lastUpdateEndIndex = newRawData.getBufferProperties().getOutPoint();
         else
            lastUpdateEndIndex = newRawData.getTo();
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
         else if (currentBufferProperties.get().getInPoint() != lastProperties.getInPoint() && currentBufferProperties.get().getOutPoint() != lastProperties.getOutPoint())
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
      DataSet2D chartData = updateLineChartData(lastChartData, newBufferSample);

      if (chartData != null)
      {
         callerIDs.forEach(callerID -> newChartData.put(callerID, chartData));
         lastChartData = chartData;
      }

      publishChartData.set(false);
   }

   protected abstract BufferSample<double[]> toDoubleBuffer(BufferSample<B> yoVariableBuffer);

   public void registerCaller(Object callerID)
   {
      callerIDs.add(callerID);
      if (lastChartData != null)
         newChartData.put(callerID, lastChartData);
   }

   public void removeCaller(Object callerID)
   {
      callerIDs.remove(callerID);
      newChartData.remove(callerID);
   }

   public boolean hasNewChartData(Object callerID)
   {
      return newChartData.get(callerID) != null;
   }

   public DataSet2D pollChartData(Object callerID)
   {
      return newChartData.remove(callerID);
   }

   public YoVariable<?> getYoVariable()
   {
      return linkedYoVariable.getLinkedYoVariable();
   }

   public boolean isCurrentlyInUse()
   {
      return !callerIDs.isEmpty();
   }

   public Property<ChartIntegerBounds> chartBoundsProperty()
   {
      return chartBoundsProperty;
   }

   public static DataSet2D updateLineChartData(DataSet2D lastDataSet, BufferSample<double[]> bufferSample)
   {
      double[] sample = bufferSample.getSample();
      int sampleLength = bufferSample.getSampleLength();
      int bufferSize = bufferSample.getBufferProperties().getSize();

      if (bufferSample == null || sampleLength == 0)
         return null;

      String name = "Unknown";
      DoubleDataSet dataSet = new DoubleDataSet(name, bufferSize);

      int sampleStart = bufferSample.getFrom();
      int sampleEnd = bufferSample.getTo();

      for (int i = 0; i < bufferSize; i++)
      {
         double x = i;
         double y = 0.0;

         if (sampleStart <= sampleEnd)
         {
            if (i >= sampleStart && i <= sampleEnd)
               y = sample[i - sampleStart];
            else if (lastDataSet != null)
               y = lastDataSet.getY(i);
         }
         else
         {
            if (i <= sampleEnd)
               y = sample[i - sampleStart + bufferSize];
            else if (i >= sampleStart)
               y = sample[i - sampleStart];
            else if (lastDataSet != null)
               y = lastDataSet.getY(i);
         }

         // TODO Need to check if chart-fx handles NaN.
         if (!Double.isFinite(y))
            y = 0.0;

         dataSet.add(x, y);
      }

      return dataSet;
   }
}
