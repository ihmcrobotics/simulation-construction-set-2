package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class LongPushRequestTest
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
         YoLongBuffer yoLongBuffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));
         long valueToPush = random.nextLong();

         long currentValue = yoLongBuffer.getYoVariable().getValue();
         long[] currentBufferValue = Arrays.copyOf(yoLongBuffer.getBuffer(), yoLongBuffer.getProperties().getSize());

         LongPushRequest pushRequest = new LongPushRequest(valueToPush, yoLongBuffer);
         assertEquals(currentValue, yoLongBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoLongBuffer.getBuffer());

         pushRequest.push();
         assertEquals(valueToPush, yoLongBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoLongBuffer.getBuffer());
      }
   }

   @Test
   public void testIsPushNecessary()
   {
      Random random = new Random(89734579);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLongBuffer yoLongBuffer = YoBufferRandomTools.nextYoLongBuffer(random, new YoVariableRegistry("Dummy"));

         long currentValue = yoLongBuffer.getYoVariable().getValue();

         LongPushRequest pushRequest = new LongPushRequest(currentValue, yoLongBuffer);
         assertFalse(pushRequest.isPushNecessary());

         pushRequest = new LongPushRequest(currentValue + 1, yoLongBuffer);
         assertTrue(pushRequest.isPushNecessary());
      }
   }
}
