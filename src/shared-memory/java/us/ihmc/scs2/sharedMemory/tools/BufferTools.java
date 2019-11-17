package us.ihmc.scs2.sharedMemory.tools;

import java.util.Arrays;

public class BufferTools
{
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
