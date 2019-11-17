package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerBuffer extends YoVariableBuffer<YoInteger>
{
   private int[] buffer = new int[0];

   public YoIntegerBuffer(YoInteger yoInteger, YoBufferPropertiesReadOnly properties)
   {
      super(yoInteger, properties);
   }

   @Override
   public void resizeBuffer(int from, int length)
   {
      buffer = BufferTools.ringArrayCopy(buffer, from, length);
   }

   @Override
   public void writeBuffer()
   {
      buffer[properties.getCurrentIndex()] = yoVariable.getValue();
   }

   @Override
   public void readBuffer()
   {
      yoVariable.set(buffer[properties.getCurrentIndex()]);
   }

   @Override
   public BufferSample<int[]> copy(int from, int length)
   {
      return new BufferSample<>(from, properties.getSize(), BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoInteger newLinkedYoVariable(YoInteger variableToLink)
   {
      return new LinkedYoInteger(variableToLink, this);
   }
}
