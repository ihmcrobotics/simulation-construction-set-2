package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class YoBufferPropertiesTest
{
   private static final int ITERATIONS = 100000;

   @Test
   public void testConstructors()
   {
      Random random = new Random(45346);

      { // Empty constructor
         YoBufferProperties yoBufferProperties = new YoBufferProperties();
         assertEquals(0, yoBufferProperties.getInPoint());
         assertEquals(0, yoBufferProperties.getOutPoint());
         assertEquals(0, yoBufferProperties.getCurrentIndex());
         assertEquals(0, yoBufferProperties.getSize());
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // YoBufferProperties(int index, int size)
         int size = random.nextInt(1280000);
         int index = random.nextInt(size);
         YoBufferProperties yoBufferProperties = new YoBufferProperties(index, size);
         assertEquals(0, yoBufferProperties.getInPoint());
         assertEquals(0, yoBufferProperties.getOutPoint());
         assertEquals(index, yoBufferProperties.getCurrentIndex());
         assertEquals(size, yoBufferProperties.getSize());
      }
   }

   @Test
   public void testIncrementIndex()
   {
      Random random = new Random(436);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test incrementIndex(updateBufferBounds == false)
         int size = random.nextInt(1000000);
         int inPoint = random.nextInt(size);
         int index = random.nextInt(size);
         int outPoint = random.nextInt(size);

         YoBufferProperties properties = new YoBufferProperties(index, size);
         properties.setInPointIndex(inPoint);
         properties.setOutPointIndex(outPoint);

         int newIndex = properties.incrementIndex(false);
         assertEquals(newIndex, properties.getCurrentIndex());
         assertEquals(size, properties.getSize());
         assertEquals(inPoint, properties.getInPoint());
         assertEquals(outPoint, properties.getOutPoint());

         if (properties.isIndexBetweenBounds(index) && properties.isIndexBetweenBounds(index + 1))
            assertEquals(index + 1, newIndex);
         else
            assertEquals(inPoint, newIndex);

         index = outPoint;
         properties.setCurrentIndex(index);
         newIndex = properties.incrementIndex(false);
         assertEquals(inPoint, newIndex);

         index = size - 1;
         properties.setCurrentIndex(index);
         newIndex = properties.incrementIndex(false);
         if (properties.isIndexBetweenBounds(size - 1) && properties.isIndexBetweenBounds(0))
            assertEquals(0, newIndex);
         else
            assertEquals(inPoint, newIndex);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test incrementIndex(updateBufferBounds == true)
         int size = random.nextInt(1000000);
         int inPoint = random.nextInt(size);
         int outPoint = random.nextInt(size);
         int index = random.nextInt(size);
         String messagePrefix = "iteration " + i + ", index " + index + ", size " + size + ", in " + inPoint + ", out " + outPoint;

         YoBufferProperties properties = new YoBufferProperties(index, size);
         properties.setInPointIndex(inPoint);
         properties.setOutPointIndex(outPoint);

         int newIndex = properties.incrementIndex(true);
         assertEquals(newIndex, properties.getCurrentIndex(), messagePrefix);
         assertEquals(size, properties.getSize(), messagePrefix);
         assertEquals(newIndex, properties.getOutPoint());
         assertEquals(inPoint, properties.getInPoint(), messagePrefix);

         if (index < size -1)
            assertEquals(index + 1, newIndex, messagePrefix);
         else
            assertEquals(0, newIndex, messagePrefix);
      }
   }
}
