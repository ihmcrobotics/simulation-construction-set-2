package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

   private DataEntry lastChartData;
   private final Queue<Object> callerIDs = new ConcurrentLinkedQueue<>();
   private final Map<Object, DataEntry> newChartData = new ConcurrentHashMap<>();

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static YoVariableChartData<?, ?> newYoVariableChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoVariable<?> linkedYoVariable)
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

   public YoVariableChartData(Messager messager, SessionVisualizerTopics topics, L linkedYoVariable)
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
         rawDataProperty.setValue(newRawData);
      // Now check if a new request should be submitted.
      if (callerIDs.stream().anyMatch(callerID -> !hasNewChartData(callerID)))
      {// Only request data if JFX is keeping up with the rendering.
         if (currentSessionMode.get() == SessionMode.RUNNING || currentSessionMode.get() != lastSessionModeStatus)
         { // Only request data when the session is running or when the session state changes.
            linkedYoVariable.requestEntireBuffer();
         }
         else if (lastProperties == null || currentBufferProperties.get().getOutPoint() != lastProperties.getOutPoint()
               || currentBufferProperties.get().getSize() != lastProperties.getSize())
         {
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

      DataEntry chartData;
      BufferSample rawData = rawDataProperty.getValue();
      if (rawData == null)
         return;
      ChartIntegerBounds bounds = chartBoundsProperty.getValue();

      if (lastChartData == null || lastProperties == null || bounds == null)
      {
         chartData = extractChartData(rawData, 0.001);
      }
      else
      {
         int lower = bounds.getLower();
         int upper = bounds.getUpper();
         // Add a little more than needed so when shifting the chart, some data is already available.
         int length = upper - lower;
         int margin = Math.max(length / 20, 1);
         lower = Math.max(0, lower - margin);
         upper = Math.min(rawData.getBufferProperties().getSize() - 1, upper + margin);
         chartData = extractChartData(rawData, lower, upper, 0.001);
      }

      if (chartData != null)
      {
         callerIDs.forEach(callerID -> newChartData.put(callerID, chartData));
         lastChartData = chartData;
      }

      publishChartData.set(false);
   }

   protected DataEntry extractChartData(BufferSample<B> yoVariableBuffer, double epsilon)
   {
      return extractChartData(yoVariableBuffer, 0, yoVariableBuffer.getBufferProperties().getSize() - 1, epsilon);
   }

   protected abstract DataEntry extractChartData(BufferSample<B> yoVariableBuffer, int startIndex, int endIndex, double epsilon);

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

   public DataEntry pollChartData(Object callerID)
   {
      return newChartData.remove(callerID);
   }

   public YoVariable getYoVariable()
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
}