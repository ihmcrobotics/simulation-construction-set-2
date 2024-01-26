package us.ihmc.scs2.session.mcap;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAP.Channel;
import us.ihmc.scs2.session.mcap.MCAP.Message;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Schema;
import us.ihmc.scs2.session.mcap.MCAPBufferedChunk.ChunkBundle;
import us.ihmc.scs2.simulation.SpyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MCAPConsoleLogManager
{
   public static final String FOXGLOVE_LOG = "foxglove::Log";

   public enum MCAPLogLevel
   {UNKNOWN, DEBUG, INFO, WARNING, ERROR, FATAL}

   private final List<MCAPConsoleLogItem> allConsoleLogItems = new ArrayList<>();
   private final SpyList<MCAPConsoleLogItem> currentConsoleLogItems = new SpyList<>();

   private int channelId = -1;

   public MCAPConsoleLogManager(MCAP mcap, MCAPBufferedChunk chunkBuffer) throws IOException
   {
      Optional<Schema> logSchema = mcap.records()
                                       .stream()
                                       .filter(record -> record.op() == Opcode.SCHEMA)
                                       .map(record -> (Schema) record.body())
                                       .filter(schema -> schema.name().equals(FOXGLOVE_LOG))
                                       .findFirst();

      if (logSchema.isPresent())
      {
         int id = logSchema.get().id();

         Optional<Channel> logChannel = mcap.records()
                                            .stream()
                                            .filter(record -> record.op() == Opcode.CHANNEL)
                                            .map(record -> (Channel) record.body())
                                            .filter(channel -> channel.schemaId() == id)
                                            .findFirst();
         if (logChannel.isPresent())
         {
            if (!"cdr".equalsIgnoreCase(logChannel.get().messageEncoding()))
            {
               LogTools.error("Unsupported message encoding: {}", logChannel.get().messageEncoding());
               return;
            }

            channelId = logChannel.get().id();

            for (ChunkBundle chunkBundle : chunkBuffer.getChunkBundles())
            {
               chunkBundle.requestLoadChunk(true);
               chunkBundle.forEachRecord(Opcode.MESSAGE, record ->
               {
                  Message message = (Message) record.body();
                  if (message.channelId() == channelId)
                     allConsoleLogItems.add(parseLogItem(message));
               });
            }
         }
      }
   }

   private static MCAPConsoleLogItem parseLogItem(Message message)
   {
      /*
       * @formatter:off
       * Expected schema:
       * struct type_010f744a0100bb8d02000000_030c3_1
       * {
       *     unsigned long sec;
       *     unsigned long nsec;
       * };
       *
       * struct foxglove::Log
       * {
       *     type_010f744a0100bb8d02000000_030c3_1 timestamp;
       *     octet level;
       *     string message;
       *     string name;
       *     string file;
       *     unsigned long line;
       * };
       * @formatter:on
       */
      CDRDeserializer deserializer = new CDRDeserializer();
      deserializer.initialize(message.messageBuffer(), message.offsetData(), message.lengthData());
      MCAPConsoleLogTimestamp timestamp = new MCAPConsoleLogTimestamp(deserializer.read_uint32(), deserializer.read_uint32());
      MCAPLogLevel logLevel = MCAPLogLevel.values()[deserializer.read_uint8()];
      String logMessage = deserializer.read_string();
      String processName = deserializer.read_string();
      String filename = deserializer.read_string();
      long lineNumberInFile = deserializer.read_uint32();
      deserializer.finalize(false);
      return new MCAPConsoleLogItem(timestamp, logLevel, logMessage, processName, filename, lineNumberInFile);
   }

   public record MCAPConsoleLogItem(MCAPConsoleLogTimestamp timestamp, MCAPLogLevel logLevel, String message, String processName, String filename,
                                    long lineNumberInFile)
   {
   }

   public record MCAPConsoleLogTimestamp(long seconds, long nanoseconds)
   {
   }
}
