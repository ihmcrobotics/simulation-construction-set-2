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
   public BufferSample<boolean[]> copy(int from, int length)
   {
      return new BufferSample<>(from, properties.getSize(), BufferTools.ringArrayCopy(buffer, from, length), length);
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
