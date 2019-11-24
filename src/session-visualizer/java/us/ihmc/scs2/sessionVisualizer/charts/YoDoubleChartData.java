package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoDouble;

public class YoDoubleChartData extends YoVariableChartData<LinkedYoDouble, double[]>
{
   public YoDoubleChartData(JavaFXMessager messager, SessionVisualizerTopics topics, LinkedYoDouble linkedYoDouble)
   {
      super(messager, topics, linkedYoDouble);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<double[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromDoubleBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex, epsilon);
   }
}
