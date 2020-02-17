package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class LinkedYoLongTest extends LinkedYoVariableTest<YoLong>
{
   @Override
   YoLong copy(YoLong original)
   {
      YoLong copy = new YoLong(original.getName() + "Copy", new YoVariableRegistry("Dummy"));
      copy.set(original.getValue());
      return copy;
   }

   @Override
   YoLong nextYoVariable(Random random, int iteration)
   {
      return YoRandomTools.nextYoLong(random, new YoVariableRegistry("Dummy"));
   }

   @BeforeAll
   public static void disableStackTrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @Test
   public void testConstructor()
   {
      Random random = new Random(76267);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLong linkedVariable = YoRandomTools.nextYoLong(random, new YoVariableRegistry("Dummy"));
         YoLongBuffer buffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoLong linkedYoLong = new LinkedYoLong(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoLong.getLinkedYoVariable());
         assertTrue(buffer == linkedYoLong.getBuffer());
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLong linkedVariable = YoRandomTools.nextYoLong(random, new YoVariableRegistry("Dummy"));
         YoLongBuffer buffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoLong linkedYoLong = (LinkedYoLong) LinkedYoVariable.newLinkedYoVariable(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoLong.getLinkedYoVariable());
         assertTrue(buffer == linkedYoLong.getBuffer());
      }
   }

   @Test
   public void testToPullRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLong linkedVariable = YoRandomTools.nextYoLong(random, new YoVariableRegistry("Dummy"));
         YoLongBuffer buffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoLong linkedYoLong = new LinkedYoLong(linkedVariable, buffer);

         LongPullRequest pullRequest = linkedYoLong.toPullRequest();
         assertTrue(linkedVariable == pullRequest.getVariableToUpdate());
         assertEquals(buffer.getYoVariable().getValue(), pullRequest.getValueToPull());

         pullRequest.pull();
         assertEquals(linkedVariable.getValue(), buffer.getYoVariable().getValue());
      }
   }

   @Test
   public void testToPushRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLong linkedVariable = YoRandomTools.nextYoLong(random, new YoVariableRegistry("Dummy"));
         YoLongBuffer buffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoLong linkedYoLong = new LinkedYoLong(linkedVariable, buffer);

         LongPushRequest pullRequest = linkedYoLong.toPushRequest();
         assertTrue(buffer.getYoVariable() == pullRequest.getVariableToUpdate());
         assertEquals(linkedVariable.getValue(), pullRequest.getValueToPush());

         pullRequest.push();
         assertEquals(linkedVariable.getValue(), buffer.getYoVariable().getValue());
      }
   }
}
