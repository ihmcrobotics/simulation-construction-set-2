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
   public void writeBuffer()
   {
      buffer[properties.getCurrentIndex()] = (byte) yoVariable.getOrdinal();
   }

   @Override
   public void readBuffer()
   {
      yoVariable.set(buffer[properties.getCurrentIndex()]);
   }

   @Override
   public BufferSample<byte[]> copy(int from, int length)
   {
      return new BufferSample<>(from, properties.getSize(), BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoEnum<E> newLinkedYoVariable(YoEnum<E> variableToLink)
   {
      return new LinkedYoEnum<E>(variableToLink, this);
   }

   byte[] getBuffer()
   {
      return buffer;
   }
}
