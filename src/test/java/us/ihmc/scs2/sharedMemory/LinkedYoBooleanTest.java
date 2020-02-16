package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoVariable;

public class LinkedYoBooleanTest extends LinkedYoVariableTest<YoBoolean>
{
   @Override
   YoBoolean copy(YoBoolean original)
   {
      YoBoolean copy = new YoBoolean(original.getName() + "Copy", new YoVariableRegistry("Dummy"));
      copy.set(original.getValue());
      return copy;
   }

   @Override
   YoBoolean nextYoVariable(Random random)
   {
      return YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
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
         YoBoolean linkedVariable = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBooleanBuffer buffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoBoolean linkedYoBoolean = new LinkedYoBoolean(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoBoolean.getLinkedYoVariable());
         assertTrue(buffer == linkedYoBoolean.getBuffer());
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBoolean linkedVariable = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBooleanBuffer buffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoBoolean linkedYoBoolean = (LinkedYoBoolean) LinkedYoVariable.newLinkedYoVariable(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoBoolean.getLinkedYoVariable());
         assertTrue(buffer == linkedYoBoolean.getBuffer());
      }
   }

   @Test
   public void testToPullRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBoolean linkedVariable = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBooleanBuffer buffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoBoolean linkedYoBoolean = new LinkedYoBoolean(linkedVariable, buffer);

         BooleanPullRequest pullRequest = linkedYoBoolean.toPullRequest();
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
         YoBoolean linkedVariable = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBooleanBuffer buffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoBoolean linkedYoBoolean = new LinkedYoBoolean(linkedVariable, buffer);

         BooleanPushRequest pullRequest = linkedYoBoolean.toPushRequest();
         assertTrue(buffer.getYoVariable() == pullRequest.getVariableToUpdate());
         assertEquals(linkedVariable.getValue(), pullRequest.getValueToPush());

         pullRequest.push();
         assertEquals(linkedVariable.getValue(), buffer.getYoVariable().getValue());
      }
   }
}
