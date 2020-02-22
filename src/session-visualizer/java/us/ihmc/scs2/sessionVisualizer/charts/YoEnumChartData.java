package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoEnum;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoEnumChartData<E extends Enum<E>> extends YoVariableChartData<LinkedYoEnum<E>, byte[]>
{
   public YoEnumChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoEnum<E> linkedYoEnum)
   {
      super(messager, topics, linkedYoEnum);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<byte[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      int bufferSize = yoVariableBuffer.getBufferSize();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, bufferSize, sample, sampleLength);
   }
}
