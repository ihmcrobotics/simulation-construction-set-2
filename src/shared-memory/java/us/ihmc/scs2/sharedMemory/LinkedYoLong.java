package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoLong;

public class LinkedYoLong extends LinkedYoVariable<YoLong>
{
   LinkedYoLong(YoLong linkedVariable, YoLongBuffer buffer)
   {
      super(linkedVariable, buffer);
   }

   public long peekCurrentValue(long defaultValue)
   {
      LongPullRequest pull = (LongPullRequest) pullRequest;

      if (pull != null)
         return pull.getValueToPull();
      else
         return defaultValue;
   }

   @Override
   LongPullRequest toPullRequest()
   {
      return new LongPullRequest(linkedYoVariable, buffer.getYoVariable().getLongValue());
   }

   @Override
   protected LongPushRequest toPushRequest()
   {
      return new LongPushRequest(linkedYoVariable.getLongValue(), buffer);
   }

   @Override
   @SuppressWarnings("unchecked")
   public BufferSample<long[]> pollRequestedBufferSample()
   {
      return super.pollRequestedBufferSample();
   }

   @Override
   public String toString()
   {
      return String.format("%s: %s", linkedYoVariable.getName(), linkedYoVariable.getValue());
   }
}
