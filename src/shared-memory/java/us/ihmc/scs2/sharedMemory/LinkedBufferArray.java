package us.ihmc.scs2.sharedMemory;

import java.util.Arrays;

public class LinkedBufferArray extends LinkedBuffer
{
   private int size = 0;
   private LinkedBuffer[] linkedBuffers = new LinkedBuffer[8];

   public LinkedBufferArray()
   {
   }

   public boolean add(LinkedBuffer e)
   {
      size++;
      ensureCapacity(size);
      linkedBuffers[size - 1] = e;
      return true;
   }

   public int size()
   {
      return size;
   }

   @Override
   public boolean pull()
   {
      boolean hasPulledSomething = false;
      for (int i = 0; i < size; i++)
      {
         hasPulledSomething |= linkedBuffers[i].pull();
      }
      return hasPulledSomething;
   }

   @Override
   public void push()
   {
      for (int i = 0; i < size; i++)
      {
         linkedBuffers[i].push();
      }
   }

   @Override
   public boolean processPush(boolean writeBuffer)
   {
      boolean hasPushedSomething = false;

      for (int i = 0; i < size; i++)
      {
         hasPushedSomething |= linkedBuffers[i].processPush(writeBuffer);
      }

      return hasPushedSomething;
   }

   @Override
   public void flushPush()
   {
      for (int i = 0; i < size; i++)
      {
         linkedBuffers[i].flushPush();
      }
   }

   @Override
   public void prepareForPull()
   {
      for (int i = 0; i < size; i++)
      {
         linkedBuffers[i].prepareForPull();
      }
   }

   @Override
   public boolean hasRequestPending()
   {
      for (int i = 0; i < size; i++)
      {
         if (linkedBuffers[i].hasRequestPending())
            return true;
      }
      return false;
   }

   private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

   protected void ensureCapacity(int minCapacity)
   {
      if (minCapacity <= linkedBuffers.length)
         return;

      int previousArraySize = linkedBuffers.length;
      int newArraySize = previousArraySize + (previousArraySize >> 1);
      if (newArraySize - minCapacity < 0)
         newArraySize = minCapacity;
      if (newArraySize - MAX_ARRAY_SIZE > 0)
         newArraySize = checkWithMaxCapacity(minCapacity);

      linkedBuffers = Arrays.copyOf(linkedBuffers, newArraySize);
   }

   private static int checkWithMaxCapacity(int minCapacity)
   {
      if (minCapacity < 0) // overflow
         throw new OutOfMemoryError();
      return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
   }
}