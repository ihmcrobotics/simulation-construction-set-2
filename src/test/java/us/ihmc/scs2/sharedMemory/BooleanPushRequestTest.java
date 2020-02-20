package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
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
         YoBoolean bufferYoBoolean = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         boolean valueToPush = random.nextBoolean();

         boolean currentValue = bufferYoBoolean.getValue();

         BooleanPushRequest pushRequest = new BooleanPushRequest(valueToPush, bufferYoBoolean);
         assertEquals(currentValue, bufferYoBoolean.getValue());

         pushRequest.push();
         assertEquals(valueToPush, bufferYoBoolean.getValue());

         assertFalse(pushRequest.push());

         pushRequest = new BooleanPushRequest(!valueToPush, bufferYoBoolean);
         assertTrue(pushRequest.push());
      }
   }
}
