package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanBuffer extends YoVariableBuffer<YoBoolean>
{
   private boolean[] buffer;

   public YoBooleanBuffer(YoBoolean yoBoolean, YoBufferPropertiesReadOnly properties)
   {
      super(yoBoolean, properties);
      buffer = new boolean[properties.getSize()];
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
   public BufferSample<boolean[]> copy(int from, int length)
   {
      return new BufferSample<>(from, buffer.length, BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoBoolean newLinkedYoVariable(YoBoolean variableToLink)
   {
      return new LinkedYoBoolean(variableToLink, this);
   }

   boolean[] getBuffer()
   {
      return buffer;
   }
}
