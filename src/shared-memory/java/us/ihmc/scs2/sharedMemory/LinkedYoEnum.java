package us.ihmc.scs2.sharedMemory;

import us.ihmc.yoVariables.variable.YoEnum;

public class LinkedYoEnum<E extends Enum<E>> extends LinkedYoVariable<YoEnum<E>>
{
   LinkedYoEnum(YoEnum<E> linkedVariable, YoEnumBuffer<E> buffer)
   {
      super(linkedVariable, buffer);
   }

   public E peekCurrentValue(E defaultValue)
   {
      EnumPullRequest<E> pull = (EnumPullRequest<E>) pullRequest;

      if (pull != null)
      {
         int ordinal = pull.getValueToPull();
         return ordinal < 0 ? null : linkedYoVariable.getEnumValues()[ordinal];
      }
      else
      {
         return defaultValue;
      }
   }

   @Override
   EnumPullRequest<E> toPullRequest()
   {
      return new EnumPullRequest<>(linkedYoVariable, buffer.getYoVariable().getOrdinal());
   }

   @Override
   protected EnumPushRequest<E> toPushRequest()
   {
      return new EnumPushRequest<>(linkedYoVariable.getOrdinal(), buffer);
   }

   @Override
   @SuppressWarnings("unchecked")
   public BufferSample<byte[]> pollRequestedBufferSample()
   {
      return super.pollRequestedBufferSample();
   }

   @Override
   public String toString()
   {
      return String.format("%s: %s", linkedYoVariable.getName(), linkedYoVariable.getValue());
   }
}
