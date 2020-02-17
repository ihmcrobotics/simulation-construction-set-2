package us.ihmc.scs2.sharedMemory.tools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.commons.RandomNumbers;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;

public class BufferToolsTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testComputeSubLength()
   {
      Random random = new Random(34536);

      { // Test exceptions
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeSubLength(0, 10, 0));
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeSubLength(0, 10, -1));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeSubLength(0, 10, 5));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeSubLength(5, 4, 5));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeSubLength(-1, 4, 5));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeSubLength(0, -1, 5));
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         int length = random.nextInt(1000) + 1;
         int from = random.nextInt(length);
         int to = random.nextInt(length);
         int subLength = BufferTools.computeSubLength(from, to, length);

         // Use naive approach to measure the length

         int index = from;
         int expectedSubLength = 1;

         while (index != to)
         {
            index++;
            if (index >= length)
               index = 0;
            expectedSubLength++;
         }

         assertEquals(expectedSubLength, subLength);
      }
   }

   @Test
   public void testComputeFromIndex()
   {
      Random random = new Random(2456);

      { // Test exceptions
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeFromIndex(9, 1, 0));
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeFromIndex(9, 0, 1));
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeFromIndex(9, 2, 1));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeFromIndex(11, 10, 11));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeFromIndex(-1, 10, 11));
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         int length = random.nextInt(1000) + 1;
         int to = random.nextInt(length);
         int subLength = random.nextInt(length - 1) + 1;
         int from = BufferTools.computeFromIndex(to, subLength, length);

         assertEquals(subLength, BufferTools.computeSubLength(from, to, length));
      }
   }

   @Test
   public void testComputeToIndex()
   {
      Random random = new Random(2456);

      { // Test exceptions
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeToIndex(9, 1, 0));
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeToIndex(9, 0, 1));
         assertThrows(IllegalArgumentException.class, () -> BufferTools.computeToIndex(9, 2, 1));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeToIndex(11, 10, 11));
         assertThrows(IndexOutOfBoundsException.class, () -> BufferTools.computeToIndex(-1, 10, 11));
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         int length = random.nextInt(1000) + 1;
         int from = random.nextInt(length);
         int subLength = random.nextInt(length - 1) + 1;
         int to = BufferTools.computeToIndex(from, subLength, length);

         assertEquals(subLength, BufferTools.computeSubLength(from, to, length));
      }
   }

   @Test
   public void testRingArrayCopyWithBoolean()
   {
      Random random = new Random(4353);

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test full copy.
         int length = random.nextInt(1000) + 1;
         boolean[] source = nextBooleanArray(random, length);
         boolean[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
         double[] source = nextDoubleArray(random, length);
         double[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
         int[] source = nextIntegerArray(random, length);
         int[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
         long[] source = nextLongArray(random, length);
         long[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
         byte[] source = nextByteArray(random, length);
         byte[] copy = BufferTools.ringArrayCopy(source, 0, length);
         assertArrayEquals(source, copy);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Trivial example: Test partial copy without going over the source's length.
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
         int length = random.nextInt(1000) + 1;
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
