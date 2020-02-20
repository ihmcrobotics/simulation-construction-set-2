package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoVariable;

public class LinkedYoEnumTest<E extends Enum<E>> extends LinkedYoVariableTest<YoEnum<E>>
{
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   YoEnum<E> copy(YoEnum<E> original)
   {
      YoEnum copy = new YoEnum<>(original.getName() + "Copy", new YoVariableRegistry("Dummy"), original.getEnumType(), original.getAllowNullValue());
      copy.set(original.getOrdinal());
      return copy;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   YoEnum<E> nextYoVariable(Random random, int iteration)
   {
      YoEnum next = YoRandomTools.nextYoEnum(new Random(iteration), new YoVariableRegistry("Dummy"));
      YoRandomTools.randomizeYoEnum(random, next);
      return next;
   }

   @BeforeAll
   public static void disableStackTrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   @Test
   public void testConstructor()
   {
      Random random = new Random(76267);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoEnum<?> linkedVariable = YoRandomTools.nextYoEnum(random, new YoVariableRegistry("Dummy"));
         YoEnumBuffer buffer = YoBufferRandomTools.nextYoEnumBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoEnum linkedYoEnum = new LinkedYoEnum(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoEnum.getLinkedYoVariable());
         assertTrue(buffer == linkedYoEnum.getBuffer());
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoEnum linkedVariable = YoRandomTools.nextYoEnum(random, new YoVariableRegistry("Dummy"));
         YoEnumBuffer buffer = YoBufferRandomTools.nextYoEnumBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoEnum linkedYoEnum = (LinkedYoEnum) LinkedYoVariable.newLinkedYoVariable(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoEnum.getLinkedYoVariable());
         assertTrue(buffer == linkedYoEnum.getBuffer());
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testToPullRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoEnum<?> linkedVariable = nextYoVariable(random, i);
         YoEnumBuffer<?> buffer = (YoEnumBuffer<?>) YoBufferRandomTools.nextYoVariableBuffer(random, nextYoVariable(random, i));
         LinkedYoEnum<?> linkedYoEnum = new LinkedYoEnum(linkedVariable, buffer);

         EnumPullRequest<?> pullRequest = linkedYoEnum.toPullRequest();
         assertTrue(linkedVariable == pullRequest.getVariableToUpdate());
         assertEquals(buffer.getYoVariable().getOrdinal(), pullRequest.getValueToPull());

         pullRequest.pull();
         assertEquals(linkedVariable.getOrdinal(), buffer.getYoVariable().getOrdinal());
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Test
   public void testToPushRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoEnum<?> linkedVariable = nextYoVariable(random, i);
         YoEnumBuffer<?> buffer = (YoEnumBuffer<?>) YoBufferRandomTools.nextYoVariableBuffer(random, nextYoVariable(random, i));
         LinkedYoEnum<?> linkedYoEnum = new LinkedYoEnum(linkedVariable, buffer);

         EnumPushRequest<?> pullRequest = linkedYoEnum.toPushRequest();
         assertTrue(buffer.getYoVariable() == pullRequest.getVariableToUpdate());
         assertEquals(linkedVariable.getOrdinal(), pullRequest.getValueToPush());

         pullRequest.push();
         assertEquals(linkedVariable.getOrdinal(), buffer.getYoVariable().getOrdinal());
      }
   }
}
