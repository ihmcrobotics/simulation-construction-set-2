package us.ihmc.scs2.sharedMemory;

public class BufferSampleRequest
{
   private final int from;
   private final int length;

   public BufferSampleRequest(int from, int length)
   {
      this.from = from;
      this.length = length;
   }

   public int getFrom()
   {
      return from;
   }

   public int getLength()
   {
      return length;
   }
}
