package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoInteger;

public class IntegerPushRequest implements PushRequest<YoInteger>
{
   private final int valueToPush;
   private final YoVariableBuffer<YoInteger> buffer;

   public IntegerPushRequest(int valueToPush, YoVariableBuffer<YoInteger> buffer)
   {
      this.valueToPush = valueToPush;
      this.buffer = buffer;
   }

   @Override
   public void push()
   {
      buffer.getYoVariable().setValueFromLongBits(valueToPush);
   }

   @Override
   public boolean isPushNecessary()
   {
      return valueToPush != buffer.getYoVariable().getValue();
   }
}
