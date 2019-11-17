package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoLong;

public class YoLongBuffer extends YoVariableBuffer<YoLong>
{
   private long[] buffer = new long[0];

   public YoLongBuffer(YoLong yoLong, YoBufferPropertiesReadOnly properties)
   {
      super(yoLong, properties);
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
   public BufferSample<long[]> copy(int from, int length)
   {
      return new BufferSample<>(from, properties.getSize(), BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoLong newLinkedYoVariable(YoLong variableToLink)
   {
      return new LinkedYoLong(variableToLink, this);
   }
}
