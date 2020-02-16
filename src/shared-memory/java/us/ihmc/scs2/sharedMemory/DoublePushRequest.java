package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoDouble;

public class DoublePushRequest implements PushRequest<YoDouble>
{
   private final double valueToPush;
   private final YoVariableBuffer<YoDouble> buffer;

   public DoublePushRequest(double valueToPush, YoVariableBuffer<YoDouble> buffer)
   {
      this.valueToPush = valueToPush;
      this.buffer = buffer;
   }

   @Override
   public void push()
   {
      buffer.getYoVariable().set(valueToPush);
   }

   @Override
   public boolean isPushNecessary()
   {
      return valueToPush != buffer.getYoVariable().getValue();
   }
}
