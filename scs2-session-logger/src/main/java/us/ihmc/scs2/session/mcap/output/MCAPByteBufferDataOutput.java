package us.ihmc.scs2.session.mcap.output;

import java.nio.ByteBuffer;

public class MCAPByteBufferDataOutput implements MCAPDataOutput
{
   private static final int DEFAULT_INITIAL_CAPACITY = 8192;
   private static final int DEFAULT_GROWTH_FACTOR = 2;

   private ByteBuffer buffer;
   private int growthFactor;

   public MCAPByteBufferDataOutput()
   {
      this(DEFAULT_INITIAL_CAPACITY, DEFAULT_GROWTH_FACTOR);
   }

   public MCAPByteBufferDataOutput(int initialCapacity, int growthFactor)
   {
      this.growthFactor = growthFactor;
      buffer = ByteBuffer.allocate(initialCapacity);
   }

   @Override
   public void putLong(long value)
   {
      ensureCapacity(Long.BYTES);
      buffer.putLong(value);
   }

   @Override
   public void putInt(int value)
   {
      ensureCapacity(Integer.BYTES);
      buffer.putInt(value);
   }

   @Override
   public void putShort(short value)
   {
      ensureCapacity(Short.BYTES);
      buffer.putShort(value);
   }

   @Override
   public void putByte(byte value)
   {
      ensureCapacity(Byte.BYTES);
      buffer.put(value);
   }

   @Override
   public void putBytes(byte[] bytes, int offset, int length)
   {
      ensureCapacity(length);
      buffer.put(bytes, offset, length);
   }

   @Override
   public void putByteBuffer(ByteBuffer byteBuffer)
   {
      ensureCapacity(byteBuffer.remaining());
      buffer.put(byteBuffer);
   }

   @Override
   public void close()
   {
      buffer.flip();
   }

   private void ensureCapacity(int bytesToWrite)
   {
      if (buffer.remaining() < bytesToWrite)
      {
         int newCapacity = Math.max(buffer.capacity() * growthFactor, buffer.position() + bytesToWrite);
         ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
         buffer.flip();
         newBuffer.put(buffer);
         buffer = newBuffer;
      }
   }

   public ByteBuffer getBuffer()
   {
      return buffer;
   }
}
