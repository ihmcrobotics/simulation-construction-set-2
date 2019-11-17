package us.ihmc.scs2.sessionVisualizer.charts;

import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SCSGUITopics;
import us.ihmc.scs2.sharedMemory.BufferSample;
import us.ihmc.scs2.sharedMemory.LinkedYoEnum;

public class YoEnumChartData<E extends Enum<E>> extends YoVariableChartData<LinkedYoEnum<E>, byte[]>
{
   public YoEnumChartData(JavaFXMessager messager, SCSGUITopics topics, LinkedYoEnum<E> linkedYoEnum)
   {
      super(messager, topics, linkedYoEnum);
   }

   @Override
   protected DataEntry extractChartData(BufferSample<byte[]> yoVariableBuffer, int startIndex, int endIndex, double epsilon)
   {
      return LineChartTools.fromByteBufferSampleToLineChartData(yoVariableBuffer, startIndex, endIndex, epsilon);
   }
}
