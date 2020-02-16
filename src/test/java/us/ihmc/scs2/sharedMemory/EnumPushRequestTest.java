package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
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
         YoEnumBuffer<?> yoEnumBuffer = YoBufferRandomTools.nextYoEnumBuffer(random, new YoVariableRegistry("Dummy"));
         int valueToPush = YoRandomTools.nextOrdinal(random, yoEnumBuffer.getYoVariable());

         int currentValue = yoEnumBuffer.getYoVariable().getOrdinal();
         byte[] currentBufferValue = Arrays.copyOf(yoEnumBuffer.getBuffer(), yoEnumBuffer.getProperties().getSize());

         EnumPushRequest<?> pushRequest = new EnumPushRequest<>(valueToPush, yoEnumBuffer);
         assertEquals(currentValue, yoEnumBuffer.getYoVariable().getOrdinal());
         assertArrayEquals(currentBufferValue, yoEnumBuffer.getBuffer());

         pushRequest.push();
         assertEquals(valueToPush, yoEnumBuffer.getYoVariable().getOrdinal());
         assertArrayEquals(currentBufferValue, yoEnumBuffer.getBuffer());
      }
   }

   @Test
   public void testIsPushNecessary()
   {
      Random random = new Random(89734579);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoEnumBuffer<?> yoEnumBuffer = YoBufferRandomTools.nextYoEnumBuffer(random, new YoVariableRegistry("Dummy"));

         int currentValue = yoEnumBuffer.getYoVariable().getOrdinal();

         EnumPushRequest<?> pushRequest = new EnumPushRequest<>(currentValue, yoEnumBuffer);
         assertFalse(pushRequest.isPushNecessary());

         pushRequest = new EnumPushRequest<>(currentValue + 1, yoEnumBuffer);
         assertTrue(pushRequest.isPushNecessary());
      }
   }
}
