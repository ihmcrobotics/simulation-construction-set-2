package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoBoolean;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public class YoBooleanChartData extends YoVariableChartData<LinkedYoBoolean, boolean[]>
{
   public YoBooleanChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoBoolean linkedYoBoolean)
   {
      super(messager, topics, linkedYoBoolean);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<boolean[]> yoVariableBuffer)
   {
      int from = yoVariableBuffer.getFrom();
      int bufferSize = yoVariableBuffer.getBufferSize();
      double[] sample = BufferTools.toDoubleArray(yoVariableBuffer.getSample());
      int sampleLength = yoVariableBuffer.getSampleLength();
      return new BufferSample<>(from, bufferSize, sample, sampleLength);
   }
}
