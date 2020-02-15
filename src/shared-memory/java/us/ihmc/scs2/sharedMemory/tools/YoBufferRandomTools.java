package us.ihmc.scs2.sharedMemory.tools;

import java.util.Random;

import us.ihmc.scs2.sharedMemory.*;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.*;

public class YoBufferRandomTools
{
   public static YoBufferProperties nextYoBufferProperties(Random random)
   {
      return nextYoBufferProperties(random, random.nextInt(100000) + 1);
   }

   public static YoBufferProperties nextYoBufferProperties(Random random, int size)
   {
      YoBufferProperties next = new YoBufferProperties(random.nextInt(size), size);
      next.setInPointIndex(random.nextInt(size));
      next.setOutPointIndex(random.nextInt(size));
      return next;
   }

   public static YoBooleanBuffer nextYoBooleanBuffer(Random random, YoVariableRegistry registry)
   {
      YoBoolean yoBoolean = YoRandomTools.nextYoBoolean(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoBooleanBuffer next = new YoBooleanBuffer(yoBoolean, properties);
      randomizeYoVariableBuffer(random, properties, next);

      return next;
   }

   public static YoDoubleBuffer nextYoDoubleBuffer(Random random, YoVariableRegistry registry)
   {
      YoDouble yoDouble = YoRandomTools.nextYoDouble(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoDoubleBuffer next = new YoDoubleBuffer(yoDouble, properties);
      randomizeYoVariableBuffer(random, properties, next);

      return next;
   }

   public static YoIntegerBuffer nextYoIntegerBuffer(Random random, YoVariableRegistry registry)
   {
      YoInteger yoInteger = YoRandomTools.nextYoInteger(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoIntegerBuffer next = new YoIntegerBuffer(yoInteger, properties);
      randomizeYoVariableBuffer(random, properties, next);

      return next;
   }

   public static YoLongBuffer nextYoLongBuffer(Random random, YoVariableRegistry registry)
   {
      YoLong yoLong = YoRandomTools.nextYoLong(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoLongBuffer next = new YoLongBuffer(yoLong, properties);
      randomizeYoVariableBuffer(random, properties, next);

      return next;
   }

   public static YoEnumBuffer<?> nextYoEnumBuffer(Random random, YoVariableRegistry registry)
   {
      YoEnum<?> yoEnum = YoRandomTools.nextYoEnum(random, registry);
      YoBufferProperties properties = nextYoBufferProperties(random);
      YoEnumBuffer<?> next = new YoEnumBuffer<>(yoEnum, properties);
      randomizeYoVariableBuffer(random, properties, next);

      return next;
   }

   public static void randomizeYoVariableBuffer(Random random, YoBufferProperties bufferProperties, YoVariableBuffer<?> yoVariableBuffer)
   {
      int originalIndex = bufferProperties.getCurrentIndex();

      for (int i = 0; i < bufferProperties.getSize(); i++)
      {
         YoRandomTools.randomizeYoVariable(random, yoVariableBuffer.getYoVariable());
         bufferProperties.setCurrentIndexUnsafe(i);
         yoVariableBuffer.writeBuffer();
      }

      bufferProperties.setCurrentIndexUnsafe(originalIndex);
   }
}
