package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoInteger;
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
         YoInteger bufferYoInteger = YoRandomTools.nextYoInteger(random, new YoVariableRegistry("Dummy"));
         int valueToPush = random.nextInt();

         int currentValue = bufferYoInteger.getValue();

         IntegerPushRequest pushRequest = new IntegerPushRequest(valueToPush, bufferYoInteger);
         assertEquals(currentValue, bufferYoInteger.getValue());

         pushRequest.push();
         assertEquals(valueToPush, bufferYoInteger.getValue());

         assertFalse(pushRequest.push());

         pushRequest = new IntegerPushRequest(valueToPush + 1, bufferYoInteger);
         assertTrue(pushRequest.push());
      }
   }
}
