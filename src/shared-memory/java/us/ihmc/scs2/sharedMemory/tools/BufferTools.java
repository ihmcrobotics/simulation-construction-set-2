package us.ihmc.scs2.sharedMemory.tools;

import java.util.Arrays;

public class BufferTools
{
   /**
    * Calculates the sub-length defined by the interval [{@code from}, {@code to}] in a ring buffer of
    * size {@code length}.
    * 
    * @param from   the (inclusive) start of the interval.
    * @param to     the (inclusive) end of the interval.
    * @param length the number of elements in the ring buffer.
    * @return the number of elements contained in the interval [{@code from}, {@code to}].
    */
   public static int computeSubLength(int from, int to, int length)
   {
      int subLength = to - from + 1;
      if (subLength <= 0)
         subLength += length;
      return subLength;
   }

   /**
    * Tests whether the {@code query} is contained within the sub-interval of a buffer defined by
    * [{@code start}, {@code to}].
    * 
    * @param query the index to test.
    * @param start the (inclusive) start of the interval.
    * @param end   the (inclusive) end of the interval.
    * @param size  the size of the buffer.
    * @return {@code true} if {@code query} &in; [{@code start}, {@code to}], {@code false} otherwise.
    */
   public static boolean isInsideBounds(int query, int start, int end, int size)
   {
      if (query < 0 || query >= size)
         return false;
      else if (start <= end)
         return query >= start && query <= end;
      else
         return query <= end || query >= start;
   }

   public static boolean[] ringArrayCopy(boolean[] ringArray, int from, int newLength)
   {
      int length = Math.min(newLength, ringArray.length);

      boolean[] bufferCopy;
      if (from + length <= ringArray.length)
      {
         bufferCopy = Arrays.copyOfRange(ringArray, from, from + newLength);
      }
      else
      {
         int lengthOfFirstCopy = ringArray.length - from;
         bufferCopy = new boolean[newLength];
         System.arraycopy(ringArray, from, bufferCopy, 0, lengthOfFirstCopy);
         System.arraycopy(ringArray, 0, bufferCopy, lengthOfFirstCopy, length - lengthOfFirstCopy);
      }
      return bufferCopy;
   }

   public static double[] ringArrayCopy(double[] ringArray, int from, int newLength)
   {
      int length = Math.min(newLength, ringArray.length);

      double[] bufferCopy;
      if (from + length <= ringArray.length)
      {
         bufferCopy = Arrays.copyOfRange(ringArray, from, from + newLength);
      }
      else
      {
         int lengthOfFirstCopy = ringArray.length - from;
         bufferCopy = new double[newLength];
         System.arraycopy(ringArray, from, bufferCopy, 0, lengthOfFirstCopy);
         System.arraycopy(ringArray, 0, bufferCopy, lengthOfFirstCopy, length - lengthOfFirstCopy);
      }
      return bufferCopy;
   }

   public static int[] ringArrayCopy(int[] ringArray, int from, int newLength)
   {
      int length = Math.min(newLength, ringArray.length);

      int[] bufferCopy;
      if (from + length <= ringArray.length)
      {
         bufferCopy = Arrays.copyOfRange(ringArray, from, from + newLength);
      }
      else
      {
         int lengthOfFirstCopy = ringArray.length - from;
         bufferCopy = new int[newLength];
         System.arraycopy(ringArray, from, bufferCopy, 0, lengthOfFirstCopy);
         System.arraycopy(ringArray, 0, bufferCopy, lengthOfFirstCopy, length - lengthOfFirstCopy);
      }
      return bufferCopy;
   }

   public static long[] ringArrayCopy(long[] ringArray, int from, int newLength)
   {
      int length = Math.min(newLength, ringArray.length);

      long[] bufferCopy;
      if (from + length <= ringArray.length)
      {
         bufferCopy = Arrays.copyOfRange(ringArray, from, from + newLength);
      }
      else
      {
         int lengthOfFirstCopy = ringArray.length - from;
         bufferCopy = new long[newLength];
         System.arraycopy(ringArray, from, bufferCopy, 0, lengthOfFirstCopy);
         System.arraycopy(ringArray, 0, bufferCopy, lengthOfFirstCopy, length - lengthOfFirstCopy);
      }
      return bufferCopy;
   }

   public static byte[] ringArrayCopy(byte[] ringArray, int from, int newLength)
   {
      int length = Math.min(newLength, ringArray.length);

      byte[] bufferCopy;
      if (from + length <= ringArray.length)
      {
         bufferCopy = Arrays.copyOfRange(ringArray, from, from + newLength);
      }
      else
      {
         int lengthOfFirstCopy = ringArray.length - from;
         bufferCopy = new byte[newLength];
         System.arraycopy(ringArray, from, bufferCopy, 0, lengthOfFirstCopy);
         System.arraycopy(ringArray, 0, bufferCopy, lengthOfFirstCopy, length - lengthOfFirstCopy);
      }
      return bufferCopy;
   }
}
