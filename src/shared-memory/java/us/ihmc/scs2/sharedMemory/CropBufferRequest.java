package us.ihmc.scs2.sharedMemory;

public interface CropBufferRequest
{
   int getFrom();

   int getTo();

   default int getSize(int originalBufferSize)
   {
      if (getFrom() <= getTo())
         return getTo() - getFrom() + 1;
      else
         return originalBufferSize;
   }

   public static CropBufferRequest toCropBufferRequest(int from, int to)
   {
      return new CropBufferRequest()
      {
         @Override
         public int getFrom()
         {
            return from;
         }

         @Override
         public int getTo()
         {
            return to;
         }
      };
   }
}
