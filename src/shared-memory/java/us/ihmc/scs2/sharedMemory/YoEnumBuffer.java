package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoEnum;

public class YoEnumBuffer<E extends Enum<E>> extends YoVariableBuffer<YoEnum<E>>
{
   private byte[] buffer;

   public YoEnumBuffer(YoEnum<E> yoEnum, YoBufferPropertiesReadOnly properties)
   {
      super(yoEnum, properties);
      buffer = new byte[properties.getSize()];
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
      buffer[index] = (byte) yoVariable.getOrdinal();
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
   public BufferSample<byte[]> copy(int from, int length)
   {
      return new BufferSample<>(from, buffer.length, BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoEnum<E> newLinkedYoVariable(YoEnum<E> variableToLink)
   {
      return new LinkedYoEnum<>(variableToLink, this);
   }

   byte[] getBuffer()
   {
      return buffer;
   }
}
