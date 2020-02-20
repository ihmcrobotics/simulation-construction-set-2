package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoVariable;

public class LinkedYoDoubleTest extends LinkedYoVariableTest<YoDouble>
{
   @Override
   YoDouble copy(YoDouble original)
   {
      YoDouble copy = new YoDouble(original.getName() + "Copy", new YoVariableRegistry("Dummy"));
      copy.set(original.getValue());
      return copy;
   }

   @Override
   YoDouble nextYoVariable(Random random, int iteration)
   {
      return YoRandomTools.nextYoDouble(random, new YoVariableRegistry("Dummy"));
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
         YoDouble linkedVariable = YoRandomTools.nextYoDouble(random, new YoVariableRegistry("Dummy"));
         YoDoubleBuffer buffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoDouble linkedYoDouble = new LinkedYoDouble(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoDouble.getLinkedYoVariable());
         assertTrue(buffer == linkedYoDouble.getBuffer());
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoDouble linkedVariable = YoRandomTools.nextYoDouble(random, new YoVariableRegistry("Dummy"));
         YoDoubleBuffer buffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoDouble linkedYoDouble = (LinkedYoDouble) LinkedYoVariable.newLinkedYoVariable(linkedVariable, buffer);

         assertTrue(linkedVariable == linkedYoDouble.getLinkedYoVariable());
         assertTrue(buffer == linkedYoDouble.getBuffer());
      }
   }

   @Test
   public void testToPullRequest()
   {
      Random random = new Random(349785);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoDouble linkedVariable = YoRandomTools.nextYoDouble(random, new YoVariableRegistry("Dummy"));
         YoDoubleBuffer buffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoDouble linkedYoDouble = new LinkedYoDouble(linkedVariable, buffer);

         DoublePullRequest pullRequest = linkedYoDouble.toPullRequest();
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
         YoDouble linkedVariable = YoRandomTools.nextYoDouble(random, new YoVariableRegistry("Dummy"));
         YoDoubleBuffer buffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));
         LinkedYoDouble linkedYoDouble = new LinkedYoDouble(linkedVariable, buffer);

         DoublePushRequest pullRequest = linkedYoDouble.toPushRequest();
         assertTrue(buffer.getYoVariable() == pullRequest.getVariableToUpdate());
         assertEquals(linkedVariable.getValue(), pullRequest.getValueToPush());

         pullRequest.push();
         assertEquals(linkedVariable.getValue(), buffer.getYoVariable().getValue());
      }
   }
}
