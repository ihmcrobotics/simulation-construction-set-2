package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoBoolean;

public class BooleanPushRequest implements PushRequest<YoBoolean>
{
   private final boolean valueToPush;
   private final YoVariableBuffer<YoBoolean> buffer;

   public BooleanPushRequest(boolean valueToPush, YoVariableBuffer<YoBoolean> buffer)
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
      return valueToPush != buffer.getYoVariable().getValue();
   }
}
