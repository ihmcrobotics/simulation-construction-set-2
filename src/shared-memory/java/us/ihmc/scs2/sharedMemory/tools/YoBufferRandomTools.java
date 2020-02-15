package us.ihmc.scs2.sharedMemory.tools;

import java.util.Random;

import us.ihmc.scs2.sharedMemory.YoBufferProperties;

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
}
