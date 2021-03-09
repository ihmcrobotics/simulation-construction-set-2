package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.messager.Messager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;

public class YoDoubleChartData extends YoVariableChartData<LinkedYoDouble, double[]>
{
   public YoDoubleChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoDouble linkedYoDouble)
   {
      super(messager, topics, linkedYoDouble);
   }

   @Override
   protected BufferSample<double[]> toDoubleBuffer(BufferSample<double[]> yoVariableBuffer)
   {
      return yoVariableBuffer;
   }
}