package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.encoding.MCAPCRC32Helper;
import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.List;
import java.util.Objects;

/**
 * Channel records define encoded streams of messages on topics.
 * Channel records are uniquely identified within a file by their channel ID.
 * A Channel record must occur at least once in the file prior to any message referring to its channel ID.
 * Any two channel records sharing a common ID must be identical.
 *
 * @see <a href="https://mcap.dev/spec#channel-op0x04">MCAP Channel</a>
 */
public interface Channel extends MCAPElement
{
   static Channel load(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      return new ChannelDataInputBacked(dataInput, elementPosition, elementLength);
   }

   int id();

   int schemaId();

   String topic();

   String messageEncoding();

   List<StringPair> metadata();

   @Override
   default void write(MCAPDataOutput dataOutput)
   {
      dataOutput.putInt(id());
      dataOutput.putInt(schemaId());
      dataOutput.putString(topic());
      dataOutput.putString(messageEncoding());
      dataOutput.putCollection(metadata());
   }

   @Override
   default MCAPCRC32Helper updateCRC(MCAPCRC32Helper crc32)
   {
      if (crc32 == null)
         crc32 = new MCAPCRC32Helper();
      crc32.addInt(id());
      crc32.addInt(schemaId());
      crc32.addString(topic());
      crc32.addString(messageEncoding());
      crc32.addCollection(metadata());
      return crc32;
   }

   @Override
   default String toString(int indent)
   {
      String out = getClass().getSimpleName() + ":";
      out += "\n\t-id = " + id();
      out += "\n\t-schemaId = " + schemaId();
      out += "\n\t-topic = " + topic();
      out += "\n\t-messageEncoding = " + messageEncoding();
      out += "\n\t-metadata = [%s]".formatted(metadata().toString());
      return MCAPElement.indent(out, indent);
   }

   @Override
   default boolean equals(MCAPElement mcapElement)
   {
      if (mcapElement == this)
         return true;

      if (mcapElement instanceof Channel other)
      {
         if (id() != other.id())
            return false;
         if (schemaId() != other.schemaId())
            return false;
         if (!Objects.equals(topic(), other.topic()))
            return false;
         if (!Objects.equals(messageEncoding(), other.messageEncoding()))
            return false;
         return Objects.equals(metadata(), other.metadata());
      }

      return false;
   }
}
