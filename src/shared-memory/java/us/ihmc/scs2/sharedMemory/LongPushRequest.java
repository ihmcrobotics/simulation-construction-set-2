package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoLong;

public class LongPushRequest implements PushRequest<YoLong>
{
   private final long valueToPush;
   private final YoVariableBuffer<YoLong> buffer;

   public LongPushRequest(long valueToPush, YoVariableBuffer<YoLong> buffer)
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
      return valueToPush != buffer.getYoVariable().getLongValue();
   }
}
