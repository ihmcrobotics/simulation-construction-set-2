package us.ihmc.scs2.sharedMemory;

public class BufferSample<D>
{
   private final int from;
   private final int bufferSize;
   private final D sample;
   private final int sampleLength;

   public BufferSample(int from, int bufferSize, D sample, int sampleLength)
   {
      this.from = from;
      this.bufferSize = bufferSize;
      this.sample = sample;
      this.sampleLength = sampleLength;
   }

   public int getFrom()
   {
      return from;
   }

   public int getTo()
   {
      return (from + sampleLength) % bufferSize;
   }

   public int getBufferSize()
   {
      return bufferSize;
   }

   public D getSample()
   {
      return sample;
   }

   public int getSampleLength()
   {
      return sampleLength;
   }
}
