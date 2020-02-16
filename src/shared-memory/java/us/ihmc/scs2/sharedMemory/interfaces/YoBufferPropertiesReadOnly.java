package us.ihmc.scs2.sharedMemory.interfaces;

import us.ihmc.scs2.sharedMemory.tools.BufferTools;

public interface YoBufferPropertiesReadOnly
{
   int getSize();

   int getCurrentIndex();

   int getInPoint();

   int getOutPoint();

   default int getActiveBufferLength()
   {
      return BufferTools.computeSubLength(getInPoint(), getOutPoint(), getSize());
   }

   default boolean isIndexBetweenBounds(int indexToCheck)
   {
      return BufferTools.isInsideBounds(indexToCheck, getInPoint(), getOutPoint(), getSize());
   }

   default YoBufferPropertiesReadOnly copy()
   {
      int size = getSize();
      int currentIndex = getCurrentIndex();
      int inPoint = getInPoint();
      int outPoint = getOutPoint();

      return new YoBufferPropertiesReadOnly()
      {
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
      };
   }

   default boolean equals(YoBufferPropertiesReadOnly other)
   {
      if (other == null)
         return false;
      if (getCurrentIndex() != other.getCurrentIndex())
         return false;
      if (getSize() != other.getSize())
         return false;
      if (getInPoint() != other.getInPoint())
         return false;
      if (getOutPoint() != other.getOutPoint())
         return false;
      return true;
   }
}
