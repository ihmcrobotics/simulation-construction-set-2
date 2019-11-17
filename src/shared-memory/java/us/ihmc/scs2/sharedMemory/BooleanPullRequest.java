package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoBoolean;

public class BooleanPullRequest implements PullRequest<YoBoolean>
{
   private final YoBoolean variableToUpdate;
   private final boolean valueToPull;

   public BooleanPullRequest(YoBoolean variableToUpdate, boolean valueToPull)
   {
      this.variableToUpdate = variableToUpdate;
      this.valueToPull = valueToPull;
   }

   public void pull()
   {
      variableToUpdate.set(valueToPull);
   }

   public boolean getValueToPull()
   {
      return valueToPull;
   }
}
