package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoLong;

public class YoLongChartData extends YoVariableChartData<LinkedYoLong, long[]>
{
   public YoLongChartData(JavaFXMessager messager, SCSGUITopics topics, LinkedYoLong linkedYoLong)
   {
      super(messager, topics, linkedYoLong);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<long[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromLongBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex, epsilon);
   }
}
