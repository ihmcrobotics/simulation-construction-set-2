package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.messager.Messager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoInteger;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoIntegerChartData extends YoVariableChartData<LinkedYoInteger, int[]>
{
   public YoIntegerChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoInteger linkedYoInteger)
   {
      super(messager, topics, linkedYoInteger);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<int[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }
}
