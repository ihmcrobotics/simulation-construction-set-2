package us.ihmc.scs2.sharedMemory;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class YoBufferProperties implements YoBufferPropertiesReadOnly
{
   private int inPoint = 0;
   private int outPoint = 0;

   private int currentIndex = 0;
   private int size = 0;

   public YoBufferProperties()
   {
   }

   public YoBufferProperties(int index, int size)
   {
      setSize(size);
      setCurrentIndex(index);
   }

   @Override
   public int getSize()
   {
      return size;
   }

   @Override
   public int getCurrentIndex()
   {
      return currentIndex;
   }

   @Override
   public int getInPoint()
   {
      return inPoint;
   }

   @Override
   public int getOutPoint()
   {
      return outPoint;
   }

   public int incrementIndex(boolean updateBufferBounds)
   {
      currentIndex = increment(currentIndex);

      if (updateBufferBounds)
      {
         // The out-point is always at the last writing index.
         outPoint = currentIndex;

         if (outPoint == inPoint)
         { // Push the in-point
            inPoint = increment(inPoint);
         }
      }
      else if (!isIndexBetweenBounds(currentIndex))
      { // The current index has to remain within the bounds when reading.
         currentIndex = inPoint;
      }
      return currentIndex;
   }

   public int incrementIndex(boolean updateBufferBounds, int stepSize)
   {
      if (stepSize < 0)
         return decrementIndex(-stepSize);

      for (int i = 0; i < stepSize; i++)
         incrementIndex(updateBufferBounds);
      return currentIndex;
   }

   public int decrementIndex(int stepSize)
   {
      if (stepSize < 0)
         return incrementIndex(false, stepSize);
      
      currentIndex -= stepSize;
      if (!isIndexBetweenBounds(currentIndex))
         currentIndex = outPoint;
      return currentIndex;
   }

   public boolean setCurrentIndexSafe(int newCurrentIndex)
   {
      if (newCurrentIndex < 0 || newCurrentIndex >= size || newCurrentIndex == currentIndex)
         return false;
      currentIndex = newCurrentIndex;
      return true;
   }

   public void setCurrentIndex(int currentIndex)
   {
      this.currentIndex = currentIndex;
   }

   public void setSize(int size)
   {
      this.size = size;
   }

   public boolean setInPointIndex(int newInPointIndex)
   {
      if (newInPointIndex < 0 || newInPointIndex >= size || newInPointIndex == inPoint)
         return false;
      inPoint = newInPointIndex;
      return true;
   }

   public boolean setOutPointIndex(int newOutPointIndex)
   {
      if (newOutPointIndex < 0 || newOutPointIndex >= size || newOutPointIndex == outPoint)
         return false;
      outPoint = newOutPointIndex;
      return true;
   }

   private int increment(int index)
   {
      return increment(index, 1);
   }

   private int increment(int index, int stepSize)
   {
      index += stepSize;
      return index >= size ? 0 : index;
   }
}
