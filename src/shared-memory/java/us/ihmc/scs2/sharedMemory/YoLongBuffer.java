package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongBuffer extends YoVariableBuffer<YoLong>
{
   private long[] buffer;

   public YoLongBuffer(YoLong yoLong, YoBufferPropertiesReadOnly properties)
   {
      super(yoLong, properties);
      buffer = new long[properties.getSize()];
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
   long getValueAsLongBits(int index)
   {
      return buffer[index];
   }

   @Override
   public BufferSample<long[]> copy(int from, int length, YoBufferPropertiesReadOnly properties)
   {
      return new BufferSample<>(from, BufferTools.ringArrayCopy(buffer, from, length), length, properties);
   }

   @Override
   LinkedYoLong newLinkedYoVariable(YoLong variableToLink)
   {
      return new LinkedYoLong(variableToLink, this);
   }

   long[] getBuffer()
   {
      return buffer;
   }
}
