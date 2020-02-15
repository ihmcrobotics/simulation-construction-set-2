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
   public BufferSample<long[]> copy(int from, int length)
   {
      return new BufferSample<>(from, properties.getSize(), BufferTools.ringArrayCopy(buffer, from, length), length);
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
