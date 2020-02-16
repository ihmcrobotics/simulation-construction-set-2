package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoBoolean;

public class BooleanPushRequest implements PushRequest<YoBoolean>
{
   private final boolean valueToPush;
   private final YoBoolean variableToUpdate;

   public BooleanPushRequest(boolean valueToPush, YoBoolean variableToUpdate)
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
