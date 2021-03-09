package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.messager.Messager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoLongChartData extends YoVariableChartData<LinkedYoLong, long[]>
{
   public YoLongChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoLong linkedYoLong)
   {
      super(messager, topics, linkedYoLong);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<long[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }
}