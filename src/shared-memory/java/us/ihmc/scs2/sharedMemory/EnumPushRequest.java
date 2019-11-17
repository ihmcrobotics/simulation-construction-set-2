package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoEnum;

public class EnumPushRequest<E extends Enum<E>> implements PushRequest<YoEnum<E>>
{
   private final int valueToPush;
   private final YoVariableBuffer<YoEnum<E>> buffer;

   public EnumPushRequest(int valueToPush, YoVariableBuffer<YoEnum<E>> buffer)
   {
      this.valueToPush = valueToPush;
      this.buffer = buffer;
   }

   @Override
   public void push()
   {
      buffer.getYoVariable().set(valueToPush);
   }

   @Override
   public boolean isPushNecessary()
   {
      return valueToPush != buffer.getYoVariable().getOrdinal();
   }
}
