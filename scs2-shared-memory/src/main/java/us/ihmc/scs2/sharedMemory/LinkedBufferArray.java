package us.ihmc.scs2.sharedMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import us.ihmc.euclid.tools.EuclidCoreIOTools;
import us.ihmc.log.LogTools;

public class LinkedBufferArray extends LinkedBuffer
{
   private int size = 0;
   private LinkedBuffer[] linkedBuffers = new LinkedBuffer[8];

   private final AtomicReference<Set<LinkedBuffer>> linkedBuffersWithPendingPushRequest = new AtomicReference<>(null);
   private final PushRequestListener listener = target ->
   {
      linkedBuffersWithPendingPushRequest.getAndUpdate(pendingSet ->
      {
         if (pendingSet == null)
            pendingSet = new HashSet<>();
         pendingSet.add(target);
         return pendingSet;
      });
   };

   private final List<LinkedBufferChangeListener> changeListeners = new ArrayList<>();

   private boolean isDisposed = false;

   public LinkedBufferArray()
   {
   }

   public void cleanupInactiveLinkedBuffers()
   {
      if (isDisposed)
         return;

      for (int i = size - 1; i >= 0; i--)
      {
         LinkedBuffer linkedBuffer = linkedBuffers[i];

         if (linkedBuffer == null)
         {
            LogTools.error(String.format("Unexpected null pointer, queried index:%d, current size:%d, elements: [%s], stacktrace:[%s]",
                                         i,
                                         size,
                                         EuclidCoreIOTools.getArrayString(", ", linkedBuffers, b -> b.getClass().getSimpleName()),
                                         EuclidCoreIOTools.getArrayString(", ", new Exception().getStackTrace())));
            continue;
         }

         if (!linkedBuffer.isActive())
            remove(i);
      }
   }

   public boolean add(LinkedBuffer e)
   {
      if (isDisposed)
         return false;

      ensureCapacity(size + 1);
      linkedBuffers[size++] = e;
      e.addPushRequestListener(listener);
      Change change = new Change(true, false, e);
      changeListeners.forEach(listener -> listener.onChange(change));
      return true;
   }

   public boolean remove(LinkedBuffer e)
   {
      if (isDisposed)
         return false;

      int index = indexOf(e);
      if (index == -1)
         return false;
      remove(index);
      return true;
   }

   public LinkedBuffer remove(int index)
   {
      if (isDisposed)
         return null;

      LinkedBuffer removedLinkedBuffer = linkedBuffers[index];
      removedLinkedBuffer.removePushRequestListener(listener);
      linkedBuffers[index] = linkedBuffers[--size];
      linkedBuffers[size] = null;
      Change change = new Change(false, true, removedLinkedBuffer);
      changeListeners.forEach(listener -> listener.onChange(change));
      return removedLinkedBuffer;
   }

   public int indexOf(LinkedBuffer e)
   {
      if (isDisposed)
         return -1;

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
      if (isDisposed)
         return;

      changeListeners.add(listener);
   }

   public boolean removeChangeListener(LinkedBufferChangeListener listener)
   {
      return changeListeners.remove(listener);
   }

   @Override
   public boolean pull()
   {
      if (isDisposed)
         return false;

      boolean hasPulledSomething = false;
      try
      {
         boolean attemptRepair = false;

         for (int i = 0; i < size; i++)
         {
            if (linkedBuffers[i] == null)
            {
               if (!attemptRepair)
               {
                  LogTools.error(String.format("Unexpected null pointer, queried index:%d, current size:%d, elements: [%s], stacktrace:[%s]",
                                               i,
                                               size,
                                               EuclidCoreIOTools.getArrayString(", ", linkedBuffers, b -> b.getClass().getSimpleName()),
                                               EuclidCoreIOTools.getArrayString(", ", new Exception().getStackTrace())));
                  LogTools.info("Attempting to repair.");
                  attemptRepair = true;
               }
               linkedBuffers[i] = linkedBuffers[--size];
               i--;
            }
            else
            {
               hasPulledSomething |= linkedBuffers[i].pull();
            }
         }
      }
      catch (NullPointerException e)
      {
         LogTools.info("linkedBuffers: size = " + size + ", " + Arrays.toString(linkedBuffers));
         e.printStackTrace();
      }
      return hasPulledSomething;
   }

   @Override
   public void push()
   {
      if (isDisposed)
         return;

      for (int i = 0; i < size; i++)
      {
         linkedBuffers[i].push();
      }
   }

   @Override
   public boolean processPush(boolean writeBuffer)
   {
      if (isDisposed)
         return false;

      Set<LinkedBuffer> localRequests = linkedBuffersWithPendingPushRequest.getAndSet(null);

      if (localRequests == null || localRequests.isEmpty())
         return false;
      localRequests.forEach(buffer -> buffer.processPush(writeBuffer));
      return true;
   }

   @Override
   public void flushPush()
   {
      if (isDisposed)
         return;

      for (int i = 0; i < size; i++)
      {
         LinkedBuffer linkedBuffer = linkedBuffers[i];

         if (linkedBuffer == null)
         {
            LogTools.error(String.format("Unexpected null pointer, queried index:%d, current size:%d, elements: [%s], stacktrace:[%s]",
                                         i,
                                         size,
                                         EuclidCoreIOTools.getArrayString(", ", linkedBuffers, b -> b.getClass().getSimpleName()),
                                         EuclidCoreIOTools.getArrayString(", ", new Exception().getStackTrace())));
            continue;
         }

         linkedBuffer.flushPush();
      }
   }

   @Override
   public void prepareForPull()
   {
      if (isDisposed)
         return;

      for (int i = size - 1; i >= 0; i--)
      {
         LinkedBuffer linkedBuffer = linkedBuffers[i];

         if (linkedBuffer == null)
         {
            LogTools.error(String.format("Unexpected null pointer, queried index:%d, current size:%d, elements: [%s], stacktrace:[%s]",
                                         i,
                                         size,
                                         EuclidCoreIOTools.getArrayString(", ", linkedBuffers, b -> b.getClass().getSimpleName()),
                                         EuclidCoreIOTools.getArrayString(", ", new Exception().getStackTrace())));
            continue;
         }

         if (!linkedBuffer.isActive())
            remove(i);
         else
            linkedBuffer.prepareForPull();
      }
   }

   @Override
   public boolean hasRequestPending()
   {
      for (int i = 0; i < size; i++)
      {
         LinkedBuffer linkedBuffer = linkedBuffers[i];

         if (linkedBuffer == null)
         {
            LogTools.error(String.format("Unexpected null pointer, queried index:%d, current size:%d, elements: [%s], stacktrace:[%s]",
                                         i,
                                         size,
                                         EuclidCoreIOTools.getArrayString(", ", linkedBuffers, b -> b.getClass().getSimpleName()),
                                         EuclidCoreIOTools.getArrayString(", ", new Exception().getStackTrace())));
            continue;
         }

         if (linkedBuffer.hasRequestPending())
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
      return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
   }

   @Override
   public void dispose()
   {
      if (isDisposed)
         return;

      isDisposed = true;

      for (int i = 0; i < linkedBuffers.length; i++)
      {
         if (linkedBuffers[i] != null)
         {
            linkedBuffers[i].dispose();
            linkedBuffers[i] = null;
         }
      }
      linkedBuffers = null;
      linkedBuffersWithPendingPushRequest.set(null);
      changeListeners.clear();
   }

   public interface LinkedBufferChangeListener
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