package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class IntegerPushRequestTest
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
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         int valueToPush = random.nextInt();

         int currentValue = yoIntegerBuffer.getYoVariable().getValue();
         int[] currentBufferValue = Arrays.copyOf(yoIntegerBuffer.getBuffer(), yoIntegerBuffer.getProperties().getSize());

         IntegerPushRequest pushRequest = new IntegerPushRequest(valueToPush, yoIntegerBuffer);
         assertEquals(currentValue, yoIntegerBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoIntegerBuffer.getBuffer());

         pushRequest.push();
         assertEquals(valueToPush, yoIntegerBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoIntegerBuffer.getBuffer());
      }
   }

   @Test
   public void testIsPushNecessary()
   {
      Random random = new Random(89734579);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));

         int currentValue = yoIntegerBuffer.getYoVariable().getValue();

         IntegerPushRequest pushRequest = new IntegerPushRequest(currentValue, yoIntegerBuffer);
         assertFalse(pushRequest.isPushNecessary());

         pushRequest = new IntegerPushRequest(currentValue + 1, yoIntegerBuffer);
         assertTrue(pushRequest.isPushNecessary());
      }
   }
}
