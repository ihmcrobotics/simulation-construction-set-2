package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoEnum;

public class EnumPushRequest<E extends Enum<E>> implements PushRequest<YoEnum<E>>
{
   private final int valueToPush;
   private final YoEnum<E> variableToUpdate;

   public EnumPushRequest(int valueToPush, YoEnum<E> variableToUpdate)
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
      return valueToPush != variableToUpdate.getOrdinal();
   }
}
