package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoDouble;

public class DoublePullRequest implements PullRequest<YoDouble>
{
   private final YoDouble variableToUpdate;
   private final double valueToPull;

   public DoublePullRequest(YoDouble variableToUpdate, double valueToPull)
   {
      this.variableToUpdate = variableToUpdate;
      this.valueToPull = valueToPull;
   }

   public void pull()
   {
      variableToUpdate.set(valueToPull);
   }

   public double getValueToPull()
   {
      return valueToPull;
   }
}
