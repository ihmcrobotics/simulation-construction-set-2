package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class DoublePushRequestTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testPush()
   {
      Random random = new Random(3466);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoDoubleBuffer yoDoubleBuffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));
         double valueToPush = random.nextDouble();

         double currentValue = yoDoubleBuffer.getYoVariable().getValue();
         double[] currentBufferValue = Arrays.copyOf(yoDoubleBuffer.getBuffer(), yoDoubleBuffer.getProperties().getSize());

         DoublePushRequest pushRequest = new DoublePushRequest(valueToPush, yoDoubleBuffer);
         assertEquals(currentValue, yoDoubleBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoDoubleBuffer.getBuffer());

         pushRequest.push();
         assertEquals(valueToPush, yoDoubleBuffer.getYoVariable().getValue());
         assertArrayEquals(currentBufferValue, yoDoubleBuffer.getBuffer());
      }
   }

   @Test
   public void testIsPushNecessary()
   {
      Random random = new Random(89734579);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoDoubleBuffer yoDoubleBuffer = YoBufferRandomTools.nextYoDoubleBuffer(random, new YoVariableRegistry("Dummy"));

         double currentValue = yoDoubleBuffer.getYoVariable().getValue();

         DoublePushRequest pushRequest = new DoublePushRequest(currentValue, yoDoubleBuffer);
         assertFalse(pushRequest.isPushNecessary());

         pushRequest = new DoublePushRequest(currentValue + 1.0, yoDoubleBuffer);
         assertTrue(pushRequest.isPushNecessary());
      }
   }
}
