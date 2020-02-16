package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoBoolean;

public class LinkedYoBoolean extends LinkedYoVariable<YoBoolean>
{
   LinkedYoBoolean(YoBoolean linkedVariable, YoBooleanBuffer buffer)
   {
      super(linkedVariable, buffer);
   }

   public boolean peekCurrentValue(boolean defaultValue)
   {
      BooleanPullRequest pull = (BooleanPullRequest) pullRequest;

      if (pull != null)
         return pull.getValueToPull();
      else
         return defaultValue;
   }

   @Override
   BooleanPullRequest toPullRequest()
   {
      return new BooleanPullRequest(linkedYoVariable, buffer.getYoVariable().getBooleanValue());
   }

   @Override
   protected BooleanPushRequest toPushRequest()
   {
      return new BooleanPushRequest(linkedYoVariable.getBooleanValue(), buffer);
   }

   @Override
   @SuppressWarnings("unchecked")
   public BufferSample<boolean[]> pollRequestedBufferSample()
   {
      return super.pollRequestedBufferSample();
   }

   @Override
   public String toString()
   {
      return String.format("%s: %s", linkedYoVariable.getName(), linkedYoVariable.getValue());
   }
}