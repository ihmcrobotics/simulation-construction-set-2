package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoLong;

public class LongPushRequest implements PushRequest<YoLong>
{
   private final long valueToPush;
   private final YoLong variableToUpdate;

   public LongPushRequest(long valueToPush, YoLong variableToUpdate)
   {
      this.valueToPush = valueToPush;
      this.variableToUpdate = variableToUpdate;
   }

   @Override
   public void push()
   {
      variableToUpdate.set(valueToPush);
   }

   @Override
   public boolean isPushNecessary()
   {
      return valueToPush != variableToUpdate.getValue();
   }
}
