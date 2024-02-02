package us.ihmc.scs2.session.mcap;

import com.github.luben.zstd.ZstdDecompressCtx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public interface MCAPDataInput
{
   enum Compression
   {
      NONE, LZ4, ZSTD;

      public static Compression fromString(String name)
      {
         return switch (name.trim().toLowerCase())
         {
            case "none", "" -> NONE;
            case "lz4" -> LZ4;
            case "zstd" -> ZSTD;
            default -> throw new IllegalArgumentException("Unsupported compression: " + name);
         };
      }
   }

   void position(long position);

   long position();

   default void skip(long length)
   {
      position(position() + length);
   }

   long size();

   long getLong();

   int getInt();

   default long getUnsignedInt()
   {
      return Integer.toUnsignedLong(getInt());
   }

   short getShort();

   default int getUnsignedShort()
   {
      return Short.toUnsignedInt(getShort());
   }

   byte getByte();

   default int getUnsignedByte()
   {
      return Byte.toUnsignedInt(getByte());
   }

   void getBytes(byte[] bytes);

   default byte[] getBytes(int length)
   {
      byte[] bytes = new byte[length];
      getBytes(bytes);
      return bytes;
   }

   byte[] getBytes(long offset, int length);

   default String getString()
   {
      return new String(getBytes((int) getUnsignedInt()));
   }

   ByteBuffer getByteBuffer(long offset, int length, boolean direct);

   ByteBuffer getDecompressedByteBuffer(long offset, int compressedLength, int uncompressedLength, Compression compression, boolean direct);

   static MCAPDataInput wrap(FileChannel fileChannel)
   {
      return new MCAPBufferedFileChannelInput(fileChannel);
   }

   static MCAPDataInput wrap(ByteBuffer buffer)
   {
      return new MCAPByteBufferDataInput(buffer);
   }

   class MCAPBufferedFileChannelInput implements MCAPDataInput
   {
      private static final int DEFAULT_BUFFER_SIZE = 8192;

      private long _pos;
      private final ByteBuffer readingBuffer;
      private final FileChannel fileChannel;

      public MCAPBufferedFileChannelInput(FileChannel fileChannel)
      {
         this.fileChannel = fileChannel;
         readingBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
         readingBuffer.order(ByteOrder.LITTLE_ENDIAN);
         try
         {
            _pos = fileChannel.position();
            fileChannel.read(readingBuffer);
            readingBuffer.flip();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public void position(long newPosition)
      {
         if (newPosition == position())
            return;

         try
         {
            if (newPosition > _pos && newPosition < _pos + readingBuffer.limit())
            {
               readingBuffer.position((int) (newPosition - _pos));
               return;
            }

            _pos = newPosition;
            fileChannel.position(newPosition);
            readingBuffer.clear();
            fileChannel.read(readingBuffer);
            readingBuffer.flip();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public long position()
      {
         return _pos + readingBuffer.position();
      }

      @Override
      public long size()
      {
         try
         {
            return fileChannel.size();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public long getLong()
      {
         if (readingBuffer.remaining() < Long.BYTES)
            fillBuffer();
         return readingBuffer.getLong();
      }

      @Override
      public int getInt()
      {
         if (readingBuffer.remaining() < Integer.BYTES)
            fillBuffer();
         return readingBuffer.getInt();
      }

      @Override
      public short getShort()
      {
         if (readingBuffer.remaining() < Short.BYTES)
            fillBuffer();
         return readingBuffer.getShort();
      }

      @Override
      public byte getByte()
      {
         if (readingBuffer.remaining() < Byte.BYTES)
            fillBuffer();
         return readingBuffer.get();
      }

      @Override
      public void getBytes(byte[] bytes)
      {
         int length = bytes.length;
         int remaining = length;

         if (size() - position() < length)
            throw new IndexOutOfBoundsException(
                  "End of file reached. Requested: " + length + ", remaining: " + (size() - position()) + ", position: " + position() + ", size: " + size());

         while (remaining > 0)
         {
            if (readingBuffer.remaining() < remaining)
            {
               int toRead = readingBuffer.remaining();
               readingBuffer.get(bytes, length - remaining, toRead);
               remaining -= toRead;
               fillBuffer();
            }
            else
            {
               readingBuffer.get(bytes, length - remaining, remaining);
               remaining = 0;
            }
         }
      }

      @Override
      public byte[] getBytes(long offset, int length)
      {
         byte[] bytes = new byte[length];

         try
         {
            if (offset >= _pos && offset + length < _pos + readingBuffer.limit())
               readingBuffer.get((int) (offset - _pos), bytes);
            else
               fileChannel.read(ByteBuffer.wrap(bytes), offset);
            return bytes;
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public ByteBuffer getByteBuffer(long offset, int length, boolean direct)
      {
         try
         {
            ByteBuffer buffer = direct ? ByteBuffer.allocateDirect(length) : ByteBuffer.allocate(length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            if (offset >= _pos && offset + length < _pos + readingBuffer.limit())
            {
               buffer.put(0, readingBuffer, (int) (offset - _pos), length);
            }
            else
            {
               fileChannel.read(buffer, offset);
               buffer.flip();
            }
            return buffer;
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public ByteBuffer getDecompressedByteBuffer(long offset, int compressedLength, int uncompressedLength, Compression compression, boolean direct)
      {
         if (compression == Compression.NONE)
            return getByteBuffer(offset, uncompressedLength, direct);

         ByteBuffer compressedBuffer = getByteBuffer(offset,
                                                     compressedLength,
                                                     compression == Compression.ZSTD); // TODO Try to use internal buffer instead when possible
         ByteBuffer decompressedBuffer;

         if (compression == Compression.LZ4)
         {
            LZ4FrameDecoder lz4FrameDecoder = new LZ4FrameDecoder();
            decompressedBuffer = direct ? ByteBuffer.allocateDirect(uncompressedLength) : ByteBuffer.allocate(uncompressedLength);
            lz4FrameDecoder.decode(compressedBuffer, 0, compressedLength, decompressedBuffer, 0);
         }
         else if (compression == Compression.ZSTD)
         {
            try (ZstdDecompressCtx zstdDecompressCtx = new ZstdDecompressCtx())
            {
               decompressedBuffer = zstdDecompressCtx.decompress(compressedBuffer, uncompressedLength);
            }
         }
         else
         {
            throw new IllegalArgumentException("Unsupported compression: " + compression);
         }

         decompressedBuffer.order(ByteOrder.LITTLE_ENDIAN);
         return decompressedBuffer;
      }

      private void fillBuffer()
      {
         try
         {
            readingBuffer.compact();
            int bytesRead = fileChannel.read(readingBuffer);
            _pos += bytesRead;
            readingBuffer.flip();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public String toString()
      {
         return "MCAPBufferedFileChannelInput{" + "readingBuffer=" + readingBuffer + ", fileChannel=" + fileChannel + '}';
      }
   }

   class MCAPByteBufferDataInput implements MCAPDataInput
   {
      private final ByteBuffer buffer;

      private MCAPByteBufferDataInput(ByteBuffer buffer)
      {
         this.buffer = buffer;
         buffer.order(ByteOrder.LITTLE_ENDIAN);
      }

      @Override
      public void position(long position)
      {
         buffer.position((int) position);
      }

      @Override
      public long position()
      {
         return buffer.position();
      }

      @Override
      public long size()
      {
         return buffer.limit();
      }

      @Override
      public long getLong()
      {
         return buffer.getLong();
      }

      @Override
      public int getInt()
      {
         return buffer.getInt();
      }

      @Override
      public short getShort()
      {
         return buffer.getShort();
      }

      @Override
      public byte getByte()
      {
         return buffer.get();
      }

      @Override
      public void getBytes(byte[] bytes)
      {
         buffer.get(bytes);
      }

      @Override
      public byte[] getBytes(long offset, int length)
      {
         byte[] bytes = new byte[length];
         buffer.get((int) offset, bytes);
         return bytes;
      }

      @Override
      public ByteBuffer getByteBuffer(long offset, int length, boolean direct)
      {
         ByteBuffer out = direct ? ByteBuffer.allocateDirect(length) : ByteBuffer.allocate(length);
         out.put(0, buffer, (int) offset, length);
         out.order(ByteOrder.LITTLE_ENDIAN);
         return out;
      }

      @Override
      public ByteBuffer getDecompressedByteBuffer(long offset, int compressedLength, int uncompressedLength, Compression compression, boolean direct)
      {
         ByteBuffer compressedBuffer = getByteBuffer(offset, compressedLength, false);
         ByteBuffer decompressedBuffer;

         if (compression == Compression.LZ4)
         {
            LZ4FrameDecoder lz4FrameDecoder = new LZ4FrameDecoder();
            decompressedBuffer = direct ? ByteBuffer.allocateDirect(uncompressedLength) : ByteBuffer.allocate(uncompressedLength);
            lz4FrameDecoder.decode(buffer, (int) offset, compressedLength, decompressedBuffer, 0);
         }
         else if (compression == Compression.ZSTD)
         {
            try (ZstdDecompressCtx zstdDecompressCtx = new ZstdDecompressCtx())
            {
               int previousPosition = buffer.position();
               int previousLimit = buffer.limit();
               buffer.limit((int) (offset + compressedLength));
               buffer.position((int) offset);
               decompressedBuffer = zstdDecompressCtx.decompress(compressedBuffer, uncompressedLength);
               buffer.position(previousPosition);
               buffer.limit(previousLimit);
            }
         }
         else
         {
            throw new IllegalArgumentException("Unsupported compression: " + compression);
         }

         decompressedBuffer.order(ByteOrder.LITTLE_ENDIAN);
         return decompressedBuffer;
      }

      @Override
      public String toString()
      {
         return "MCAPByteBufferDataInput{" + "buffer=" + buffer + '}';
      }
   }
}
