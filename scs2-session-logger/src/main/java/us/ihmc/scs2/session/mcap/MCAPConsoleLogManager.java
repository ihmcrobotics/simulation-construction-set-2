package us.ihmc.scs2.session.mcap;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAP.Channel;
import us.ihmc.scs2.session.mcap.MCAP.Message;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Record;
import us.ihmc.scs2.session.mcap.MCAP.Schema;
import us.ihmc.scs2.simulation.SpyList;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MCAPConsoleLogManager
{
   public static final String FOXGLOVE_LOG = "foxglove::Log";
   private final long desiredLogDT;

   public enum MCAPLogLevel
   {UNKNOWN, DEBUG, INFO, WARNING, ERROR, FATAL}

   private final List<MCAPConsoleLogItem> allConsoleLogItems = new ArrayList<>();
   private final SpyList<MCAPConsoleLogItem> currentConsoleLogItems = new SpyList<>();

   private int channelId = -1;

   public MCAPConsoleLogManager(MCAP mcap, MCAPBufferedChunk chunkBuffer, long desiredLogDT) throws IOException
   {
      this.desiredLogDT = desiredLogDT;
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

            chunkBuffer.peekAllChunkRecords(records ->
                                            {
                                               for (Record record : records)
                                               {
                                                  if (record.op() != Opcode.MESSAGE)
                                                     continue;
                                                  Message message = (Message) record.body();
                                                  if (message.channelId() == channelId)
                                                     allConsoleLogItems.add(parseLogItem(message, desiredLogDT));
                                                  record.unloadBody();
                                               }
                                            });
         }
      }

      allConsoleLogItems.sort(Comparator.comparingLong(MCAPConsoleLogItem::logTime));
   }

   public void update(long logTime)
   {
      if (channelId < 0)
         return;

      // First, if we actually need to update the currentConsoleLogItems
      if (currentConsoleLogItems.isEmpty())
      { // Case 1: We have no currentConsoleLogItems, but logTime is still before the first log item
         if (!allConsoleLogItems.isEmpty() && logTime < allConsoleLogItems.get(0).logTime())
            return;
      }
      else if (currentConsoleLogItems.size() == allConsoleLogItems.size())
      { // Case 2: We loaded all log items, and logTime is after the last log item
         if (logTime >= allConsoleLogItems.get(allConsoleLogItems.size() - 1).logTime())
            return;
      }
      else
      {// Case 3: We have some log items, and logTime is after the last loaded log item and before the next not-loaded log item
         int lastLoadedIndex = currentConsoleLogItems.size() - 1;
         int nextNotLoadedIndex = lastLoadedIndex + 1;
         if (logTime >= currentConsoleLogItems.get(lastLoadedIndex).logTime() && logTime < allConsoleLogItems.get(nextNotLoadedIndex).logTime())
            return;
      }

      int index = Collections.binarySearch(allConsoleLogItems,
                                           new MCAPConsoleLogItem(logTime, null, null, null, null, null, 0),
                                           Comparator.comparingLong(MCAPConsoleLogItem::logTime));
      if (index < 0)
         index = -index - 1;

      List<MCAPConsoleLogItem> updatedList = allConsoleLogItems.subList(0, index);
      if (currentConsoleLogItems.isEmpty())
      {
         currentConsoleLogItems.addAll(updatedList);
      }
      else if (currentConsoleLogItems.size() < updatedList.size())
      {
         int startIndex = currentConsoleLogItems.size();
         currentConsoleLogItems.addAll(updatedList.subList(startIndex, updatedList.size()));
      }
      else if (currentConsoleLogItems.size() > updatedList.size())
      {
         int endIndex = currentConsoleLogItems.size();
         currentConsoleLogItems.subList(updatedList.size(), endIndex).clear();
      }
   }

   public SpyList<MCAPConsoleLogItem> getCurrentConsoleLogItems()
   {
      return currentConsoleLogItems;
   }

   private static MCAPConsoleLogItem parseLogItem(Message message, long desiredLogDT)
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
       *     type_010f744a0100bb8d02000000_030c3_1 instant;
       *     octet level;
       *     string message;
       *     string name;
       *     string file;
       *     unsigned long line;
       * };
       * @formatter:on
       */
      long logTime = MCAPMessageManager.round(message.logTime(), desiredLogDT);
      CDRDeserializer deserializer = new CDRDeserializer();
      deserializer.initialize(message.messageBuffer(), message.offsetData(), message.lengthData());
      Instant instant = Instant.ofEpochSecond(deserializer.read_uint32(), deserializer.read_uint32());
      MCAPLogLevel logLevel = MCAPLogLevel.values()[deserializer.read_uint8()];
      String logMessage = deserializer.read_string();
      String processName = deserializer.read_string();
      String filename = deserializer.read_string();
      long lineNumberInFile = deserializer.read_uint32();
      deserializer.finalize(false);
      return new MCAPConsoleLogItem(logTime, instant, logLevel, logMessage, processName, filename, lineNumberInFile);
   }

   public record MCAPConsoleLogItem(long logTime, Instant instant, MCAPLogLevel logLevel, String message, String processName, String filename,
                                    long lineNumberInFile)
   {
   }
}
