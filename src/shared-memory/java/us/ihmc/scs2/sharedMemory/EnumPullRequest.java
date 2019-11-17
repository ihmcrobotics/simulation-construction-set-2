package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoEnum;

public class EnumPullRequest<E extends Enum<E>> implements PullRequest<YoEnum<E>>
{
   private final YoEnum<E> variableToUpdate;
   private final int valueToPull;

   public EnumPullRequest(YoEnum<E> variableToUpdate, int valueToPull)
   {
      this.variableToUpdate = variableToUpdate;
      this.valueToPull = valueToPull;
   }

   public void pull()
   {
      variableToUpdate.set(valueToPull);
   }

   public int getValueToPull()
   {
      return valueToPull;
   }
}
