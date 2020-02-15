package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerBufferTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testConstructors()
   {
      Random random = new Random(467);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoInteger yoInteger = YoRandomTools.nextYoInteger(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties yoBufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoIntegerBuffer yoIntegerBuffer = new YoIntegerBuffer(yoInteger, yoBufferProperties);
         assertTrue(yoInteger == yoIntegerBuffer.getYoVariable());
         assertTrue(yoBufferProperties == yoIntegerBuffer.getProperties());
         assertEquals(yoBufferProperties.getSize(), yoIntegerBuffer.getBuffer().length);

         for (int j = 0; j < yoBufferProperties.getSize(); j++)
            assertEquals(0, yoIntegerBuffer.getBuffer()[j]);
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoInteger yoInteger = YoRandomTools.nextYoInteger(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties yoBufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoIntegerBuffer yoIntegerBuffer = (YoIntegerBuffer) YoVariableBuffer.newYoVariableBuffer(yoInteger, yoBufferProperties);
         assertTrue(yoInteger == yoIntegerBuffer.getYoVariable());
         assertTrue(yoBufferProperties == yoIntegerBuffer.getProperties());
         assertEquals(yoBufferProperties.getSize(), yoIntegerBuffer.getBuffer().length);

         for (int j = 0; j < yoBufferProperties.getSize(); j++)
            assertEquals(0, yoIntegerBuffer.getBuffer()[j]);
      }
   }

   @Test
   public void testResizeBuffer()
   {
      Random random = new Random(8967254);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties originalBufferProperties = new YoBufferProperties(yoIntegerBuffer.getProperties());
         int from = random.nextInt(yoIntegerBuffer.getProperties().getSize());
         int newLength = random.nextInt(yoIntegerBuffer.getProperties().getSize());
         int[] expectedBuffer = BufferTools.ringArrayCopy(yoIntegerBuffer.getBuffer(), from, newLength);

         yoIntegerBuffer.resizeBuffer(from, newLength);
         assertArrayEquals(expectedBuffer, yoIntegerBuffer.getBuffer());
         assertEquals(originalBufferProperties, yoIntegerBuffer.getProperties());

         int[] buffer = yoIntegerBuffer.getBuffer();
         yoIntegerBuffer.resizeBuffer(0, buffer.length);
         assertTrue(buffer == yoIntegerBuffer.getBuffer());
      }
   }

   @Test
   public void testWriteBuffer()
   {
      Random random = new Random(867324);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         YoInteger yoInteger = yoIntegerBuffer.getYoVariable();

         int currentIndex = yoIntegerBuffer.getProperties().getCurrentIndex();
         for (int j = 0; j < 10; j++)
         {
            int newValue = RandomNumbers.nextInt(random, -100000, 100000);
            yoInteger.set(newValue);
            yoIntegerBuffer.writeBuffer();
            assertEquals(newValue, yoInteger.getValue());
            assertEquals(newValue, yoIntegerBuffer.getBuffer()[currentIndex]);
         }
      }
   }

   @Test
   public void testReadBuffer()
   {
      Random random = new Random(867324);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         YoInteger yoInteger = yoIntegerBuffer.getYoVariable();

         int currentIndex = yoIntegerBuffer.getProperties().getCurrentIndex();
         for (int j = 0; j < 10; j++)
         {
            int newValue = RandomNumbers.nextInt(random, -100000, 100000);
            yoIntegerBuffer.getBuffer()[currentIndex] = newValue;
            yoIntegerBuffer.readBuffer();
            assertEquals(newValue, yoInteger.getValue());
            assertEquals(newValue, yoIntegerBuffer.getBuffer()[currentIndex]);
         }
      }
   }

   @Test
   public void testCopy()
   {
      Random random = new Random(43566);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         int from = random.nextInt(yoIntegerBuffer.getProperties().getSize());
         int length = random.nextInt(yoIntegerBuffer.getProperties().getSize());

         int[] expectedCopy = BufferTools.ringArrayCopy(yoIntegerBuffer.getBuffer(), from, length);
         BufferSample<int[]> actualCopy = yoIntegerBuffer.copy(from, length);

         assertEquals(from, actualCopy.getFrom());
         assertEquals(length, actualCopy.getSampleLength());
         assertEquals(yoIntegerBuffer.getProperties().getSize(), actualCopy.getBufferSize());
         int to = from + length;
         if (to >= yoIntegerBuffer.getProperties().getSize())
            to -= yoIntegerBuffer.getProperties().getSize();
         assertEquals(to, actualCopy.getTo());
         assertArrayEquals(expectedCopy, actualCopy.getSample());
      }
   }

   @Test
   public void testNewLinkedYoVariable()
   {
      Random random = new Random(87324);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoIntegerBuffer yoIntegerBuffer = YoBufferRandomTools.nextYoIntegerBuffer(random, new YoVariableRegistry("Dummy"));
         YoInteger linkedInteger = new YoInteger("linked", new YoVariableRegistry("Dummy"));
         LinkedYoInteger linkedYoVariable = yoIntegerBuffer.newLinkedYoVariable(linkedInteger);
         assertTrue(linkedInteger == linkedYoVariable.getLinkedYoVariable());
         assertTrue(yoIntegerBuffer == linkedYoVariable.getBuffer());
      }
   }
}
