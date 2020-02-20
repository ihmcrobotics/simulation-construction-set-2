package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoVariable;

public class EnumPushRequestTest
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
         YoEnum<?> bufferYoEnum = YoRandomTools.nextYoEnum(random, new YoVariableRegistry("Dummy"));
         int valueToPush = YoRandomTools.nextOrdinal(random, bufferYoEnum);

         int currentValue = bufferYoEnum.getOrdinal();

         EnumPushRequest<?> pushRequest = new EnumPushRequest<>(valueToPush, bufferYoEnum);
         assertEquals(currentValue, bufferYoEnum.getOrdinal());

         pushRequest.push();
         assertEquals(valueToPush, bufferYoEnum.getOrdinal());

         assertFalse(pushRequest.push());

         if (bufferYoEnum.getEnumValuesAsString().length > 1)
         {
            while (valueToPush == bufferYoEnum.getOrdinal())
               valueToPush = YoRandomTools.nextOrdinal(random, bufferYoEnum);
            pushRequest = new EnumPushRequest<>(valueToPush, bufferYoEnum);
            assertTrue(pushRequest.push());
         }
      }
   }
}
