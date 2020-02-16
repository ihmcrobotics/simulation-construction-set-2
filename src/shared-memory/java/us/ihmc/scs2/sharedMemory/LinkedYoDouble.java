package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoDouble;

public class LinkedYoDouble extends LinkedYoVariable<YoDouble>
{
   LinkedYoDouble(YoDouble linkedVariable, YoDoubleBuffer buffer)
   {
      super(linkedVariable, buffer);
   }

   public double peekCurrentValue(double defaultValue)
   {
      DoublePullRequest pull = (DoublePullRequest) pullRequest;

      if (pull != null)
         return pull.getValueToPull();
      else
         return defaultValue;
   }

   @Override
   DoublePullRequest toPullRequest()
   {
      return new DoublePullRequest(linkedYoVariable, buffer.getYoVariable().getValue());
   }

   @Override
   protected DoublePushRequest toPushRequest()
   {
      return new DoublePushRequest(linkedYoVariable.getValue(), buffer);
   }

   @Override
   @SuppressWarnings("unchecked")
   public BufferSample<double[]> pollRequestedBufferSample()
   {
      return super.pollRequestedBufferSample();
   }

   @Override
   public String toString()
   {
      return String.format("%s: %s", linkedYoVariable.getName(), linkedYoVariable.getValue());
   }
}
