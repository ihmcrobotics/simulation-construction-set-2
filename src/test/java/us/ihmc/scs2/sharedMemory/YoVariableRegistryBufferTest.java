package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sharedMemory.tools.YoBufferRandomTools;
import us.ihmc.scs2.sharedMemory.tools.YoMirroredRegistryTools;
import us.ihmc.scs2.sharedMemory.tools.YoRandomTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.*;

public class YoVariableRegistryBufferTest
{
   private static final int ITERATIONS = 1000;

   @BeforeAll
   public static void disableStackTrace()
   {
      YoVariable.SAVE_STACK_TRACE = false;
   }

   @Test
   public void testConstructor()
   {
      Random random = new Random(62825);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 10, 15);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         assertTrue(rootRegistry == yoVariableRegistryBuffer.getRootRegistry());

         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

         for (YoVariable<?> yoVariable : allYoVariables)
         {
            YoVariableBuffer<?> yoVariableBuffer = yoVariableRegistryBuffer.findYoVariableBuffer(yoVariable);
            assertNotNull(yoVariableBuffer);
            assertTrue(yoVariable == yoVariableBuffer.getYoVariable());
         }
      }
   }

   @Test
   public void testRegisterMissingBuffers()
   {
      Random random = new Random(74586);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         for (int j = 0; j < allRegistries.length; j++)
         {
            YoVariableRegistry yoVariableRegistry = allRegistries[j];
            YoRandomTools.nextYoVariableRegistryTree(random, yoVariableRegistry, "new" + j, 5, 5);
         }

         yoVariableRegistryBuffer.registerMissingBuffers();
         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

         for (YoVariable<?> yoVariable : allYoVariables)
         {
            YoVariableBuffer<?> yoVariableBuffer = yoVariableRegistryBuffer.findYoVariableBuffer(yoVariable);
            assertNotNull(yoVariableBuffer);
            assertTrue(yoVariable == yoVariableBuffer.getYoVariable());
         }
      }
   }

   @Test
   public void testResizeBuffer()
   {
      Random random = new Random(734259);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();
         List<YoVariableBuffer<?>> allYoVariableBuffers = new ArrayList<>();
         List<BufferSample<?>> allSamples = new ArrayList<>();

         int from = random.nextInt(bufferProperties.getSize());
         int length = random.nextInt(bufferProperties.getSize());

         for (YoVariable<?> yoVariable : allYoVariables)
         {
            YoVariableBuffer<?> yoVariableBuffer = yoVariableRegistryBuffer.findYoVariableBuffer(yoVariable);
            allYoVariableBuffers.add(yoVariableBuffer);
            allSamples.add(new BufferSample<>(0, bufferProperties.getSize(), yoVariableBuffer.copy(from, length).getSample(), length));
         }

         yoVariableRegistryBuffer.resizeBuffer(from, length);

         for (int j = 0; j < allYoVariables.size(); j++)
         {
            assertEquals(allSamples.get(j), allYoVariableBuffers.get(j).copy(0, length));
         }
      }
   }

   @Test
   public void testWriteBuffer()
   {
      Random random = new Random(734259);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();

         for (int j = 0; j < 10; j++)
         {
            allYoVariables.forEach(v -> YoRandomTools.randomizeYoVariable(random, v));
            yoVariableRegistryBuffer.writeBuffer();
            for (YoVariable<?> yoVariable : allYoVariables)
            {
               assertBufferCurrentValueEquals(bufferProperties.getCurrentIndex(), yoVariable, yoVariableRegistryBuffer.findYoVariableBuffer(yoVariable));
            }
         }
      }
   }

   @Test
   public void testReadBuffer()
   {
      Random random = new Random(734259);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         List<YoVariable<?>> allYoVariables = rootRegistry.getAllVariables();
         List<YoVariableBuffer<?>> allYoVariableBuffers = allYoVariables.stream().map(yoVariableRegistryBuffer::findYoVariableBuffer)
                                                                        .collect(Collectors.toList());

         for (int j = 0; j < 10; j++)
         {
            allYoVariableBuffers.forEach(b -> randomizeBuffer(random, bufferProperties.getCurrentIndex(), b));
            yoVariableRegistryBuffer.readBuffer();
            for (YoVariable<?> yoVariable : allYoVariables)
            {
               assertBufferCurrentValueEquals(bufferProperties.getCurrentIndex(), yoVariable, yoVariableRegistryBuffer.findYoVariableBuffer(yoVariable));
            }
         }
      }
   }

   @Test
   public void testFindOrCreateYoVariableBuffer()
   {
      Random random = new Random(78924);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         // Create a mirror of the registries
         YoVariableRegistry rootMirrorRegistry = new YoVariableRegistry(rootRegistry.getName());
         YoMirroredRegistryTools.duplicateMissingYoVariablesInTarget(rootRegistry, rootMirrorRegistry);

         // Add a YoVariable to one of the existing registries
         YoVariableRegistry yoVariableRegistry = rootMirrorRegistry.getAllRegistriesIncludingChildren().get(random.nextInt(allRegistries.length - 1));
         YoVariable<?> newYoVariable = YoRandomTools.nextYoVariable(random, yoVariableRegistry);
         assertNull(yoVariableRegistryBuffer.findYoVariableBuffer(newYoVariable));
         assertNull(rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()));
         YoVariableBuffer<?> newBuffer = yoVariableRegistryBuffer.findOrCreateYoVariableBuffer(newYoVariable);
         assertEquals(newYoVariable.getFullNameWithNameSpace(), newBuffer.getYoVariable().getFullNameWithNameSpace());
         assertNotNull(yoVariableRegistryBuffer.findYoVariableBuffer(newYoVariable));
         assertNotNull(rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()));
         assertEquals(newYoVariable.getClass(), rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()).getClass());

         // Add a YoVariable with new registries to one of the existing registries.
         List<YoVariableRegistry> newRegistries = new ArrayList<>();

         for (int j = 0; j < random.nextInt(10) + 1; j++)
         {
            YoVariableRegistry newChild = new YoVariableRegistry(YoRandomTools.nextAvailableRegistryName(random, 10, 20, yoVariableRegistry));
            yoVariableRegistry.addChild(newChild);
            newRegistries.add(newChild);
            yoVariableRegistry = newChild;
         }

         newYoVariable = YoRandomTools.nextYoVariable(random, yoVariableRegistry);
         assertNull(yoVariableRegistryBuffer.findYoVariableBuffer(newYoVariable));
         assertNull(rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()));
         newBuffer = yoVariableRegistryBuffer.findOrCreateYoVariableBuffer(newYoVariable);
         assertEquals(newYoVariable.getFullNameWithNameSpace(), newBuffer.getYoVariable().getFullNameWithNameSpace());
         assertNotNull(yoVariableRegistryBuffer.findYoVariableBuffer(newYoVariable));
         assertNotNull(rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()));
         assertEquals(newYoVariable.getClass(), rootRegistry.getVariable(newYoVariable.getFullNameWithNameSpace()).getClass());
         newRegistries.forEach(newRegistry -> assertNotNull(rootRegistry.getRegistry(newRegistry.getNameSpace())));
      }
   }

   @Test
   public void testNewLinkedYoVariableRegistry()
   {
      Random random = new Random(978345);

      for (int i = 0; i < ITERATIONS; i++)
      {
         YoVariableRegistry[] allRegistries = YoRandomTools.nextYoVariableRegistryTree(random, 5, 5);
         YoVariableRegistry rootRegistry = allRegistries[0];
         YoBufferProperties bufferProperties = YoBufferRandomTools.nextYoBufferProperties(random);
         YoVariableRegistryBuffer yoVariableRegistryBuffer = new YoVariableRegistryBuffer(rootRegistry, bufferProperties);

         LinkedYoVariableRegistry linkedRootRegistry = yoVariableRegistryBuffer.newLinkedYoVariableRegistry();
         assertEquals(rootRegistry.getName(), linkedRootRegistry.getRootRegistry().getName());
         for (YoVariableRegistry registry : allRegistries)
         {
            YoVariableRegistry linkedRegistry = linkedRootRegistry.getRootRegistry().getRegistry(registry.getNameSpace());
            assertNotNull(linkedRegistry);
            assertEquals(linkedRegistry.getChildren().size(), registry.getChildren().size());
            assertEquals(linkedRegistry.getAllVariablesInThisListOnly().size(), registry.getAllVariablesInThisListOnly().size());
         }

         YoVariableRegistry subTreeRootRegistry = allRegistries[random.nextInt(allRegistries.length)];
         YoVariableRegistry linkedSubTreeRootRegistry = YoMirroredRegistryTools.newEmptyCloneRegistry(subTreeRootRegistry);

         LinkedYoVariableRegistry linkedSubTreeRegistry = yoVariableRegistryBuffer.newLinkedYoVariableRegistry(linkedSubTreeRootRegistry);
         assertTrue(linkedSubTreeRootRegistry == linkedSubTreeRegistry.getRootRegistry());

         for (YoVariableRegistry registry : subTreeRootRegistry.getAllRegistriesIncludingChildren())
         {
            YoVariableRegistry linkedRegistry = linkedSubTreeRootRegistry.getRegistry(registry.getNameSpace());
            assertNotNull(linkedRegistry);
            assertEquals(linkedRegistry.getChildren().size(), registry.getChildren().size());
            assertEquals(linkedRegistry.getAllVariablesInThisListOnly().size(), registry.getAllVariablesInThisListOnly().size());
         }
      }
   }

   private static void assertBufferCurrentValueEquals(int currentIndex, YoVariable<?> expectedValue, YoVariableBuffer<?> yoVariableBuffer)
   {
      if (expectedValue instanceof YoBoolean)
         assertEquals(((YoBoolean) expectedValue).getValue(), ((YoBooleanBuffer) yoVariableBuffer).getBuffer()[currentIndex]);
      else if (expectedValue instanceof YoDouble)
         assertEquals(((YoDouble) expectedValue).getValue(), ((YoDoubleBuffer) yoVariableBuffer).getBuffer()[currentIndex]);
      else if (expectedValue instanceof YoInteger)
         assertEquals(((YoInteger) expectedValue).getValue(), ((YoIntegerBuffer) yoVariableBuffer).getBuffer()[currentIndex]);
      else if (expectedValue instanceof YoLong)
         assertEquals(((YoLong) expectedValue).getValue(), ((YoLongBuffer) yoVariableBuffer).getBuffer()[currentIndex]);
      else if (expectedValue instanceof YoEnum)
         assertEquals(((YoEnum<?>) expectedValue).getOrdinal(), ((YoEnumBuffer<?>) yoVariableBuffer).getBuffer()[currentIndex]);
      else
         throw new IllegalStateException("Unhandled variable type.");
   }

   private static void randomizeBuffer(Random random, int index, YoVariableBuffer<?> yoVariableBuffer)
   {
      if (yoVariableBuffer instanceof YoBooleanBuffer)
      {
         ((YoBooleanBuffer) yoVariableBuffer).getBuffer()[index] = random.nextBoolean();
      }
      else if (yoVariableBuffer instanceof YoDoubleBuffer)
      {
         ((YoDoubleBuffer) yoVariableBuffer).getBuffer()[index] = random.nextDouble();
      }
      else if (yoVariableBuffer instanceof YoIntegerBuffer)
      {
         ((YoIntegerBuffer) yoVariableBuffer).getBuffer()[index] = random.nextInt();
      }
      else if (yoVariableBuffer instanceof YoLongBuffer)
      {
         ((YoLongBuffer) yoVariableBuffer).getBuffer()[index] = random.nextLong();
      }
      else if (yoVariableBuffer instanceof YoEnumBuffer)
      {
         YoEnumBuffer<?> yoEnumBuffer = (YoEnumBuffer<?>) yoVariableBuffer;
         if (yoEnumBuffer.getYoVariable().getAllowNullValue())
            yoEnumBuffer.getBuffer()[index] = (byte) (random.nextInt(yoEnumBuffer.getYoVariable().getEnumSize() + 1) - 1);
         else
            yoEnumBuffer.getBuffer()[index] = (byte) random.nextInt(yoEnumBuffer.getYoVariable().getEnumSize());
      }
      else
      {
         throw new IllegalStateException("Unhandled buffer type.");
      }
   }
}
