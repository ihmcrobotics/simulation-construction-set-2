package us.ihmc.scs2.sharedMemory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class BufferSampleTest
{
   private static final int ITERATIONS = 1000;

   @Test
   public void testConstructor()
   {
      Random random = new Random(9782);

      for (int i = 0; i < ITERATIONS; i++)
      {
         int bufferSize = random.nextInt(100000);
         int from = random.nextInt(bufferSize);
         int sampleLength = random.nextInt(bufferSize);
         double[] sample = new double[sampleLength];
         for (int j = 0; j < sampleLength; j++)
            sample[j] = random.nextDouble();
         BufferSample<double[]> bufferSample = new BufferSample<>(from, bufferSize, sample, sampleLength);

         assertEquals(from, bufferSample.getFrom());
         int to = from + sampleLength - 1;
         if (to >= bufferSize)
            to -= bufferSize;
         assertEquals(to, bufferSample.getTo());
         assertEquals(bufferSize, bufferSample.getBufferSize());
         assertTrue(sample == bufferSample.getSample());
         assertEquals(sampleLength, bufferSample.getSampleLength());
      }
   }

   @Test
   public void testEquals()
   {
      Random random = new Random(63892);

      for (int i = 0; i < ITERATIONS; i++)
      {
         int bufferSize = random.nextInt(1000);
         int from = random.nextInt(bufferSize);
         int sampleLength = random.nextInt(bufferSize);

         boolean[] booleans = new boolean[sampleLength];
         double[] doubles = new double[sampleLength];
         int[] ints = new int[sampleLength];
         long[] longs = new long[sampleLength];
         byte[] bytes = new byte[sampleLength];

         for (int j = 0; j < sampleLength; j++)
         {
            booleans[j] = random.nextBoolean();
            doubles[j] = random.nextDouble();
            ints[j] = random.nextInt();
            longs[j] = random.nextInt();
            bytes[j] = (byte) random.nextInt();
         }

         BufferSample<boolean[]> booleanSample = new BufferSample<>(from, bufferSize, booleans, sampleLength);
         BufferSample<double[]> doubleSample = new BufferSample<>(from, bufferSize, doubles, sampleLength);
         BufferSample<int[]> intSample = new BufferSample<>(from, bufferSize, ints, sampleLength);
         BufferSample<long[]> longSample = new BufferSample<>(from, bufferSize, longs, sampleLength);
         BufferSample<byte[]> byteSample = new BufferSample<>(from, bufferSize, bytes, sampleLength);

         assertFalse(booleanSample.equals(null));
         assertTrue(booleanSample.equals(booleanSample));
         assertTrue(booleanSample.equals(new BufferSample<>(booleanSample)));
         assertFalse(booleanSample.equals(new BufferSample<>(from + 1, bufferSize, booleans, sampleLength)));
         assertFalse(booleanSample.equals(new BufferSample<>(from, bufferSize + 1, booleans, sampleLength)));
         assertFalse(booleanSample.equals(new BufferSample<>(from, bufferSize, booleans, sampleLength + 1)));
         assertFalse(booleanSample.equals(new Object()));

         assertFalse(doubleSample.equals(null));
         assertTrue(doubleSample.equals(doubleSample));
         assertTrue(doubleSample.equals(new BufferSample<>(doubleSample)));
         assertFalse(doubleSample.equals(new BufferSample<>(from + 1, bufferSize, doubles, sampleLength)));
         assertFalse(doubleSample.equals(new BufferSample<>(from, bufferSize + 1, doubles, sampleLength)));
         assertFalse(doubleSample.equals(new BufferSample<>(from, bufferSize, doubles, sampleLength + 1)));
         assertFalse(doubleSample.equals(new Object()));

         assertFalse(intSample.equals(null));
         assertTrue(intSample.equals(intSample));
         assertTrue(intSample.equals(new BufferSample<>(intSample)));
         assertFalse(intSample.equals(new BufferSample<>(from + 1, bufferSize, ints, sampleLength)));
         assertFalse(intSample.equals(new BufferSample<>(from, bufferSize + 1, ints, sampleLength)));
         assertFalse(intSample.equals(new BufferSample<>(from, bufferSize, ints, sampleLength + 1)));
         assertFalse(intSample.equals(new Object()));

         assertFalse(longSample.equals(null));
         assertTrue(longSample.equals(longSample));
         assertTrue(longSample.equals(new BufferSample<>(longSample)));
         assertFalse(longSample.equals(new BufferSample<>(from + 1, bufferSize, longs, sampleLength)));
         assertFalse(longSample.equals(new BufferSample<>(from, bufferSize + 1, longs, sampleLength)));
         assertFalse(longSample.equals(new BufferSample<>(from, bufferSize, longs, sampleLength + 1)));
         assertFalse(longSample.equals(new Object()));

         assertFalse(byteSample.equals(null));
         assertTrue(byteSample.equals(byteSample));
         assertTrue(byteSample.equals(new BufferSample<>(byteSample)));
         assertFalse(byteSample.equals(new BufferSample<>(from + 1, bufferSize, bytes, sampleLength)));
         assertFalse(byteSample.equals(new BufferSample<>(from, bufferSize + 1, bytes, sampleLength)));
         assertFalse(byteSample.equals(new BufferSample<>(from, bufferSize, bytes, sampleLength + 1)));
         assertFalse(byteSample.equals(new Object()));

         if (sampleLength > 0)
         {
            int index = random.nextInt(sampleLength);

            boolean[] booleans2 = Arrays.copyOf(booleans, sampleLength);
            booleans2[index] = !booleans[index];
            double[] doubles2 = Arrays.copyOf(doubles, sampleLength);
            doubles2[index] += 1.0;
            int[] ints2 = Arrays.copyOf(ints, sampleLength);
            ints2[index] += 1;
            long[] longs2 = Arrays.copyOf(longs, sampleLength);
            longs2[index] += 1L;
            byte[] bytes2 = Arrays.copyOf(bytes, sampleLength);
            bytes2[index] += (byte) 1;

            assertFalse(booleanSample.equals(new BufferSample<>(from, bufferSize, booleans2, sampleLength)));
            assertFalse(doubleSample.equals(new BufferSample<>(from, bufferSize, doubles2, sampleLength)));
            assertFalse(intSample.equals(new BufferSample<>(from, bufferSize, ints2, sampleLength)));
            assertFalse(longSample.equals(new BufferSample<>(from, bufferSize, longs2, sampleLength)));
            assertFalse(byteSample.equals(new BufferSample<>(from, bufferSize, bytes2, sampleLength)));
         }
      }
   }

   @Test
   public void testSampleEquals()
   {
      Random random = new Random(80925);

      assertTrue(BufferSample.sampleEquals(null, null));
      assertFalse(BufferSample.sampleEquals(new boolean[10], null));
      assertFalse(BufferSample.sampleEquals(null, new boolean[10]));
      assertThrows(IllegalArgumentException.class, () -> BufferSample.sampleEquals(new Object(), new boolean[10]));
      assertThrows(IllegalArgumentException.class, () -> BufferSample.sampleEquals(new boolean[10], new Object()));

      for (int i = 0; i < ITERATIONS; i++)
      {
         int size = random.nextInt(200);
         boolean[] booleans = new boolean[size];
         double[] doubles = new double[size];
         int[] ints = new int[size];
         long[] longs = new long[size];
         byte[] bytes = new byte[size];

         for (int j = 0; j < size; j++)
         {
            booleans[j] = random.nextBoolean();
            doubles[j] = random.nextDouble();
            ints[j] = random.nextInt();
            longs[j] = random.nextInt();
            bytes[j] = (byte) random.nextInt();
         }

         assertTrue(BufferSample.sampleEquals(booleans, booleans));
         assertTrue(BufferSample.sampleEquals(booleans, Arrays.copyOf(booleans, size)));
         assertTrue(BufferSample.sampleEquals(doubles, doubles));
         assertTrue(BufferSample.sampleEquals(doubles, Arrays.copyOf(doubles, size)));
         assertTrue(BufferSample.sampleEquals(ints, ints));
         assertTrue(BufferSample.sampleEquals(ints, Arrays.copyOf(ints, size)));
         assertTrue(BufferSample.sampleEquals(longs, longs));
         assertTrue(BufferSample.sampleEquals(longs, Arrays.copyOf(longs, size)));
         assertTrue(BufferSample.sampleEquals(bytes, bytes));
         assertTrue(BufferSample.sampleEquals(bytes, Arrays.copyOf(bytes, size)));

         if (size > 0)
         {
            assertFalse(BufferSample.sampleEquals(booleans, Arrays.copyOf(booleans, size - 1)));
            assertFalse(BufferSample.sampleEquals(doubles, Arrays.copyOf(doubles, size - 1)));
            assertFalse(BufferSample.sampleEquals(ints, Arrays.copyOf(ints, size - 1)));
            assertFalse(BufferSample.sampleEquals(longs, Arrays.copyOf(longs, size - 1)));
            assertFalse(BufferSample.sampleEquals(bytes, Arrays.copyOf(bytes, size - 1)));
         }
         assertFalse(BufferSample.sampleEquals(booleans, Arrays.copyOf(booleans, size + 1)));
         assertFalse(BufferSample.sampleEquals(doubles, Arrays.copyOf(doubles, size + 1)));
         assertFalse(BufferSample.sampleEquals(ints, Arrays.copyOf(ints, size + 1)));
         assertFalse(BufferSample.sampleEquals(longs, Arrays.copyOf(longs, size + 1)));
         assertFalse(BufferSample.sampleEquals(bytes, Arrays.copyOf(bytes, size + 1)));

         assertFalse(BufferSample.sampleEquals(booleans, doubles));

         if (size > 0)
         {
            int index = random.nextInt(size);

            boolean[] booleans2 = Arrays.copyOf(booleans, size);
            booleans2[index] = !booleans[index];
            double[] doubles2 = Arrays.copyOf(doubles, size);
            doubles2[index] += 1.0;
            int[] ints2 = Arrays.copyOf(ints, size);
            ints2[index] += 1;
            long[] longs2 = Arrays.copyOf(longs, size);
            longs2[index] += 1L;
            byte[] bytes2 = Arrays.copyOf(bytes, size);
            bytes2[index] += (byte) 1;

            assertFalse(BufferSample.sampleEquals(booleans, booleans2));
            assertFalse(BufferSample.sampleEquals(doubles, doubles2));
            assertFalse(BufferSample.sampleEquals(ints, ints2));
            assertFalse(BufferSample.sampleEquals(longs, longs2));
            assertFalse(BufferSample.sampleEquals(bytes, bytes2));
         }
      }
   }
}
