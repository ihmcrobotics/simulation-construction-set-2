package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;

public class YoBooleanBufferTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testConstructors()
   {
      Random random = new Random(467);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBoolean yoBoolean = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties yoBufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoBooleanBuffer yoBooleanBuffer = new YoBooleanBuffer(yoBoolean, yoBufferProperties);
         assertTrue(yoBoolean == yoBooleanBuffer.getYoVariable());
         assertTrue(yoBufferProperties == yoBooleanBuffer.getProperties());
         assertEquals(yoBufferProperties.getSize(), yoBooleanBuffer.getBuffer().length);

         for (int j = 0; j < yoBufferProperties.getSize(); j++)
            assertEquals(false, yoBooleanBuffer.getBuffer()[j]);
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBoolean yoBoolean = YoRandomTools.nextYoBoolean(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties yoBufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoBooleanBuffer yoBooleanBuffer = (YoBooleanBuffer) YoVariableBuffer.newYoVariableBuffer(yoBoolean, yoBufferProperties);
         assertTrue(yoBoolean == yoBooleanBuffer.getYoVariable());
         assertTrue(yoBufferProperties == yoBooleanBuffer.getProperties());
         assertEquals(yoBufferProperties.getSize(), yoBooleanBuffer.getBuffer().length);

         for (int j = 0; j < yoBufferProperties.getSize(); j++)
            assertEquals(false, yoBooleanBuffer.getBuffer()[j]);
      }
   }

   @Test
   public void testResizeBuffer()
   {
      Random random = new Random(8967254);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         YoBufferProperties originalBufferProperties = new YoBufferProperties(yoBooleanBuffer.getProperties());
         int from = random.nextInt(yoBooleanBuffer.getProperties().getSize());
         int newLength = random.nextInt(yoBooleanBuffer.getProperties().getSize());
         boolean[] expectedBuffer = BufferTools.ringArrayCopy(yoBooleanBuffer.getBuffer(), from, newLength);

         yoBooleanBuffer.resizeBuffer(from, newLength);
         assertArrayEquals(expectedBuffer, yoBooleanBuffer.getBuffer());
         assertEquals(originalBufferProperties, yoBooleanBuffer.getProperties());

         boolean[] buffer = yoBooleanBuffer.getBuffer();
         yoBooleanBuffer.resizeBuffer(0, buffer.length);
         assertTrue(buffer == yoBooleanBuffer.getBuffer());
      }
   }

   @Test
   public void testWriteBuffer()
   {
      Random random = new Random(867324);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         YoBoolean yoBoolean = yoBooleanBuffer.getYoVariable();

         int currentIndex = yoBooleanBuffer.getProperties().getCurrentIndex();
         yoBoolean.set(false);
         yoBooleanBuffer.writeBuffer();
         assertEquals(false, yoBoolean.getValue());
         assertEquals(false, yoBooleanBuffer.getBuffer()[currentIndex]);

         yoBoolean.set(true);
         yoBooleanBuffer.writeBuffer();
         assertEquals(true, yoBoolean.getValue());
         assertEquals(true, yoBooleanBuffer.getBuffer()[currentIndex]);
      }
   }

   @Test
   public void testReadBuffer()
   {
      Random random = new Random(867324);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         YoBoolean yoBoolean = yoBooleanBuffer.getYoVariable();

         int currentIndex = yoBooleanBuffer.getProperties().getCurrentIndex();
         yoBooleanBuffer.getBuffer()[currentIndex] = false;
         yoBooleanBuffer.readBuffer();
         assertEquals(false, yoBoolean.getValue());
         assertEquals(false, yoBooleanBuffer.getBuffer()[currentIndex]);

         yoBooleanBuffer.getBuffer()[currentIndex] = true;
         yoBooleanBuffer.readBuffer();
         assertEquals(true, yoBoolean.getValue());
         assertEquals(true, yoBooleanBuffer.getBuffer()[currentIndex]);
      }
   }

   @Test
   public void testCopy()
   {
      Random random = new Random(43566);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         int from = random.nextInt(yoBooleanBuffer.getProperties().getSize());
         int length = random.nextInt(yoBooleanBuffer.getProperties().getSize());

         boolean[] expectedCopy = BufferTools.ringArrayCopy(yoBooleanBuffer.getBuffer(), from, length);
         BufferSample<boolean[]> actualCopy = yoBooleanBuffer.copy(from, length);

         assertEquals(from, actualCopy.getFrom());
         assertEquals(length, actualCopy.getSampleLength());
         assertEquals(yoBooleanBuffer.getProperties().getSize(), actualCopy.getBufferSize());
         int to = from + length;
         if (to > yoBooleanBuffer.getProperties().getSize())
            to -= yoBooleanBuffer.getProperties().getSize();
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
         YoBooleanBuffer yoBooleanBuffer = YoBufferRandomTools.nextYoBooleanBuffer(random, new YoVariableRegistry("Dummy"));
         YoBoolean linkedBoolean = new YoBoolean("linked", new YoVariableRegistry("Dummy"));
         LinkedYoBoolean linkedYoVariable = yoBooleanBuffer.newLinkedYoVariable(linkedBoolean);
         assertTrue(linkedBoolean == linkedYoVariable.getLinkedYoVariable());
         assertTrue(yoBooleanBuffer == linkedYoVariable.getBuffer());
      }
   }
}
