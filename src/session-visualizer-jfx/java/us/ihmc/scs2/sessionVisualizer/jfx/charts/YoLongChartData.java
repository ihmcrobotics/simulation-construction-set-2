package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import us.ihmc.messager.Messager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;

public class YoLongChartData extends YoVariableChartData<LinkedYoLong, long[]>
{
   public YoLongChartData(Messager messager, SessionVisualizerTopics topics, LinkedYoLong linkedYoLong)
   {
      super(messager, topics, linkedYoLong);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<long[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromLongBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex, epsilon);
   }
}