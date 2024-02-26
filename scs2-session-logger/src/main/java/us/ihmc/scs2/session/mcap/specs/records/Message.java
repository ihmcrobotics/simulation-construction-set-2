package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Message records encode a single timestamped message on a channel.
 * The message encoding and schema must match that of the Channel record corresponding to the message's channel ID.
 *
 * @see <a href="https://mcap.dev/spec#message-op0x05">MCAP Message</a>
 */
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

   @Override
   default boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof Message other)
      {
         if (channelId() != other.channelId())
            return false;
         if (sequence() != other.sequence())
            return false;
         if (logTime() != other.logTime())
            return false;
         if (publishTime() != other.publishTime())
            return false;
         if (dataOffset() != other.dataOffset())
            return false;
         if (dataLength() != other.dataLength())
            return false;
         return Objects.equals(messageBuffer(), other.messageBuffer());
      }

      return false;
   }
}
