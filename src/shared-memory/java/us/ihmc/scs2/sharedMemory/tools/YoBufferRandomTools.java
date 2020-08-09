package us.ihmc.scs2.sharedMemory.tools;

import java.util.List;
import java.util.Random;

import us.ihmc.scs2.sharedMemory.*;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.*;

public class YoBufferRandomTools
{
   public static YoBufferProperties nextYoBufferProperties(Random random)
   {
      return nextYoBufferProperties(random, random.nextInt(1000) + 2);
   }

   public static YoBufferProperties nextYoBufferProperties(Random random, int size)
   {
      YoBufferProperties next = new YoBufferProperties(random.nextInt(size), size);
      next.setInPointIndex(random.nextInt(size));
      next.setOutPointIndex(random.nextInt(size));
      return next;
   }

   public static YoVariableBuffer<?> nextYoVariableBuffer(Random random, YoVariable yoVariable)
   {
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoVariableBuffer<?> next = YoVariableBuffer.newYoVariableBuffer(yoVariable, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoBooleanBuffer nextYoBooleanBuffer(Random random, YoRegistry registry)
   {
      YoBoolean yoBoolean = YoRandomTools.nextYoBoolean(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoBooleanBuffer next = new YoBooleanBuffer(yoBoolean, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoDoubleBuffer nextYoDoubleBuffer(Random random, YoRegistry registry)
   {
      YoDouble yoDouble = YoRandomTools.nextYoDouble(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoDoubleBuffer next = new YoDoubleBuffer(yoDouble, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoIntegerBuffer nextYoIntegerBuffer(Random random, YoRegistry registry)
   {
      YoInteger yoInteger = YoRandomTools.nextYoInteger(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoIntegerBuffer next = new YoIntegerBuffer(yoInteger, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoLongBuffer nextYoLongBuffer(Random random, YoRegistry registry)
   {
      YoLong yoLong = YoRandomTools.nextYoLong(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoLongBuffer next = new YoLongBuffer(yoLong, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoEnumBuffer<?> nextYoEnumBuffer(Random random, YoRegistry registry)
   {
      YoEnum<?> yoEnum = YoRandomTools.nextYoEnum(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoEnumBuffer<?> next = new YoEnumBuffer<>(yoEnum, properties);
      randomizeYoVariableBuffer(random, next);

      return next;
   }

   public static YoSharedBuffer nextYoSharedBuffer(Random random, int maxNumberOfVariablesPerRegistry, int numberOfRegistries)
   {
      return nextYoSharedBuffer(random, YoRandomTools.nextYoRegistryTree(random, maxNumberOfVariablesPerRegistry, numberOfRegistries)[0]);
   }

   public static YoSharedBuffer nextYoSharedBuffer(Random random, YoRegistry rootRegistry)
   {
      YoSharedBuffer next = new YoSharedBuffer(rootRegistry, random.nextInt(500) + 2);
      randomizeYoSharedBuffer(random, next);
      return next;
   }

   public static void randomizeYoSharedBuffer(Random random, YoSharedBuffer yoSharedBuffer)
   {
      randomizeYoRegistryBuffer(random, yoSharedBuffer.getRegistryBuffer());
      yoSharedBuffer.setCurrentIndex(random.nextInt(yoSharedBuffer.getProperties().getSize()));
      yoSharedBuffer.setInPoint(random.nextInt(yoSharedBuffer.getProperties().getSize()));
      yoSharedBuffer.setOutPoint(random.nextInt(yoSharedBuffer.getProperties().getSize()));
   }

   public static void randomizeYoRegistryBuffer(Random random, YoRegistryBuffer YoRegistryBuffer)
   {
      List<YoVariable> allVariables = YoRegistryBuffer.getRootRegistry().collectSubtreeVariables();

      for (int i = 0; i < YoRegistryBuffer.getProperties().getSize(); i++)
      {
         allVariables.forEach(v -> YoRandomTools.randomizeYoVariable(random, v));
         YoRegistryBuffer.writeBufferAt(i);
      }
   }

   public static void randomizeYoVariableBuffer(Random random, YoVariableBuffer<?> yoVariableBuffer)
   {
      for (int i = 0; i < yoVariableBuffer.getProperties().getSize(); i++)
      {
         YoRandomTools.randomizeYoVariable(random, yoVariableBuffer.getYoVariable());
         yoVariableBuffer.writeBufferAt(i);
      }
   }
}
