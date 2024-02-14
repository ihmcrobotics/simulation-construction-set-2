package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.List;

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
}
