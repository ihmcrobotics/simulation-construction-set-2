package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.messager.Messager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoEnum;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoEnumChartData<E extends Enum<E>> extends YoVariableChartData<LinkedYoEnum<E>, byte[]>
{
   public YoEnumChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoEnum<E> linkedYoEnum)
   {
      super(messager, topics, linkedYoEnum);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<byte[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      YoBufferPropertiesReadOnly bufferProperties = yoVariableBuffer.getBufferProperties();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, sample, sampleLength, bufferProperties);
   }
}
