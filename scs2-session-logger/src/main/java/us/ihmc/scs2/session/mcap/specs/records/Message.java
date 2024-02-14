package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;

public interface Message extends MCAPElement
{
   static Message load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new MessageDataInputBacked(dataInput, elementPosition, elementLength);
   }

   int channelId();

   long sequence();

   long logTime();

   long publishTime();

   long dataOffset();

   int dataLength();

   ByteBuffer messageBuffer();

   byte[] messageData();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putUnsignedShort(channelId());
      dataOutput.putUnsignedInt(sequence());
      dataOutput.putLong(logTime());
      dataOutput.putLong(publishTime());
      dataOutput.putUnsignedInt(dataLength());
      dataOutput.putByteBuffer(messageBuffer());
   }

   @Override
   default long getElementLength()
   {
      return dataLength() + Short.BYTES + Integer.BYTES + 2 * Long.BYTES;
   }
}
