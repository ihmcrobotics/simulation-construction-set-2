package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

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
   default long getElementLength()
   {
      return dataLength() + Short.BYTES + Integer.BYTES + 2 * Long.BYTES;
   }
}
