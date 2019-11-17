package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoBoolean;

public class YoBooleanChartData extends YoVariableChartData<LinkedYoBoolean, boolean[]>
{
   public YoBooleanChartData(JavaFXMessager messager, SCSGUITopics topics, LinkedYoBoolean linkedYoBoolean)
   {
      super(messager, topics, linkedYoBoolean);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<boolean[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromBooleanBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex);
   }
}
