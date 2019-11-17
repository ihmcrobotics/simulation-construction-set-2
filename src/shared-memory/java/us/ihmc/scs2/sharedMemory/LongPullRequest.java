package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoLong;

public class LongPullRequest implements PullRequest<YoLong>
{
   private final YoLong variableToUpdate;
   private final long valueToPull;

   public LongPullRequest(YoLong variableToUpdate, long valueToPull)
   {
      this.variableToUpdate = variableToUpdate;
      this.valueToPull = valueToPull;
   }

   public void pull()
   {
      variableToUpdate.setValueFromLongBits(valueToPull);
   }

   public long getValueToPull()
   {
      return valueToPull;
   }
}
