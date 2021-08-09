package us.ihmc.scs2.sharedMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkedBufferArray extends LinkedBuffer
{
   private int size = 0;
   private LinkedBuffer[] linkedBuffers = new LinkedBuffer[8];

   private final Set<LinkedBuffer> linkedBuffersWithPendingPushRequest = new HashSet<>();
   private final PushRequestListener listener = target -> this.linkedBuffersWithPendingPushRequest.add(target);

   private final List<LinkedBufferChangeListener> changeListeners = new ArrayList<>();

   public LinkedBufferArray()
   {
   }

   public void cleanupInactiveLinkedBuffers()
   {
      for (int i = size - 1; i >= 0; i--)
      {
         if (!linkedBuffers[i].isActive())
            remove(i);
      }
   }

   public boolean add(LinkedBuffer e)
   {
      size++;
      ensureCapacity(size);
      linkedBuffers[size - 1] = e;
      e.addPushRequestListener(listener);
      Change change = new Change(true, false, e);
      changeListeners.forEach(listener -> listener.onChange(change));
      return true;
   }

   public boolean remove(LinkedBuffer e)
   {
      int index = indexOf(e);
      if (index == -1)
         return false;
      remove(index);
      return true;
   }

   public LinkedBuffer remove(int index)
   {
      LinkedBuffer removedLinkedBuffer = linkedBuffers[index];
      linkedBuffers[index].removePushRequestListener(listener);
      linkedBuffers[index] = linkedBuffers[size - 1];
      size--;
      linkedBuffers[size] = null;
      Change change = new Change(false, true, removedLinkedBuffer);
      changeListeners.forEach(listener -> listener.onChange(change));
      return removedLinkedBuffer;
   }

   public int indexOf(LinkedBuffer e)
   {
      if (e != null)
      {
         for (int i = 0; i < size; i++)
         {
            if (e.equals(linkedBuffers[i]))
               return i;
         }
      }
      return -1;
   }

   public int size()
   {
      return size;
   }

   public void addChangeListener(LinkedBufferChangeListener listener)
   {
      changeListeners.add(listener);
   }

   public boolean removeChangeListener(LinkedBufferChangeListener listener)
   {
      return changeListeners.remove(listener);
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
      if (linkedBuffersWithPendingPushRequest.isEmpty())
         return false;
      linkedBuffersWithPendingPushRequest.forEach(buffer -> buffer.processPush(writeBuffer));
      linkedBuffersWithPendingPushRequest.clear();
      return true;
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
      for (int i = size - 1; i >= 0; i--)
      {
         if (!linkedBuffers[i].isActive())
            remove(i);
         else
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

   public static interface LinkedBufferChangeListener
   {
      void onChange(Change change);
   }

   public static class Change
   {
      private final boolean wasLinkedBufferAdded;
      private final boolean wasLinkedBufferRemoved;
      private final LinkedBuffer target;

      private Change(boolean wasLinkedBufferAdded, boolean wasLinkedBufferRemoved, LinkedBuffer target)
      {
         this.wasLinkedBufferAdded = wasLinkedBufferAdded;
         this.wasLinkedBufferRemoved = wasLinkedBufferRemoved;
         this.target = target;
      }

      public boolean wasLinkedBufferAdded()
      {
         return wasLinkedBufferAdded;
      }

      public boolean wasLinkedBufferRemoved()
      {
         return wasLinkedBufferRemoved;
      }

      public LinkedBuffer getTarget()
      {
         return target;
      }
   }
}