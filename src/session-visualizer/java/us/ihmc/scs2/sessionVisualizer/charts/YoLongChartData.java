package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoLongChartData extends YoVariableChartData<LinkedYoLong, long[]>
{
   public YoLongChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoLong linkedYoLong)
   {
      super(messager, topics, linkedYoLong);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<long[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      int bufferSize = yoVariableBuffer.getBufferSize();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, bufferSize, sample, sampleLength);
   }
}
