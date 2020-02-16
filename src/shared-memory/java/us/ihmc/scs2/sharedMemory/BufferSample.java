package us.ihmc.scs2.sharedMemory;

import java.util.Arrays;

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

   public BufferSample(BufferSample<D> other)
   {
      this.from = other.from;
      this.bufferSize = other.bufferSize;
      this.sample = other.sample;
      this.sampleLength = other.sampleLength;
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

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof BufferSample)
      {
         BufferSample<?> other = (BufferSample<?>) object;

         if (from != other.from)
            return false;
         if (bufferSize != other.bufferSize)
            return false;
         if (sampleLength != other.sampleLength)
            return false;
         return sampleEquals(sample, other.sample);
      }
      else
      {
         return false;
      }
   }

   static boolean sampleEquals(Object expected, Object actual)
   {
      if (expected == null)
         return actual == null;
      if (actual == null)
         return false;
      if (expected == actual)
         return true;

      if (!expected.getClass().isArray())
         throw new IllegalArgumentException("The argument \"expected\" is not an array.");
      if (!actual.getClass().isArray())
         throw new IllegalArgumentException("The argument \"actual\" is not an array.");

      if (expected.getClass() != actual.getClass())
         return false;
      if (expected.getClass().getComponentType() == boolean.class)
         return Arrays.equals((boolean[]) expected, (boolean[]) actual);
      if (expected.getClass().getComponentType() == double.class)
         return Arrays.equals((double[]) expected, (double[]) actual);
      if (expected.getClass().getComponentType() == int.class)
         return Arrays.equals((int[]) expected, (int[]) actual);
      if (expected.getClass().getComponentType() == long.class)
         return Arrays.equals((long[]) expected, (long[]) actual);
      if (expected.getClass().getComponentType() == byte.class)
         return Arrays.equals((byte[]) expected, (byte[]) actual);
      throw new IllegalStateException("Unhandled array type: " + expected.getClass().getComponentType());
   }

   @Override
   public String toString()
   {
      return "from: " + from + ", sample length: " + sampleLength + ", type: " + sample.getClass().getComponentType();
   }
}
