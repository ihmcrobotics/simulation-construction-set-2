package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.yoVariables.variable.*;

public class YoSharedBufferTest
{
   private static final int ITERATIONS = 200;

   @Test
   public void testCropBuffer()
   {
      Random random = new Random(6234);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         YoBufferPropertiesReadOnly newProperties = yoSharedBuffer.getProperties();
         YoBufferPropertiesReadOnly initialProperties = newProperties.copy();

         CropBufferRequest cropBufferRequest = new CropBufferRequest(random.nextInt(initialProperties.getSize()), random.nextInt(initialProperties.getSize()));

         yoSharedBuffer.cropBuffer(cropBufferRequest);
         assertEquals(cropBufferRequest.getCroppedSize(initialProperties.getSize()), newProperties.getSize());
         assertEquals(0, newProperties.getInPoint());
         assertEquals(newProperties.getSize() - 1, newProperties.getOutPoint());
         assertEquals(newProperties.getInPoint(), newProperties.getCurrentIndex());

         for (YoVariable<?> variable : yoSharedBuffer.getRootRegistry().getAllVariables())
         {
            YoVariableBuffer<?> variableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(variable);
            assertVariableEqualsBufferAt(variable, variableBuffer, newProperties.getCurrentIndex());
         }
      }
   }

   @Test
   public void testResizeBuffer()
   {
      Random random = new Random(6234);

      { // Assert cases the buffer won't resize
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);

         assertFalse(yoSharedBuffer.resizeBuffer(0));
         assertFalse(yoSharedBuffer.resizeBuffer(-1));
         assertFalse(yoSharedBuffer.resizeBuffer(yoSharedBuffer.getProperties().getSize()));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Increase the size
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         YoBufferPropertiesReadOnly newProperties = yoSharedBuffer.getProperties();
         YoBufferPropertiesReadOnly initialProperties = newProperties.copy();

         int newSize = initialProperties.getSize() + random.nextInt(1000) + 1;

         assertTrue(yoSharedBuffer.resizeBuffer(newSize));
         assertEquals(newSize, newProperties.getSize());
         assertEquals(0, newProperties.getInPoint());
         assertEquals(initialProperties.getActiveBufferLength() - 1, newProperties.getOutPoint());
         if (initialProperties.isIndexBetweenBounds(initialProperties.getCurrentIndex()))
         {
            int expectedNewCurrentIndex = initialProperties.getCurrentIndex() - initialProperties.getInPoint();
            if (expectedNewCurrentIndex < 0)
               expectedNewCurrentIndex += initialProperties.getSize();
            assertEquals(expectedNewCurrentIndex, newProperties.getCurrentIndex());
         }
         else
         {
            assertEquals(newProperties.getOutPoint(), newProperties.getCurrentIndex());
         }

         for (YoVariable<?> variable : yoSharedBuffer.getRootRegistry().getAllVariables())
         {
            YoVariableBuffer<?> variableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(variable);
            assertVariableEqualsBufferAt(variable, variableBuffer, newProperties.getCurrentIndex());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Decrease the size while remaining greater than the length of the active part of the buffer
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         YoBufferPropertiesReadOnly newProperties = yoSharedBuffer.getProperties();

         while (newProperties.getActiveBufferLength() == newProperties.getSize())
            yoSharedBuffer.setOutPoint(random.nextInt(newProperties.getSize()));

         YoBufferPropertiesReadOnly initialProperties = newProperties.copy();

         int newSize = RandomNumbers.nextInt(random, initialProperties.getActiveBufferLength(), initialProperties.getSize() - 1);

         assertTrue(yoSharedBuffer.resizeBuffer(newSize));
         assertEquals(newSize, newProperties.getSize());
         assertEquals(0, newProperties.getInPoint());
         assertEquals(initialProperties.getActiveBufferLength() - 1, newProperties.getOutPoint());
         if (initialProperties.isIndexBetweenBounds(initialProperties.getCurrentIndex()))
         {
            int expectedNewCurrentIndex = initialProperties.getCurrentIndex() - initialProperties.getInPoint();
            if (expectedNewCurrentIndex < 0)
               expectedNewCurrentIndex += initialProperties.getSize();
            assertEquals(expectedNewCurrentIndex, newProperties.getCurrentIndex());
         }
         else
         {
            assertEquals(newProperties.getOutPoint(), newProperties.getCurrentIndex());
         }

         for (YoVariable<?> variable : yoSharedBuffer.getRootRegistry().getAllVariables())
         {
            YoVariableBuffer<?> variableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(variable);
            assertVariableEqualsBufferAt(variable, variableBuffer, newProperties.getCurrentIndex());
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Decrease the size to something smaller than the length of the active part of the buffer.
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         YoBufferPropertiesReadOnly newProperties = yoSharedBuffer.getProperties();
         YoBufferPropertiesReadOnly initialProperties = newProperties.copy();

         int newSize = random.nextInt(initialProperties.getActiveBufferLength()) + 1;

         assertTrue(yoSharedBuffer.resizeBuffer(newSize));
         assertEquals(newSize, newProperties.getSize());
         assertEquals(0, newProperties.getInPoint());
         assertEquals(newSize - 1, newProperties.getOutPoint());

         int expectedNewCurrentIndex = initialProperties.getCurrentIndex() - initialProperties.getInPoint();
         if (expectedNewCurrentIndex < 0)
            expectedNewCurrentIndex += initialProperties.getSize();
         expectedNewCurrentIndex += -initialProperties.getActiveBufferLength() + newSize;
         if (expectedNewCurrentIndex < 0 || expectedNewCurrentIndex >= newSize)
            assertEquals(newProperties.getOutPoint(), newProperties.getCurrentIndex());
         else
            assertEquals(expectedNewCurrentIndex, newProperties.getCurrentIndex());

         for (YoVariable<?> variable : yoSharedBuffer.getRootRegistry().getAllVariables())
         {
            YoVariableBuffer<?> variableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(variable);
            assertVariableEqualsBufferAt(variable, variableBuffer, newProperties.getCurrentIndex());
         }
      }
   }

   @Test
   public void testSetCurrentIndex()
   {
      Random random = new Random(4566);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         yoSharedBuffer.updateYoVariables();

         YoBufferPropertiesReadOnly properties = yoSharedBuffer.getProperties();

         assertFalse(yoSharedBuffer.setCurrentIndex(-1));
         assertFalse(yoSharedBuffer.setCurrentIndex(properties.getSize()));

         int newCurrentIndex = random.nextInt(properties.getSize());
         assertEquals(newCurrentIndex != properties.getCurrentIndex(), yoSharedBuffer.setCurrentIndex(newCurrentIndex));

         assertEquals(newCurrentIndex, properties.getCurrentIndex());
         assertFalse(yoSharedBuffer.setCurrentIndex(newCurrentIndex));

         for (YoVariable<?> variable : yoSharedBuffer.getRootRegistry().getAllVariables())
         {
            YoVariableBuffer<?> variableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(variable);
            assertVariableEqualsBufferAt(variable, variableBuffer, properties.getCurrentIndex());
         }
      }
   }

   @Test
   public void testSetInOutPoint()
   {
      Random random = new Random(4566);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoSharedBuffer yoSharedBuffer = YoBufferRandomTools.nextYoSharedBuffer(random, 2, 5);
         yoSharedBuffer.updateYoVariables();

         YoBufferPropertiesReadOnly properties = yoSharedBuffer.getProperties();

         // Test setInPoint
         assertFalse(yoSharedBuffer.setInPoint(-1));
         assertFalse(yoSharedBuffer.setInPoint(properties.getSize()));

         int newInPoint = random.nextInt(properties.getSize());
         assertEquals(newInPoint != properties.getInPoint(), yoSharedBuffer.setInPoint(newInPoint));

         assertEquals(newInPoint, properties.getInPoint());
         assertFalse(yoSharedBuffer.setInPoint(newInPoint));

         // Test setInPoint
         assertFalse(yoSharedBuffer.setOutPoint(-1));
         assertFalse(yoSharedBuffer.setOutPoint(properties.getSize()));
         
         int newOutPoint = random.nextInt(properties.getSize());
         assertEquals(newOutPoint != properties.getOutPoint(), yoSharedBuffer.setOutPoint(newOutPoint));
         
         assertEquals(newOutPoint, properties.getOutPoint());
         assertFalse(yoSharedBuffer.setOutPoint(newOutPoint));
      }
   }

   private static void assertVariableEqualsBufferAt(YoVariable<?> yoVariable, YoVariableBuffer<?> buffer, int index)
   {
      if (yoVariable instanceof YoBoolean)
         assertEquals(((YoBoolean) yoVariable).getValue(), ((YoBooleanBuffer) buffer).getBuffer()[index]);
      else if (yoVariable instanceof YoDouble)
         assertEquals(((YoDouble) yoVariable).getValue(), ((YoDoubleBuffer) buffer).getBuffer()[index]);
      else if (yoVariable instanceof YoInteger)
         assertEquals(((YoInteger) yoVariable).getValue(), ((YoIntegerBuffer) buffer).getBuffer()[index]);
      else if (yoVariable instanceof YoLong)
         assertEquals(((YoLong) yoVariable).getValue(), ((YoLongBuffer) buffer).getBuffer()[index]);
      else if (yoVariable instanceof YoEnum)
         assertEquals(((YoEnum<?>) yoVariable).getOrdinal(), ((YoEnumBuffer<?>) buffer).getBuffer()[index]);
      else
         throw new IllegalStateException("Type not handled: " + yoVariable.getClass());
   }
}
