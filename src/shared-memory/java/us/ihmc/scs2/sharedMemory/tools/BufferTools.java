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
    * @throws IllegalArgumentException  if {@code length} is negative or equal to zero.
    * @throws IndexOutOfBoundsException if {@code from} is not &in; [0, {@code length}[.
    * @throws IndexOutOfBoundsException if {@code to} is not &in; [0, {@code length}[.
    */
   public static int computeSubLength(int from, int to, int length)
   {
      if (length <= 0)
         throw new IllegalArgumentException("length must be greater than zero, was: " + length);
      if (from < 0 || from >= length)
         throw new IndexOutOfBoundsException("from is out-of-bound, should be in [0, " + length + "[, but was: " + from);
      if (to < 0 || to >= length)
         throw new IndexOutOfBoundsException("to is out-of-bound, should be in [0, " + length + "[, but was: " + to);

      int subLength = to - from + 1;
      if (subLength <= 0)
         subLength += length;
      return subLength;
   }

   /**
    * Calculates the start point of an interval defined in a ring buffer.
    * 
    * @param to        the (inclusive) end of the interval.
    * @param subLength the number of elements in the interval.
    * @param length    the number of elements of the entire ring buffer.
    * @return the index of the interval's start point.
    * @throws IllegalArgumentException  if {@code length} is negative or equal to zero.
    * @throws IllegalArgumentException  if {@code subLength} is either negative, equal to zero or
    *                                   greater than {@code length}.
    * @throws IndexOutOfBoundsException if {@code to} is not &in; [0, {@code length}[.
    */
   public static int computeFromIndex(int to, int subLength, int length)
   {
      if (length <= 0)
         throw new IllegalArgumentException("length must be greater than zero, was: " + length);
      if (subLength <= 0)
         throw new IllegalArgumentException("sub-length must be greater than zero, was: " + subLength);
      if (subLength > length)
         throw new IllegalArgumentException("sub-length must be smaller than length, was: " + subLength + ", length was " + length);
      if (to < 0 || to >= length)
         throw new IndexOutOfBoundsException("to is out-of-bound, should be in [0, " + length + "[, but was: " + to);

      int from = to - subLength + 1;
      if (from < 0)
         from += length;
      return from;
   }

   /**
    * Calculates the end point of an interval defined in a ring buffer.
    * 
    * @param from      the (inclusive) start of the interval.
    * @param subLength the number of elements in the interval.
    * @param length    the number of elements of the entire ring buffer.
    * @return the index of the interval's end point.
    * @throws IllegalArgumentException  if {@code length} is negative or equal to zero.
    * @throws IllegalArgumentException  if {@code subLength} is either negative, equal to zero or
    *                                   greater than {@code length}.
    * @throws IndexOutOfBoundsException if {@code from} is not &in; [0, {@code length}[.
    */
   public static int computeToIndex(int from, int subLength, int length)
   {
      if (length <= 0)
         throw new IllegalArgumentException("length must be greater than zero, was: " + length);
      if (subLength <= 0)
         throw new IllegalArgumentException("sub-length must be greater than zero, was: " + subLength);
      if (subLength > length)
         throw new IllegalArgumentException("sub-length must be smaller than length, was: " + subLength + ", length was " + length);
      if (from < 0 || from >= length)
         throw new IndexOutOfBoundsException("from is out-of-bound, should be in [0, " + length + "[, but was: " + from);

      int to = from + subLength - 1;
      if (to >= length)
         to -= length;
      return to;
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
