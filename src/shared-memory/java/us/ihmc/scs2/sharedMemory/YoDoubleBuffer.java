package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoDoubleBuffer extends YoVariableBuffer<YoDouble>
{
   private double[] buffer;

   public YoDoubleBuffer(YoDouble yoDouble, YoBufferPropertiesReadOnly properties)
   {
      super(yoDouble, properties);
      buffer = new double[properties.getSize()];
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
      return Double.doubleToLongBits(buffer[index]);
   }

   @Override
   public BufferSample<double[]> copy(int from, int length)
   {
      return new BufferSample<>(from, buffer.length, BufferTools.ringArrayCopy(buffer, from, length), length);
   }

   @Override
   LinkedYoDouble newLinkedYoVariable(YoDouble variableToLink)
   {
      return new LinkedYoDouble(variableToLink, this);
   }

   double[] getBuffer()
   {
      return buffer;
   }
}
