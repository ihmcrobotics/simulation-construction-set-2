package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoInteger;

public class YoIntegerChartData extends YoVariableChartData<LinkedYoInteger, int[]>
{
   public YoIntegerChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoInteger linkedYoInteger)
   {
      super(messager, topics, linkedYoInteger);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<int[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromIntegerBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex, epsilon);
   }
}
