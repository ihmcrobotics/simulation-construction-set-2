package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;

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
