package us.ihmc.scs2.sharedMemory.tools;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;

import static org.junit.jupiter.api.Assertions.*;

public class BufferToolsTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testRingArrayCopyWithBoolean()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(20000) + 1;
         boolean[] source = nextBooleanArray(random, length);
         boolean[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(20000) + 1;
         boolean[] source = nextBooleanArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(length - from);
         boolean[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[k + from])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test full copy starting at random index.
         int length = random.nextInt(20000) + 1;
         int from = random.nextInt(length);
         boolean[] source = nextBooleanArray(random, length);
         boolean[] copy = BufferTools.ringArrayCopy(source, from, length);

         for (int k = 0; k < length; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + "\nCopy: " + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test partial copy going over the source's length.
         int length = random.nextInt(20000) + 1;
         boolean[] source = nextBooleanArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(from + 1) + length - from;
         boolean[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }
   }

   @Test
   public void testRingArrayCopyWithDouble()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(20000) + 1;
         double[] source = nextDoubleArray(random, length);
         double[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(20000) + 1;
         double[] source = nextDoubleArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(length - from);
         double[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[k + from])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test full copy starting at random index.
         int length = random.nextInt(20000) + 1;
         int from = random.nextInt(length);
         double[] source = nextDoubleArray(random, length);
         double[] copy = BufferTools.ringArrayCopy(source, from, length);

         for (int k = 0; k < length; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + "\nCopy: " + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test partial copy going over the source's length.
         int length = random.nextInt(20000) + 1;
         double[] source = nextDoubleArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(from + 1) + length - from;
         double[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }
   }

   @Test
   public void testRingArrayCopyWithInteger()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(20000) + 1;
         int[] source = nextIntegerArray(random, length);
         int[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(20000) + 1;
         int[] source = nextIntegerArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(length - from);
         int[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[k + from])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test full copy starting at random index.
         int length = random.nextInt(20000) + 1;
         int from = random.nextInt(length);
         int[] source = nextIntegerArray(random, length);
         int[] copy = BufferTools.ringArrayCopy(source, from, length);

         for (int k = 0; k < length; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + "\nCopy: " + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test partial copy going over the source's length.
         int length = random.nextInt(20000) + 1;
         int[] source = nextIntegerArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(from + 1) + length - from;
         int[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }
   }

   @Test
   public void testRingArrayCopyWithLong()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(20000) + 1;
         long[] source = nextLongArray(random, length);
         long[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(20000) + 1;
         long[] source = nextLongArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(length - from);
         long[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[k + from])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test full copy starting at random index.
         int length = random.nextInt(20000) + 1;
         int from = random.nextInt(length);
         long[] source = nextLongArray(random, length);
         long[] copy = BufferTools.ringArrayCopy(source, from, length);

         for (int k = 0; k < length; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + "\nCopy: " + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test partial copy going over the source's length.
         int length = random.nextInt(20000) + 1;
         long[] source = nextLongArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(from + 1) + length - from;
         long[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }
   }

   @Test
   public void testRingArrayCopyWithByte()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(20000) + 1;
         byte[] source = nextByteArray(random, length);
         byte[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(20000) + 1;
         byte[] source = nextByteArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(length - from);
         byte[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[k + from])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test full copy starting at random index.
         int length = random.nextInt(20000) + 1;
         int from = random.nextInt(length);
         byte[] source = nextByteArray(random, length);
         byte[] copy = BufferTools.ringArrayCopy(source, from, length);

         for (int k = 0; k < length; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + "\nCopy: " + Arrays.toString(copy));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Circular example: Test partial copy going over the source's length.
         int length = random.nextInt(20000) + 1;
         byte[] source = nextByteArray(random, length);
         int from = random.nextInt(length);
         int copyLength = random.nextInt(from + 1) + length - from;
         byte[] copy = BufferTools.ringArrayCopy(source, from, copyLength);

         for (int k = 0; k < copyLength; k++)
         {
            if (copy[k] != source[(k + from) % length])
               throw new AssertionError("Source: " + Arrays.toString(source) + "\nfrom=" + from + ", copyLength=" + copyLength + "\nCopy: "
                     + Arrays.toString(copy));
         }
      }
   }

   public static boolean[] nextBooleanArray(Random random, int length)
   {
      boolean[] next = new boolean[length];
      for (int i = 0; i < length; i++)
         next[i] = random.nextBoolean();
      return next;
   }

   public static double[] nextDoubleArray(Random random, int length)
   {
      double[] next = new double[length];
      for (int i = 0; i < length; i++)
         next[i] = EuclidCoreRandomTools.nextDouble(random);
      return next;
   }

   public static int[] nextIntegerArray(Random random, int length)
   {
      int[] next = new int[length];
      for (int i = 0; i < length; i++)
         next[i] = RandomNumbers.nextInt(random, -100000, 100000);
      return next;
   }

   public static long[] nextLongArray(Random random, int length)
   {
      long[] next = new long[length];
      for (int i = 0; i < length; i++)
         next[i] = RandomNumbers.nextInt(random, -100000, 100000);
      return next;
   }

   public static byte[] nextByteArray(Random random, int length)
   {
      byte[] next = new byte[length];
      random.nextBytes(next);
      return next;
   }
}
