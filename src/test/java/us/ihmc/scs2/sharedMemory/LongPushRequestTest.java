package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoLong;

public class LongPushRequestTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testPush()
   {
      Random random = new Random(3466);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoLong bufferYoLong = YoRandomTools.nextYoLong(random, new YoRegistry("Dummy"));
         long valueToPush = random.nextLong();

         long currentValue = bufferYoLong.getValue();

         LongPushRequest pushRequest = new LongPushRequest(valueToPush, bufferYoLong);
         assertEquals(currentValue, bufferYoLong.getValue());

         pushRequest.push();
         assertEquals(valueToPush, bufferYoLong.getValue());

         assertFalse(pushRequest.push());

         pushRequest = new LongPushRequest(valueToPush + 1, bufferYoLong);
         assertTrue(pushRequest.push());
      }
   }
}
