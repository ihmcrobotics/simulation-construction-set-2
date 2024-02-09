package us.ihmc.scs2.session.mcap.output;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public interface MCAPDataOutput
{
   void putLong(long value);

   void putInt(int value);

   default void putUnsignedInt(long value)
   {
      putInt((int) value);
   }

   void putShort(short value);

   default void putUnsignedShort(int value)
   {
      putShort((short) value);
   }

   void putByte(byte value);

   default void putUnsignedByte(int value)
   {
      putByte((byte) value);
   }

   default void putBytes(byte[] bytes)
   {
      putBytes(bytes, 0, bytes.length);
   }

   void putBytes(byte[] bytes, int offset, int length);

   default void putString(String string)
   {
      byte[] bytes = string.getBytes();
      putUnsignedInt(bytes.length);
      putBytes(bytes);
   }

   void putByteBuffer(ByteBuffer byteBuffer);

   void close();

   static MCAPDataOutput wrap(FileChannel fileChannel)
   {
      return new MCAPBufferedFileChannelOutput(fileChannel);
   }
}
