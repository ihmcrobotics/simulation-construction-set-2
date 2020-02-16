package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class BooleanPushRequestTest
{
   private static final int ITERATIONS = 1000;

   @BeforeAll
   public static void disableStackTrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @Test
   public void testPush()
   {
      Random random = new Random(3466);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         boolean valueToPush = random.nextBoolean();

         boolean currentValue = yoBooleanBuffer.getYoVariable().getValue();
         boolean[] currentBufferValue = Arrays.copyOf(yoBooleanBuffer.getBuffer(), yoBooleanBuffer.getProperties().getSize());

         BooleanPushRequest pushRequest = new BooleanPushRequest(valueToPush, yoBooleanBuffer.getYoVariable());
         assertEquals(currentValue, yoBooleanBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoBooleanBuffer.getBuffer());

         pushRequest.push();
         assertEquals(valueToPush, yoBooleanBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoBooleanBuffer.getBuffer());
      }
   }

   @Test
   public void testIsPushNecessary()
   {
      Random random = new Random(89734579);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));

         boolean currentValue = yoBooleanBuffer.getYoVariable().getValue();

         BooleanPushRequest pushRequest = new BooleanPushRequest(currentValue, yoBooleanBuffer.getYoVariable());
         assertFalse(pushRequest.isPushNecessary());

         pushRequest = new BooleanPushRequest(!currentValue, yoBooleanBuffer.getYoVariable());
         assertTrue(pushRequest.isPushNecessary());
      }
   }
}
