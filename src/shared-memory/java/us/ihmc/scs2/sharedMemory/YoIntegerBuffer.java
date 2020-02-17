package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerBuffer extends YoVariableBuffer<YoInteger>
{
   private int[] buffer;

   public YoIntegerBuffer(YoInteger yoInteger, YoBufferPropertiesReadOnly properties)
   {
      super(yoInteger, properties);
      buffer = new int[properties.getSize()];
   }

   @Override
   public void resizeBuffer(int from, int length)
   {
      if (from == 0 && length == buffer.length)
         return;
      buffer = BufferTools.ringArrayCopy(buffer, from, length);
   }

   @Override
   public void writeBufferAt(int index)
   {
      buffer[index] = yoVariable.getValue();
   }

   @Override
   public void readBufferAt(int index)
   {
      yoVariable.set(buffer[index]);
   }

   @Override
   public BufferSample<int[]> copy(int from, int length)
   {
      return new BufferSample<>(from, buffer.length, BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoInteger newLinkedYoVariable(YoInteger variableToLink)
   {
      return new LinkedYoInteger(variableToLink, this);
   }

   int[] getBuffer()
   {
      return buffer;
   }
}
